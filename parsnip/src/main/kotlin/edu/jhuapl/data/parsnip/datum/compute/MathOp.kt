package edu.jhuapl.data.parsnip.datum.compute

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * MathOp.kt
 * edu.jhuapl.data:parsnip
 * %%
 * Copyright (C) 2019 - 2022 Johns Hopkins University Applied Physics Laboratory
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
import com.fasterxml.jackson.annotation.JsonProperty
import edu.jhuapl.data.parsnip.datum.DatumCompute
import edu.jhuapl.data.parsnip.value.filter.*
import edu.jhuapl.util.types.*
import edu.jhuapl.utilkt.core.fine
import java.util.logging.Level
import kotlin.math.pow
import kotlin.math.sqrt

/** Performs a mathematical operation on numeric or date/time input fields, e.g. add/subtract/divide.  */
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
class MathOp : DatumCompute<Any> {
    /** Fields to combine  */
    var fields: List<String>
    /** The operator to apply.  */
    var operator: Operate = Operate.ADD
    /** Result to return if inputs are invalid.  */
    var ifInvalid: Any? = null

    constructor(fields: List<String>): this(Operate.ADD, fields)

    @JsonCreator
    constructor(@JsonProperty("operator") op: Operate = Operate.ADD,
                @JsonProperty("fields") fields: List<String> = emptyList(),
                @JsonProperty("ifInvalid") ifInvalid: Any? = null) {
        this.operator = op
        this.fields = fields
        this.ifInvalid = ifInvalid
    }

    override fun invoke(map: Map<String, *>): Any? {
        val first = map.valueAtFirstPointerIn(fields) ?: return ifInvalid
        val inputs = fields.map { map.atPointer(it) }.toNullableNumberList()
        val res = operator.invoke(inputs, ifInvalid)

        // if the first argument is a date/time type, e.g. Date, and the operator is compatible, return value as that type
        val dateTimeOp = operator.returnsDates && first::class.java.timeType() && first !is Number
        return when {
            dateTimeOp -> ((res as? Number)?.toLong() ?: res)?.toCachedDateTime(first::class.java)
            else -> res
        }
    }
}

//region UTILITY FUNCTIONS

/**
 * Lookup value of the first field within the map, using pointer notation. Returns null if fields is empty or lookup
 * result is null or cannot be found.
 */
private fun Map<String, *>.valueAtFirstPointerIn(fields: List<String>) =
        if (fields.isEmpty()) null else atPointer(fields[0])

/** Converts elements of list to numbers wherever possible, leaving null values if not. */
internal fun <E> List<E>.toNullableNumberList() = map { it?.toNumberOrNull() }

//endregion

/**
 * Operations that can be performed on numeric inputs. May handle dates for operations that make sense (e.g. min/max).
 */
enum class Operate(internal val returnsDates: Boolean) : (List<Number?>?, Any?) -> Any? {
    /** First value equal to all others */
    EQUAL(false) {
        override fun invoke(inputs: List<Number?>?, def: Any?): Any? = when {
            inputs.isNullOrEmpty() || inputs[0] == null -> def
            else -> tryInvoke(def) { inputs.drop(1).all { Equal(inputs[0]).invoke(inputs[1]) } }
        }
    },

    /** First value not equal to any other */
    NOT_EQUAL(false) {
        override fun invoke(inputs: List<Number?>?, def: Any?): Any? = when {
            inputs.isNullOrEmpty() || inputs[0] == null -> def
            else -> tryInvoke(def) { inputs.drop(1).all { NotEqual(inputs[0]).invoke(inputs[1]) } }
        }
    },

    /** First value greater than others */
    GT(false) {
        override fun invoke(inputs: List<Number?>?, def: Any?): Any? = when {
            inputs.isNullOrEmpty() || inputs[0] == null -> def
            else -> tryInvoke(def) { inputs.drop(1).all { Gt(it).invoke(inputs[0]) } }
        }
    },

