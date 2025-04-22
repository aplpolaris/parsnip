/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * Chain.kt
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
package edu.jhuapl.data.parsnip.dataset.transform

import com.fasterxml.jackson.annotation.JsonCreator
import edu.jhuapl.data.parsnip.dataset.DataSequence
import edu.jhuapl.data.parsnip.dataset.DataSet
import edu.jhuapl.data.parsnip.dataset.DataSetTransform
import edu.jhuapl.util.types.SimpleValue

/** Limits to the first N results. */
data class Limit @JsonCreator constructor(var n: Int): DataSetTransform, SimpleValue {
    override val simpleValue: Any?
        get() = n

    override fun invoke(p1: DataSet) = p1.take(n)
}

/** Performs each of the given transforms in order. */
data class Chain @JsonCreator constructor(var transforms: List<DataSetTransform>): DataSetTransform, SimpleValue {
    override val simpleValue: Any?
        get() = transforms

    override fun invoke(p1: DataSet): DataSet? {
        var set: DataSet = p1
        transforms.forEach { set = it(set) ?: return null }
        return set
    }
}
