/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * RegexCapture.kt
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

import edu.jhuapl.data.parsnip.value.ValueCompute
import java.util.regex.PatternSyntaxException

/** Captures groups from a regex and adds them to target fields. Use an empty string to skip a field. */
class RegexCapture(var regex: String, var `as`: List<String>): ValueCompute<Map<String, Any?>> {

    constructor(regex: String, vararg `as`: String): this(regex, listOf(*`as`))

    private val compiledRegex: Regex?
        get() = try {
            regex.toRegex()
        } catch (x: PatternSyntaxException) {
            null
        }

    override fun invoke(p1: Any?): Map<String, Any?>? {
        return when (val match = compiledRegex?.matchEntire(p1.toString())) {
            null -> null
            else -> valueMap(`as`, match.groupValues)
        }
    }

    private fun valueMap(fields: List<String>, values: List<String>): Map<String, String> {
        return fields.mapIndexed { i, s -> s to values.getOrNull(i + 1) }
                .filter { it.first.isNotEmpty() && it.second != null }
                .map { it.first to it.second!! }
                .toMap()
    }

}