    /** First value greater than or equal to others */
    GTE(false) {
        override fun invoke(inputs: List<Number?>?, def: Any?): Any? = when {
            inputs.isNullOrEmpty() || inputs[0] == null -> def
            else -> tryInvoke(def) { inputs.drop(1).all { Gte(it).invoke(inputs[0]) } }
        }
    },

    /** First value less than others */
    LT(false) {
        override fun invoke(inputs: List<Number?>?, def: Any?): Any? = when {
            inputs.isNullOrEmpty() || inputs[0] == null -> def
            else -> tryInvoke(def) { inputs.drop(1).all { Lt(it).invoke(inputs[0]) } }
        }
    },

    /** First value less than or equal to others */
    LTE(false) {
        override fun invoke(inputs: List<Number?>?, def: Any?): Any? = when {
            inputs.isNullOrEmpty() || inputs[0] == null -> def
            else -> tryInvoke(def) { inputs.drop(1).all { Lte(it).invoke(inputs[0]) } }
        }
    },

    /** Negates a single value  */
    NEGATE(false) {
        override fun invoke(inputs: List<Number?>?, def: Any?): Any? = when {
            inputs.isNullOrEmpty() || inputs[0] == null -> def
            else -> tryInvoke(def) { -inputs[0]!! }
        }
    },

    /** Divides two values  */
    DIVIDE(false) {
        override fun invoke(inputs: List<Number?>?, def: Any?): Any? = when {
            inputs.isNullOrEmpty() || inputs[0] == null || inputs[1] == null -> def
            else -> tryInvoke(def) { divide(inputs[0]!!, inputs[1]!!) }
        }

        @Throws(ClassCastException::class, ArithmeticException::class)
        private fun divide(n0: Number, n1: Number) : Number = when (n0) {
            is Int -> n0.toInt() / n1.toInt()
            is Long -> n0.toLong() / n1.toLong()
            else -> n0.toDouble() / n1.toDouble()
        }
    },

    /** Divides two values  */
    MULTIPLY(false) {
        override fun invoke(inputs: List<Number?>?, def: Any?) : Any? = when {
            inputs.isNullOrEmpty() || inputs[0] == null || inputs[1] == null -> def
            else -> tryInvoke(def) { inputs[0]!! * inputs[1]!! }
        }
    },

    /** Subtracts all subsequent values from the first   */
    SUBTRACT(false) {
        override fun invoke(inputs: List<Number?>?, def: Any?): Any? = when {
            inputs.isNullOrEmpty() || inputs.contains(null) -> def
            else -> tryInvoke(def) { subtract(inputs) }
        }

        @Throws(ClassCastException::class, ArithmeticException::class)
        private fun subtract(inputs: List<Number?>): Any = when (val n0 = inputs[0]!!) {
            is Int -> n0.toInt() - inputs.drop(1).sumBy { it!!.toInt() }
            is Long -> n0.toLong() - inputs.drop(1).map { it!!.toLong() }.sum()
            else -> n0.toDouble() - inputs.drop(1).sumByDouble { it!!.toDouble() }
        }
    },

    /** Adds all values */
    ADD(false) {
        override fun invoke(inputs: List<Number?>?, def: Any?): Any? = when {
            inputs.isNullOrEmpty() || inputs.contains(null) -> def
            else -> tryInvoke(def) { add(inputs) }
        }

        @Throws(ClassCastException::class, ArithmeticException::class)
        private fun add(inputs: List<Number?>): Any = when (inputs[0]!!) {
            is Int -> inputs.sumBy { it!!.toInt() }
            is Long -> inputs.map { it!!.toLong() }.sum()
            else -> inputs.sumByDouble { it!!.toDouble() }
        }
    },

    /** Computes min of values   */
    MIN(true) {
        override fun invoke(inputs: List<Number?>?, def: Any?): Any? = when {
            inputs.isNullOrEmpty() || inputs.contains(null) -> def
            else -> tryInvoke(def) { min(inputs) }
        }

        @Throws(ClassCastException::class, ArithmeticException::class)
        private fun min(inputs: List<Number?>): Any = when (inputs[0]!!) {
            is Int -> inputs.map { it!!.toInt() }.minOrNull() as Int
            is Long -> inputs.map { it!!.toLong() }.minOrNull() as Long
            else -> inputs.map { it!!.toDouble() }.minOrNull() as Double
        }
    },

