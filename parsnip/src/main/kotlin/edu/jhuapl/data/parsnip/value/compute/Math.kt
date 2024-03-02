/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * Math.kt
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
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import edu.jhuapl.data.parsnip.datum.compute.Operate
import edu.jhuapl.data.parsnip.value.ValueCompute
import edu.jhuapl.util.types.SimpleValue
import edu.jhuapl.util.types.toNumberOrNull

/** Helper class where simple value is a number. */
sealed class NumberCompute(var value: Number, @JsonIgnore val operate: Operate) : ValueCompute<Any>, SimpleValue {
    override val simpleValue: Number
        get() = value
    override fun invoke(input: Any?) = operate(listOf(input.toNumberOrNull(), value), null)
}

/** Adds a constant value.  */
class Add @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(value: Number) : NumberCompute(value, Operate.ADD)
/** Subtracts a constant value.  */
class Subtract @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(value: Number) : NumberCompute(value, Operate.SUBTRACT)
/** Multiplies by a constant value.  */
class Multiply @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(value: Number) : NumberCompute(value, Operate.MULTIPLY)
/** Divides by a constant value.  */
class Divide @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(value: Number) : NumberCompute(value, Operate.DIVIDE)

/**
 * Looks up a value in a field and applies a linear transformation to convert it to a target value. The transformation is
 * defined by domain and range intervals.
 * @param <X> input type
 */
class Linear @JsonCreator constructor(
    @JsonProperty("domain") domain: DoubleArray = doubleArrayOf(0.0, 1.0),
    @JsonProperty("range") range: DoubleArray = doubleArrayOf(0.0, 1.0)
) : ValueCompute<Double> {

    constructor(domain: Pair<Number, Number>, range: Pair<Number, Number>) : this(domain.asDoubleArray(), range.asDoubleArray())

    // todo - can use delegating properties here to check valid
    var domain: DoubleArray = domain.ensureLength2()
        set(x) {
            field = x.ensureLength2()
        }
    var range: DoubleArray = range.ensureLength2()
        set(x) {
            field = x.ensureLength2()
        }

    @get:JsonIgnore
    val scale
        get() = (range[1] - range[0]) / (domain[1] - domain[0])

    override fun invoke(input: Any?) = input?.toNumberOrNull()?.let {
        range[0] + (it.toDouble() - domain[0]) * scale
    }
}

//region HELPER FUNCTIONS

/** Ensures array has length 2. */
private fun DoubleArray.ensureLength2(): DoubleArray {
    require(size == 2) { "Array must have length2" }
    return this
}
/** Lets us convert a [Pair] of [Number]s to a double array. */
private fun Pair<Number, Number>.asDoubleArray() = doubleArrayOf(first.toDouble(), second.toDouble())

//endregion
