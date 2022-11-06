package edu.jhuapl.data.parsnip.schema

import com.fasterxml.jackson.annotation.JsonIgnore
import edu.jhuapl.data.parsnip.dataset.DataSet
import edu.jhuapl.data.parsnip.datum.Datum
import edu.jhuapl.data.parsnip.datum.MutableDatum
import edu.jhuapl.util.types.numberType
import java.time.Instant
import java.util.*

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * DataSchema.kt
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

/**
 * Combines name/description with a list of field names and associated types.
 */
class DataSchema(var name: String = "", var description: String? = null, _fields: Map<String, Class<*>> = mapOf()) {

    var fields: MutableMap<String, Class<*>> = _fields.toMutableMap()

    val fieldNames
        @JsonIgnore
        get() = fields.keys
    val numericFields
        @JsonIgnore
        get() = fields.filterValues { it.numberType() }
    val categoricalFields
        @JsonIgnore
        get() = fields.filterValues { it === String::class.java || it.isEnum }

    constructor(name: String = "", description: String? = null, vararg fields: Pair<String, Class<*>>) : this(name, description, mutableMapOf(*fields))

    fun containsField(f: String) = fields.containsKey(f)

    fun type(f: String) = fields[f]

    fun copy(): DataSchema = DataSchema(name, description, fields.toMutableMap())

    //region DSL builders

    /** DSL type-safe builder function. */
    inline fun <reified X> field(name: String) = (name to X::class.java).apply {
        fields[first] = second
    }

    fun string(name: String) = field<String>(name)
    fun boolean(name: String) = field<Boolean>(name)
    fun short(name: String) = field<Short>(name)
    fun int(name: String) = field<Int>(name)
    fun long(name: String) = field<Long>(name)
    fun float(name: String) = field<Float>(name)
    fun double(name: String) = field<Double>(name)
    fun date(name: String) = field<Date>(name)
    fun instant(name: String) = field<Instant>(name)

    //endregion
}

/** Wraps a [DataSet] with metadata and schema information. */
data class DataSetWrapper(var data: DataSet = emptyList(), var metadata: MutableDatum = mutableMapOf(), var schema: DataSchema = DataSchema())

//region BUILDER CODE

/** DSL type-safe builder function for creating a schema. */
fun schema(name: String = "", description: String? = null, init: DataSchema.() -> Unit) = DataSchema(name, description).apply { init() }

//endregion

//region EXTENSION FUNCTIONS

/** Convert list of arguments into a [Datum] using the given list of names. Ignores any extra values that do not have an indexed field name. */
fun DataSchema.datum(vararg any: Any?) = fieldNames.toList().datum(*any)

/**
 * Creates schema from data, using up to the given limit number of [Datum]s. The type of a field will be the first discovered
 * non-string type, if there is one.
 */
fun DataSet.createSchema(limit: Int = Int.MAX_VALUE): DataSchema {
    val types = mutableMapOf<String, Class<out Any>>()
    take(limit).forEach {
        it.forEach { (k, v) ->
            types.compute(k) { _, type ->
                if (type == null || type == String::class.java) v?.javaClass ?: String::class.java else type
            }
        }
    }
    return DataSchema("", null, types)
}

/** Convert list of arguments into a [Datum] using the given list of names. Ignores any extra values that do not have an indexed field name. */
private fun List<String>.datum(vararg any: Any?): Datum = listOf(*any).mapIndexedNotNull { i, v -> getOrNull(i)?.let { it to v } }.toMap()

//endregion
