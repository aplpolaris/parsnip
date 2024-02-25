/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * Stats.kt
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
package edu.jhuapl.data.parsnip.dataset.compute

import com.fasterxml.jackson.annotation.JsonCreator
import edu.jhuapl.data.parsnip.dataset.DataSet
import edu.jhuapl.data.parsnip.dataset.DataSetCompute
import edu.jhuapl.data.parsnip.datum.Datum
import edu.jhuapl.util.types.SimpleValue
import edu.jhuapl.util.types.toNumberOrNull
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Finds datum where a given field is minimized, omitting nulls.
 * Returns null if there is no numbers for the minimum computation.
 */
data class ArgMin @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor (var field: String) : DataSetCompute<Datum>, SimpleValue {
    override val simpleValue: String
        get() = this.field

    override fun invoke(input: DataSet): Datum? {
        val map = input.map { it to it[field]?.toNumberOrNull()?.toDouble() }
                .filter { it.second != null }
        return map.minByOrNull { it.second!! }?.first
    }
}

/**
 * Finds datum where a given field is minimized, omitting nulls.
 * Returns null if there is no numbers for the minimum computation.
 */
data class ArgMax @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor (var field: String) : DataSetCompute<Datum>, SimpleValue {
    override val simpleValue: String
        get() = this.field

    override fun invoke(input: DataSet): Datum? {
        val map = input.map { it to it[field]?.toNumberOrNull()?.toDouble() }
                .filter { it.second != null }
        return map.maxByOrNull { it.second!! }?.first
    }
}

private fun Double?.orSmaller(other: Double) = if (this == null) other else min(this, other)
private fun Double?.orLarger(other: Double) = if (this == null) other else max(this, other)

/** Compute extended double statistics of a list of numbers. */
fun List<Number?>.extendedDoubleStatistics(): ExtendedDoubleStatistics {
    var n = 0L
    var nulls = 0
    var invalids = 0
    var min: Double? = null
    var max: Double? = null
    var sum = 0.0
    var sumSq = 0.0
    var sumCb = 0.0
    forEach {
        when (it) {
            null -> nulls++
            is Number -> {
                val value = it.toDouble()
                n++
                sum += value
                sumSq += value * value
                sumCb += value * value * value
                min = min.orSmaller(value)
                max = max.orLarger(value)
            }
            else -> invalids++
        }
    }
    return ExtendedDoubleStatistics(n, nulls, invalids, min, max, sum, sumSq, sumCb)
}

data class ExtendedDoubleStatistics(val count: Long, val nullCount: Int, val invalidCount: Int,
                                    val min: Double?, val max: Double?,
                                    val sum: Double, val sumSq: Double, val sumCb: Double) {
    val average: Double
        get() = if (count == 0L) Double.NaN else sum / count
    val mean: Double
        get() = average
    val variance: Double
        get() = if (count == 0L) 0.0 else (sumSq - sum * sum / count) / count
    val standardDeviation: Double
        get() = if (count == 0L) Double.NaN else sqrt(variance)

}
