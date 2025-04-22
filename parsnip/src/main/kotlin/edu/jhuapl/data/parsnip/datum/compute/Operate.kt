package edu.jhuapl.data.parsnip.datum.compute

import edu.jhuapl.data.parsnip.value.filter.*
import edu.jhuapl.utilkt.core.fine
import kotlin.math.pow
import kotlin.math.sqrt

/** Marker function for operations that can be performed on numeric inputs. */
interface NumberListOp {
    operator fun invoke(inputs: List<Number?>?, def: Any?): Any?
}

/**
 * Operations that can be performed on numeric inputs. May handle dates for operations that make sense (e.g. min/max).
 */
enum class Operate(internal val returnsDates: Boolean) : NumberListOp {
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

/*-
 * #%L
 * parsnip-1.0.0-SNAPSHOT
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
