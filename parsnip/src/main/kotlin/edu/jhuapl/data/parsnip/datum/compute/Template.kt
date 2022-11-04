package edu.jhuapl.data.parsnip.datum.compute

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * Template.kt
 * edu.jhuapl.data:parsnip
 * %%
 * Copyright (C) 2019 - 2022 Johns Hopkins University Applied Physics Laboratory
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.fasterxml.jackson.annotation.JsonCreator
import edu.jhuapl.data.parsnip.datum.DatumCompute
import edu.jhuapl.util.types.SimpleValue
import edu.jhuapl.util.types.atPointer
import edu.jhuapl.utilkt.core.fine
import edu.jhuapl.utilkt.core.warning
import java.text.SimpleDateFormat
import java.util.*
import java.util.stream.Stream

/**
 * Reconstructs value string from a template, with pointers referencing the given root object.
 * Requires all content between successive braces { and } to be a JSON pointer to an element in the given object.
 * Also allows a single JSON pointer (if the template starts with /), or checking multiple fields if /key1;/key2.
 */
class Template @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(var template: String) : DatumCompute<String>, SimpleValue {
    var forNull = "null"

    override val simpleValue: Any?
        get() = if (forNull == "null") template else this

    override fun invoke(map: Map<String, *>): String? = valueFromTemplate(map, template, forNull)
}

/**
 * Reconstructs value string from a template, with pointers referencing the given root object. Requires all content
 * between successive braces { and } to be a JSON pointer to an element in the given object. Also allows a single
 * JSON pointer (if the template starts with /), or checking multiple fields if /key1;/key2.
 *
 * @param obj object to reference
 * @param jsonPointerTemplate the string template with JSON pointers.
 * @param useForNull value to insert if a template pointer is null
 * @return reconstructed string value (missing if the template is null or invalid)
 */
fun valueFromTemplate(obj: Any, jsonPointerTemplate: String?, useForNull: String = "null"): String? {
    if (jsonPointerTemplate == null) {
        return null
    } else if (jsonPointerTemplate.startsWith("/")) {
        val spl = jsonPointerTemplate.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (s in spl) {
            obj.atPointer(s)?.let { return it.toString() }
        }
        return null
    }

    val res = StringBuilder()
    var i0 = 0
    var i1 = jsonPointerTemplate.indexOf('{', i0)
    while (i1 != -1) {
        res.append(jsonPointerTemplate.substring(i0, i1))
        val i2 = jsonPointerTemplate.indexOf('}', i1)
        if (i2 == -1) {
            fine<Template>("Invalid JSON pointer template: '{' without '}': {0}", jsonPointerTemplate)
            return jsonPointerTemplate
        }
        val field = jsonPointerTemplate.substring(i1 + 1, i2)
        val value = when {
            likelyDateTimeField(field) -> userFriendlyDateTime(obj.atPointer(field))
                    ?: useForNull
            else -> obj.atPointer(field, String::class.java) ?: useForNull
        }
        res.append(value)
        i0 = i2 + 1
        i1 = jsonPointerTemplate.indexOf('{', i0)
    }
    res.append(jsonPointerTemplate.substring(i0))
    return res.toString()
}

//region HELPER FUNCTIONS

private val DF = SimpleDateFormat.getDateTimeInstance()
private val LIKELY_DATE_TIME_FIELDS = arrayOf("date", "time")

/**
 * Test if given field name is likely a date/time field, to attempt user-friendly UI presentation.
 * @param field field to check
 * @return true if a likely date/time field
 */
private fun likelyDateTimeField(field: String): Boolean {
    val lc = field.toLowerCase()
    return Stream.of(*LIKELY_DATE_TIME_FIELDS).anyMatch { lc.contains(it) }
}

/**
 * Get user-friendly date/time version of given value. Works if the value is a [Date]
 * or an epoch timestamp (long or integer).
 * @param value to attempt to convert
 * @return user-friendly version
 */
private fun userFriendlyDateTime(value: Any?): String? {
    return when (value) {
        null -> null
        is Date -> DF.format(value)
        is Int -> DF.format(Date(value.toLong()))
        is Long -> DF.format(Date(value))
        else -> Objects.toString(value)
    }
}

//endregion
