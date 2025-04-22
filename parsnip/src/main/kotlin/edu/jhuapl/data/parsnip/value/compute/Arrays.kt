/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * Arrays.kt
 * edu.jhuapl.data:parsnip
 * %%
 * Copyright (C) 2019 - 2025 Johns Hopkins University Applied Physics Laboratory
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
package edu.jhuapl.data.parsnip.value.compute

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import edu.jhuapl.data.parsnip.value.ValueCompute

/** Converts a single array to a list of values with one index field and one value field. */
data class FlattenList @JsonCreator constructor(@JsonProperty("as") var asField: String,
                                                @JsonProperty("index") var index: String) : ValueCompute<Any?> {
    override fun invoke(p: Any?) = when(p) {
        is List<*> -> p.flattenToMaps(asField, index)
        else -> p
    }
}

/** Converts a matrix to a list of values with two index fields and one value field. */
data class FlattenMatrix @JsonCreator constructor(@JsonProperty("as") var asField: String,
                                                  @JsonProperty("index1") var index1: String,
                                                  @JsonProperty("index2") var index2: String) : ValueCompute<Any?> {
    override fun invoke(p: Any?) =
            if (p is List<*> && p.all { it is List<*> }) (p as List<List<*>>).flattenMatrixToMaps(asField, index1, index2) else p
}

/** Converts a list of objects into a list of maps with key-values. */
fun <X> List<X>.flattenToMaps(valueLabel: String, index: String) = mapIndexed { i, it -> mutableMapOf(valueLabel to it, index to i) }

/** Converts a matrix of objects into a list of maps with key-values. */
fun <X> List<List<X>>.flattenMatrixToMaps(valueLabel: String, index1: String, index2: String)
        = mapIndexed { i, list -> list.flattenToMaps(valueLabel, index2).onEach { it += index1 to i } }.flatten()
