/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * Sort.kt
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
import edu.jhuapl.data.parsnip.dataset.DataSet
import edu.jhuapl.data.parsnip.dataset.DataSetTransform
import edu.jhuapl.data.parsnip.datum.Datum
import edu.jhuapl.util.types.ObjectOrdering
import edu.jhuapl.util.types.SimpleValue

/** Sort data by the given field(s) */
data class SortBy @JsonCreator constructor(override var fields: List<String>): FieldSort(fields, true) {
    constructor(vararg fields: String): this(fields.toList())
}

/** Sort data by the given field(s), descending */
data class SortByDescending @JsonCreator constructor(override var fields: List<String>): FieldSort(fields, false) {
    constructor(vararg fields: String): this(fields.toList())
}

//region HELPER CLASSES


/** Helper class for sorters */
sealed class FieldSort(open var fields: List<String>, var ascending: Boolean = true): DataSetTransform, SimpleValue {
    override val simpleValue: Any?
        get() = fields

    override fun invoke(p1: DataSet) = when {
        ascending -> p1.sortedBy { ComparableTuple(it, fields) }
        else -> p1.sortedByDescending { ComparableTuple(it, fields) }
    }
}

private class ComparableTuple(val datum: Datum, val fields: List<String>) : Comparable<ComparableTuple> {
    override fun compareTo(other: ComparableTuple): Int = fields.asSequence()
            .map { ObjectOrdering.compare(datum[it], other.datum[it]) }
            .firstOrNull { it != 0 } ?: 0
}

//endregion
