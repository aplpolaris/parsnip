/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * OneHot.kt
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
package edu.jhuapl.data.parsnip.value.compute

import com.fasterxml.jackson.annotation.JsonCreator
import edu.jhuapl.data.parsnip.value.ValueCompute
import edu.jhuapl.util.types.SimpleValue
import java.lang.IllegalStateException

/**
 * Converts "enum" value to an array of binary 1/0 options. Any values that do not match one of the enum values will
 * return all zeros.
 */
class OneHot<X> @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(var values: List<X>) : ValueCompute<IntArray>, SimpleValue {

    constructor(vararg values: X): this(listOf(*values))

    override val simpleValue: Any
        get() = values

    override fun invoke(p1: Any?) = when (val i = values.indexOf(p1)) {
        -1 -> IntArray(values.size)
        else -> IntArray(values.size) { if (it == i) 1 else 0 }
    }

}
