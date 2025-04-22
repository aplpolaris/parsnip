/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * DatasetFunctions.kt
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
package edu.jhuapl.data.parsnip.dataset

import edu.jhuapl.data.parsnip.dataset.compute.ExtendedDoubleStatistics
import edu.jhuapl.data.parsnip.dataset.compute.Values
import edu.jhuapl.data.parsnip.dataset.transform.*
import edu.jhuapl.data.parsnip.datum.Datum
import edu.jhuapl.data.parsnip.datum.DatumTransform
import edu.jhuapl.data.parsnip.set.ValueSequence
import edu.jhuapl.data.parsnip.set.ValueSet
import edu.jhuapl.data.parsnip.set.stats
import edu.jhuapl.data.parsnip.set.intStats
import edu.jhuapl.data.parsnip.set.compute.Count
import java.util.*

/** A collection of [Datum] objects, which has size and can be iterated over. */
typealias DataSet = Collection<Datum>
/** A sequence of [Datum] objects, for use when size is not known or unbounded. */
typealias DataSequence = Sequence<Datum>

/** Computes a scalar output value for a [DataSet] input. */
interface DataSetCompute<out Y>: (DataSet) -> Y?
/** Computes a boolean output value for a [DataSet] input. */
interface DataSetFilter: (DataSet) -> Boolean
/** Transforms one [DataSet] to another. */
interface DataSetTransform : DataSetCompute<DataSet>

//region FACTORY METHODS

/** Applies a transform that applies a [DatumTransform] to each element. Omits null values. */
fun DataSet.onEach(transform: DatumTransform) = mapNotNull { transform(it) }
/** Creates a transform that applies a [DatumTransform] to each element. Omits null values. */
fun onEach(transform: DatumTransform) = object: DataSetTransform {
    override fun invoke(p1: DataSet) = p1.mapNotNull { transform(it) }
}

/** Chains a collection of transforms together to create a new one. */
fun chain(vararg transforms: DataSetTransform) = Chain(transforms.toList())

/** Limits to the first n records. */
fun DataSet.limit(n: Int) = Limit(n).invoke(this)
/** Limits to the first n records. */
fun limit(n: Int) = Limit(n)

/** Computes stats for given field. */
fun DataSet.stats(field: String): ExtendedDoubleStatistics = Values(field).invoke(this).stats()
/** Computes stats for given field. */
fun DataSet.intStats(field: String): IntSummaryStatistics = Values(field).invoke(this).intStats()
/** Computes stats for given field. */
fun stats(field: String): (DataSet) -> ExtendedDoubleStatistics = { set: DataSet -> set.stats(field) }

/** Sort data by the given field. */
fun DataSet.sortBy(field: String) = SortBy(field).invoke(this)
/** Sort data by the given field. */
fun sortBy(field: String) = SortBy(field)

/** Sort data by the given field (descending). */
fun DataSet.sortByDescending(field: String) = SortByDescending(field).invoke(this)
/** Sort data by the given field (descending). */
fun sortByDescending(field: String) = SortByDescending(field)

/** Create aggregate to count tuples with field names in [groupBy], with the result count stored in [asField]. */
fun DataSet.count(groupBy: Iterable<String>, asField: String) = Aggregate(groupBy, Count, null, asField).invoke(this)
/** Create aggregate to count tuples with field names in [groupBy], with the result count stored in [asField]. */
fun count(groupBy: Iterable<String>, asField: String) = Aggregate(groupBy, Count, null, asField)

/** Combo function that finds the tuples with the top counts. */
fun DataSet.topTuples(groupBy: Iterable<String>, asField: String, limit: Int) = count(groupBy, asField).sortByDescending(asField).limit(limit)
/** Combo function that finds the tuples with the top counts. */
fun topTuples(groupBy: Iterable<String>, asField: String, limit: Int) = chain(
        count(groupBy, asField),
        sortByDescending(asField),
        limit(limit)
)

//endregion

//region EXTENSION FUNCTIONS

/** Gets a value set for a given field. */
fun DataSet.valueSet(field: String): ValueSet = map { it[field] }

/** Gets a value set for a given field. */
fun DataSequence.valueSequence(field: String): ValueSequence = map { it[field] }

//endregion
