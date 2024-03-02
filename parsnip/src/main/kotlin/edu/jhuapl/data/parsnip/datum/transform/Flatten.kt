/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * Flatten.kt
 * edu.jhuapl.data:parsnip
 * %%
 * Copyright (C) 2019 - 2024 Johns Hopkins University Applied Physics Laboratory
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
package edu.jhuapl.data.parsnip.datum.transform

import com.fasterxml.jackson.annotation.JsonCreator
import edu.jhuapl.data.parsnip.datum.Datum
import edu.jhuapl.data.parsnip.datum.MultiDatumTransform

/**
 * Split one datum into several datums, one for each value in an array value field. For instance, flattening ``{ a: [1, 2] }``
 * will produce two datums: ``{ a: 1 }, { a: 2 }``. If there are multiple fields to flatten, this may either select each
 * unique set of elements (one from each field), or "collate" them, in which case the fields should have a parallel structure.
 */
data class Flatten @JsonCreator constructor(var fields: List<String>, var `as`: List<String> = emptyList(), var collate: Boolean = true): MultiDatumTransform {

    constructor(vararg fields: String) : this(listOf(*fields))

    override fun invoke(datum: Datum): List<Datum> {
        var res = listOf(datum)
        if (collate) {
            res = res.flatMap { flattenCollatedFields(it) }
        } else {
            fields.forEachIndexed { i, f ->
                res = res.flatMap { flattenField(f, `as`.getOrNull(i), it) }
            }
        }
        return res
    }

    /** Flatten multiple fields simultaneously. */
    private fun flattenCollatedFields(datum: Datum): List<Datum> {
        val maxSize = fields.mapNotNull { (datum[it] as? Iterable<*>)?.count() }.maxOrNull() ?: return listOf(datum)
        return (0 until maxSize).map {
            datum.toMutableMap().apply {
                fields.forEachIndexed { i, f ->
                    val fieldName = `as`.getOrNull(i) ?: f
                    val fieldValue = when(val value = datum[f]) {
                        is Iterable<*> -> value.toList().getOrNull(it)
                        else -> if (it == 0) value else null
                    }
                    put(fieldName, fieldValue)
                }
                if (`as`.isNotEmpty()) {
                    (fields - `as`).forEach { remove(it) }
                }
            }
        }
    }

    /** Flatten a single field. */
    private fun flattenField(field: String, fieldName: String?, datum: Datum): List<Datum> {
        return when (val value = datum[field]) {
            is Iterable<*> -> value.map { augment(datum, field, fieldName ?: field, it) }
            else -> listOf(datum)
        }
    }

    /** Augment a datum with given field/value. */
    private fun augment(datum: Datum, oldField: String, newField: String, value: Any?) = datum.toMutableMap().apply {
        remove(oldField)
        put(newField, value)
    }

}
