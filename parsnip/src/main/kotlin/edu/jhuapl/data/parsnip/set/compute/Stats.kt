package edu.jhuapl.data.parsnip.set.compute

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * Stats.kt
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

import edu.jhuapl.data.parsnip.dataset.compute.ExtendedDoubleStatistics
import edu.jhuapl.data.parsnip.dataset.compute.extendedDoubleStatistics
import edu.jhuapl.data.parsnip.set.ValueSet
import edu.jhuapl.data.parsnip.set.ValueSetCompute
import edu.jhuapl.util.types.toNumberOrNull
import java.util.*
import kotlin.reflect.KClass

/** Computes statistics for number field. */
object Stats : ValueSetCompute<ExtendedDoubleStatistics> {
    override fun invoke(input: ValueSet) = input.map { it.toNumberOrNull() }.extendedDoubleStatistics()
}

/** Computes statistics for integer number field. */
object IntStats : ValueSetCompute<IntSummaryStatistics> {
    override fun invoke(input: ValueSet) = input.mapNotNull { it.toNumberOrNull()?.toInt() }
            .stream().mapToInt { i -> i }.summaryStatistics()
}

/** Sums values, omitting nulls. */
object Sum : FlexibleNumberCompute() {
    @Suppress("UNCHECKED_CAST")
    override fun <N: Number> invoke(numbers: List<Number?>, type: KClass<N>): N {
        return when (type) {
            Double::class -> doubles(numbers).sum() as N
            Long::class -> longs(numbers).sum() as N
            Float::class -> floats(numbers).sum() as N
            Int::class -> ints(numbers).sum() as N
            Short::class -> shorts(numbers).sum() as N
            Byte::class -> bytes(numbers).sum() as N
            else -> TODO("Numbers of type $type not supported")
        }
    }
}

/** Computes average value, omitting nulls. Synonym for [Average]. */
object Mean : FlexibleNumberCompute() {
    @Suppress("UNCHECKED_CAST")
    override fun <N : Number> invoke(numbers: List<Number?>, type: KClass<N>): N {
        return when (type) {
            Double::class -> doubles(numbers).average() as N
            Long::class -> longs(numbers).average() as N
            Float::class -> floats(numbers).average() as N
            Int::class -> ints(numbers).average() as N
            Short::class -> shorts(numbers).average() as N
            Byte::class -> bytes(numbers).average() as N
            else -> TODO("Numbers of type $type not supported")
        }
    }
}

/** Computes average value, omitting nulls. Synonym for [Mean]. */
object Average : FlexibleNumberCompute() {
    override fun <N : Number> invoke(numbers: List<Number?>, type: KClass<N>): N = Mean.invoke(numbers, type)
}

/** Computes min numeric value, omitting nulls. */
object Min : FlexibleNumberCompute() {
    @Suppress("UNCHECKED_CAST")
    override fun <N : Number> invoke(numbers: List<Number?>, type: KClass<N>): N {
        return when (type) {
            Double::class -> doubles(numbers).minOrNull() as N
            Long::class -> longs(numbers).minOrNull() as N
            Float::class -> floats(numbers).minOrNull() as N
            Int::class -> ints(numbers).minOrNull() as N
            Short::class -> shorts(numbers).minOrNull() as N
            Byte::class -> bytes(numbers).minOrNull() as N
            else -> TODO("Numbers of type $type not supported")
        }
    }
}

/** Computes max numeric value, omitting nulls. */
object Max : FlexibleNumberCompute() {
    @Suppress("UNCHECKED_CAST")
    override fun <N : Number> invoke(numbers: List<Number?>, type: KClass<N>): N {
        return when (type) {
            Double::class -> doubles(numbers).maxOrNull() as N
            Long::class -> longs(numbers).maxOrNull() as N
            Float::class -> floats(numbers).maxOrNull() as N
            Int::class -> ints(numbers).maxOrNull() as N
            Short::class -> shorts(numbers).maxOrNull() as N
            Byte::class -> bytes(numbers).maxOrNull() as N
            else -> TODO("Numbers of type $type not supported")
        }
    }
}

//region TYPE-FLEXIBLE COMPUTE HELPERS

/** Helper class, performing flexible smart casting to numbers */
sealed class FlexibleNumberCompute : ValueSetCompute<Number> {
    override fun invoke(input: ValueSet): Number {
        val numbers = input.map { it.toNumberOrNull() }
        val type = numbers.assumedNumberType()
        return invoke(numbers, type)
    }

    abstract fun <N: Number> invoke(numbers: List<Number?>, type: KClass<N>): N
}

private fun doubles(numbers: List<Number?>, whenNull: () -> Double = throwNpe()) = numbers.map { it?.toDouble() ?: whenNull() }
private fun floats(numbers: List<Number?>, whenNull: () -> Float = throwNpe()) = numbers.map { it?.toFloat() ?: whenNull() }
private fun longs(numbers: List<Number?>, whenNull: () -> Long = throwNpe()) = numbers.map { it?.toLong() ?: whenNull() }
private fun ints(numbers: List<Number?>, whenNull: () -> Int = throwNpe()) = numbers.map { it?.toInt() ?: whenNull() }
private fun shorts(numbers: List<Number?>, whenNull: () -> Short = throwNpe()) = numbers.map { it?.toShort() ?: whenNull() }
private fun bytes(numbers: List<Number?>, whenNull: () -> Byte = throwNpe()) = numbers.map { it?.toByte() ?: whenNull() }

private fun <N: Number> throwNpe(): () -> N = { throw NullPointerException() }

/** Get the most expressive numeric type in iterable */
private fun List<Number?>.assumedNumberType(): KClass<out Number> = with (numericTypes()) {
    return when {
        isEmpty() -> Double::class
        size == 1 -> first()
        contains(Double::class) -> Double::class
        contains(Long::class) -> Long::class
        contains(Float::class) -> Float::class
        contains(Int::class) -> Int::class
        contains(Short::class) -> Short::class
        contains(Byte::class) -> Byte::class
        else -> TODO("Numbers of type $this not supported")
    }
}

/** Get set of numeric types in iterable. */
private fun List<Number?>.numericTypes() = mapNotNull { it?.javaClass?.kotlin as? KClass<out Number> }.toSet()

//endregion
