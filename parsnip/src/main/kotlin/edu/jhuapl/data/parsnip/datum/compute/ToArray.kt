/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * ToArray.kt
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
package edu.jhuapl.data.parsnip.datum.compute

import com.fasterxml.jackson.annotation.JsonCreator
import edu.jhuapl.data.parsnip.datum.DatumCompute
import edu.jhuapl.util.types.SimpleValue

/**
 * Converts a datum to an array using its natural field ordering. An optional flag can be used to flatten array values.
 * @param fields name of fields to get data from
 * @param flatten whether to flatten any field values
 * @param keepFieldNames if true, returns an array with two arrays, the first including the list of fields
 */
class ToArray @JsonCreator constructor(var fields: List<String>? = null,
                                                                           var flatten: Boolean = false,
                                                                           var keepFieldNames: Boolean = false) : DatumCompute<Array<Any?>> {
    constructor(flatten: Boolean) : this(null, flatten)
    constructor(vararg fields: String, flatten: Boolean = false) : this(listOf(*fields), flatten)

    override fun invoke(map: Map<String, *>): Array<Any?> {
        val values = when (flatten) {
            true -> (fields ?: map.keys).flatMap { map[it].asAnIterable() }.toTypedArray()
            else -> (fields ?: map.keys).map { map[it] }.toTypedArray()
        }
        return when (keepFieldNames) {
            true -> arrayOf(fields?.toTypedArray() ?: arrayOf<String>(), values)
            else -> values
        }
    }
}

/**
 * Converts an object to a list. Leaves the object unchanged if it's already a list. Converts to a list if it's an
 * array. Otherwise returns a list of one element.
 */
fun Any?.asList(): List<Any?> = asAnIterable().toList()

/**
 * Converts an object to an iterable. Leaves the object unchanged if it's already an iterable. Converts to an iterable
 * if it's an array. Otherwise, returns a list of one element.
 */
fun Any?.asAnIterable(): Iterable<Any?> = when (this) {
    is Iterable<*> -> this
    is Array<*> -> this.asIterable()
    is CharArray -> this.asIterable()
    is ByteArray -> this.asIterable()
    is ShortArray -> this.asIterable()
    is IntArray -> this.asIterable()
    is LongArray -> this.asIterable()
    is FloatArray -> this.asIterable()
    is DoubleArray -> this.asIterable()
    is BooleanArray -> this.asIterable()
    else -> listOf(this)
}
