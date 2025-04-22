package edu.jhuapl.data.parsnip.dataset.transform

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * Aggregate.kt
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

import edu.jhuapl.data.parsnip.dataset.*
import edu.jhuapl.data.parsnip.datum.Datum
import edu.jhuapl.data.parsnip.set.ValueSetCompute
import edu.jhuapl.data.parsnip.set.compute.Count

/**
 * Counts or performs other statistical summaries of data. Optionally groups results by one or more fields.
 */
class Aggregate(var groupBy: Iterable<String> = emptyList(), var op: ValueSetCompute<*> = Count, var field: String? = null, var asField: String) : DataSetTransform {
    init {
        require(op is Count || field != null) { "Field must be supplied if not a count aggregate" }
    }
    override fun invoke(p1: DataSet): DataSet {
        require(op == Count || field != null) { "Field must be supplied if not a count aggregate" }
        return tupleGroup(p1, groupBy).map {
            mapKeysToValues(groupBy, it.key) + mapOf(asField to calc(op, it.value, field))
        }
    }
}

//region PRIVATE METHODS

/** Groups datum's by tuple. Resulting keys are tuples, values are the associated datums. */
private fun tupleGroup(p: DataSet, groupBy: Iterable<String>): Map<List<Any?>, List<Datum>>
        = p.groupBy { datum -> groupBy.map { datum[it] } }

/** Create map from separate lists of keys and values */
private fun <K, V> mapKeysToValues(keys: Iterable<K>, values: Iterable<V>): Map<K, V> {
    var res = mutableMapOf<K, V>()
    val i1 = keys.iterator()
    val i2 = values.iterator()
    while (i1.hasNext() && i2.hasNext()) {
        res[i1.next()] = i2.next()
    }
    return res
}

/** Apply calculation to set of values */
private fun calc(op: ValueSetCompute<*>, d: DataSet, f: String?): Any? = when (op) {
    Count -> op(d)
    else -> op(d.valueSet(f!!))
}

//endregion