    /** Computes max of values   */
    MAX(true) {
        override fun invoke(inputs: List<Number?>?, def: Any?): Any? = when {
            inputs.isNullOrEmpty() || inputs.contains(null) -> def
            else -> tryInvoke(def) { max(inputs) }
        }

        @Throws(ClassCastException::class, ArithmeticException::class)
        private fun max(inputs: List<Number?>): Any = when (inputs[0]!!) {
            is Int -> inputs.map { it!!.toInt() }.maxOrNull() as Int
            is Long -> inputs.map { it!!.toLong() }.maxOrNull() as Long
            else -> inputs.map { it!!.toDouble() }.maxOrNull() as Double
        }
    },

    /** Computes average of values   */
    AVERAGE(true) {
        override fun invoke(inputs: List<Number?>?, def: Any?): Any? = when {
            inputs.isNullOrEmpty() || inputs.contains(null) -> def
            else -> tryInvoke(def) { average(inputs) }
        }

        @Throws(ClassCastException::class, ArithmeticException::class)
        private fun average(inputs: List<Number?>): Any = when (inputs[0]!!) {
            is Int -> inputs.map { it!!.toInt() }.average()
            is Long -> inputs.map { it!!.toLong() }.average()
            else -> inputs.map { it!!.toDouble() }.average()
        }
    },

    /** Computes average of values   */
    STD_DEV(true) {
        override fun invoke(inputs: List<Number?>?, def: Any?): Any? = when {
            inputs.isNullOrEmpty() || inputs.contains(null) -> def
            else -> tryInvoke(def) { stdDev(inputs) }
        }

        @Throws(ClassCastException::class, ArithmeticException::class)
        private fun stdDev(inputs: List<Number?>): Double {
            val numbers = inputs.filterNotNull()
            val count = numbers.size
            return when (count) {
                0 -> Double.NaN
                1 -> 0.0
                else -> {
                    val mean = numbers.sumByDouble { it.toDouble() } / count
                    val sumSq = numbers.sumByDouble { (it.toDouble() - mean).pow(2.0) }
                    sqrt(sumSq / count)
                }
            }
        }
    };
}

//region HELPER FUNCTIONS

/** Negate number, type-friendly. Returns the same type that was input. */
private operator fun Number.unaryMinus(): Number = when (this) {
    is Long -> -this
    is Int -> -this
    is Short -> -this
    is Byte -> -this
    is Float -> -this
    is Double -> -this
    else -> throw UnsupportedOperationException("Cannot negate $this")
}

/** Multiply number, type-friendly. Keeps the type of this. */
private operator fun Number.times(other: Number): Number = when (this) {
    is Long -> this * other.toLong()
    is Int ->  this * other.toInt()
    is Short -> this * other.toShort()
    is Byte -> this * other.toByte()
    is Float -> this * other.toFloat()
    is Double -> this * other.toDouble()
    else -> throw UnsupportedOperationException("Cannot negate $this")
}

/** Divide number, type-friendly. Keeps the type of this. */
private operator fun Number.div(other: Number): Number = when (this) {
    is Long -> this / other.toLong()
    is Int ->  this / other.toInt()
    is Short -> this / other.toShort()
    is Byte -> this / other.toByte()
    is Float -> this / other.toFloat()
    is Double -> this / other.toDouble()
    else -> throw UnsupportedOperationException("Cannot negate $this")
}

private const val ERROR_MSG = "Cast/Math error"

private fun <X> tryInvoke(def: X, param: () -> X): X {
    return try {
        param()
    } catch (x: ClassCastException) {
        fine<Operate>(ERROR_MSG, x)
        def
    } catch (x: NullPointerException) {
        fine<Operate>(ERROR_MSG, x)
        def
    } catch (x: ArithmeticException) {
        fine<Operate>(ERROR_MSG, x)
        def
    }
}

//endregion
