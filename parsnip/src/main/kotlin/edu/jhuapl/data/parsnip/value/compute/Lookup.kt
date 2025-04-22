package edu.jhuapl.data.parsnip.value.compute

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * Lookup.kt
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

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import edu.jhuapl.data.parsnip.value.ValueCompute
import edu.jhuapl.util.types.SimpleValue

/**
 * Performs lookup operations to transform values from a source field. The lookup table uses strings for matching inputs.
 */
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
data class Lookup(
        /** Maps data values that trigger to the reported trigger string.  */
        var table: Map<String, Any?> = mutableMapOf(),
        /** Value to return field is null  */
        var ifNull: Any? = null,
        /** If test should be case-sensitive (default) or not.  */
        var caseSensitive: Boolean = true) : ValueCompute<Any>, SimpleValue {

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    constructor(table: Map<String, Any?>) : this(table, null, true)

    override val simpleValue: Any?
        get() = if (ifNull == null && caseSensitive) table else this

    /**
     * Builder function that adds a value to the lookup table and returns this.
     */
    fun put(key: String, value: Any?): Lookup {
        table = if (table is MutableMap) table else table.toMutableMap()
        (table as MutableMap)[key] = value
        return this
    }

    override fun invoke(p1: Any?): Any? = with(p1.toString()) {
        when {
            caseSensitive -> table[this]
            else -> table.getIgnoreCase(this)
        } ?: ifNull
    }

}

private fun <V> Map<String, V>.getIgnoreCase(key: String): V? {
    return keys.find { it.equals(key, ignoreCase = true) }?.let { get(it) }
}