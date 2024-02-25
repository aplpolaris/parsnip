package edu.jhuapl.data.parsnip.gen

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * DimensionConstraint.kt
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

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import edu.jhuapl.util.types.convertTo
import edu.jhuapl.utilkt.core.javaTrim
import kotlin.random.Random

/**
 * Provides a constrained type/random variable for use with data applications.
 * Keeps a default value for the space.
 * Supports checking if a value is in the space.
 * Generates a random number within the space.
 */
abstract class DimensionConstraint<X>(var type: DimensionType<X>, var defaultValue: X) {
    @get:JsonProperty("type")
    val typeAsString: String
            get() = type.name

    /** Whether given value is contained in this space. */
    abstract fun contains(x: X): Boolean
    /** Generate a random value in this space. */
    abstract fun random(random: Random = Random): X

    companion object {
        /** Create constraint from string representation. */
        fun valueOf(s: String) = constraintFromString(s)
    }
}

//region STRING ENCODING

private fun constraintFromString(s: String) : DimensionConstraint<*> {
    val split = s.split("(", ";", ")").map { it.javaTrim() }
    val type = split[0]
    val initialValue = split.getOrNull(1)
    val parameters = if (s.contains(";")) split.getOrNull(2) else split.getOrNull(1)
    val parameterList = (if (parameters == null) "" else if (parameters.startsWith("[")) parameters.substring(1, parameters.length - 1) else parameters)
            .split(",").map { it.javaTrim() }

    fun String?.toSet() = (if (this == null) "" else if (startsWith("[")) substring(1, length - 1) else this)
            .split(",").map { it.javaTrim() }.toSet()

    fun stringInitialValue() = initialValue ?: ""
    fun intInitialValue() = if (initialValue == parameters) parameterList[0].toInt() else initialValue?.toInt() ?: 0
    fun doubleInitialValue() = if (initialValue == parameters) parameterList[0].toDouble() else initialValue?.toDouble() ?: 0.0
    fun setInitialValue() = initialValue.toSet()

    fun booleanParameter() = parameters.convertTo(Boolean::class.java) ?: throw IllegalArgumentException()
    fun intParameter() = parameters!!.toInt()
    fun doubleParameter() = parameters!!.toDouble()
    fun setParameter() = parameters!!.toSet()

    fun intParameterList(size: Int? = null) = parameterList.mapNotNull { it.toIntOrNull() }.also { require(it.size == size || size == null) }
    fun doubleParameterList(size: Int? = null) = parameterList.mapNotNull { it.toDoubleOrNull() }.also { require(it.size == size || size == null) }

    return when (type.toLowerCase()) {
        "true" -> BooleanConstraint(true)
        "false" -> BooleanConstraint(false)

        // for free constraint, the parameter is the initial value
        "boolean", "boolean.free" -> BooleanConstraint(booleanParameter())
        "string", "string.free" -> FreeConstraint(StringDimensionType, parameters!!)
        "int", "integer", "int.free", "integer.free" -> FreeConstraint(IntegerDimensionType, intParameter())
        "float", "double", "float.free", "double.free" -> FreeConstraint(FloatDimensionType, doubleParameter())

        "string.enum" -> EnumConstraint(StringDimensionType, parameterList, stringInitialValue())
        "int.enum", "integer.enum" -> EnumConstraint(IntegerDimensionType, intParameterList(), intInitialValue())
        "float.enum", "double.enum" -> EnumConstraint(FloatDimensionType, doubleParameterList(), doubleInitialValue())

        "int.range", "integer.range" -> FiniteIntRangeConstraint(intParameterList(2)[0], intParameterList(2)[1], intInitialValue())
        "float.range", "double.range" -> FiniteDoubleRangeConstraint(doubleParameterList(2)[0], doubleParameterList(2)[1], doubleInitialValue())
        "float.normal", "double.normal" -> NormalRangeConstraint(doubleParameterList(2)[0], doubleParameterList(2)[1], doubleInitialValue())

        "set" -> AllowedElementSetConstraint(setParameter(), setInitialValue())

        else -> throw IllegalArgumentException("Invalid dimension $type")
    }
}

private fun <X> DimensionConstraint<X>.standardConstraintString(name: String, vararg parameters: Any) = when (defaultValue) {
    null -> "$name(${parameters.joinToString(",")})"
    else -> "$name($defaultValue; ${parameters.joinToString(",")})"
}

//endregion

/** Constraint that permits all values. */
class FreeConstraint<X>(type: DimensionType<X>, defaultValue: X) : DimensionConstraint<X>(type, defaultValue) {
    @JsonCreator constructor(type: String, defaultValue: X): this(dimensionType(type) as DimensionType<X>, defaultValue)

    override fun toString() = standardConstraintString("${type.name.toLowerCase()}.free")
    override fun contains(x: X) = true
    override fun random(random: Random): X {
        throw UnsupportedOperationException("cannot get random value from free constraint")
    }
}

/** Free constraint for boolean values. */
class BooleanConstraint(defaultValue: Boolean = false) : DimensionConstraint<Boolean>(BooleanDimensionType, defaultValue) {
    override fun toString() = standardConstraintString("boolean")
    override fun contains(x: Boolean) = true
    override fun random(random: Random) = random.nextBoolean()
}

/** Constraint with a list of fixed values. */
class EnumConstraint<X>(type: DimensionType<X>, _values: List<X>, defaultValue: X) : DimensionConstraint<X>(type, defaultValue) {
    var values: List<X> = _values.toList()

    @JsonCreator constructor(type: String, values: List<X>, defaultValue: X): this(dimensionType(type) as DimensionType<X>, values, defaultValue)
    constructor(type: DimensionType<X>, defaultValue: X, vararg _values: X): this(type, listOf(*_values), defaultValue)
    constructor(type: String, defaultValue: X, vararg _values: X): this(dimensionType(type) as DimensionType<X>, listOf(*_values), defaultValue)
    constructor(type: String, vararg _values: X): this(dimensionType(type) as DimensionType<X>, listOf(*_values), _values.first())

    override fun toString() = standardConstraintString("${type.name.toLowerCase()}.enum", values)
    override fun contains(x: X) = values.contains(x)
    override fun random(random: Random) = values.random(random)
}

/** Constraint with a range of integer values. */
class FiniteIntRangeConstraint(private val range: IntRange, defaultValue: Int = range.first) : DimensionConstraint<Int>(IntegerDimensionType, defaultValue) {
    constructor(min: Int, max: Int, defaultValue: Int) : this(IntRange(min, max), defaultValue)
    constructor(min: Int, max: Int) : this(IntRange(min, max), min)

    val min
        get() = range.first
    val max
        get() = range.last

    override fun toString() = standardConstraintString("int.range", min, max)
    override fun contains(x: Int) = x in range
    override fun random(random: Random) = range.random(random)
}

/** Constraint with a range of double values. */
class FiniteDoubleRangeConstraint(private val range: ClosedFloatingPointRange<Double>, defaultValue: Double = range.start) :
        DimensionConstraint<Double>(FloatDimensionType, defaultValue) {
    constructor(min: Double, max: Double, defaultValue: Double) : this(min..max, defaultValue)
    constructor(min: Double, max: Double) : this(min..max, min)

    val min
        get() = range.start
    val max
        get() = range.endInclusive

    override fun toString() = standardConstraintString("float.range", min, max)
    override fun contains(x: Double) = x in range
    override fun random(random: Random) = random.nextDouble(range.start, range.endInclusive)
}

/** Constraint that allows all double values, but generates randoms about a normal distribution. */
class NormalRangeConstraint(val mean: Double, val dev: Double, defaultValue: Double = mean) : DimensionConstraint<Double>(FloatDimensionType, defaultValue) {
    override fun toString() = standardConstraintString("float.normal", mean, dev)
    override fun contains(x: Double) = true
    override fun random(random: Random) = random.nextGaussian(mean, dev)
}

/** Constraint with a pre-specified set of allowed elements. */
class AllowedElementSetConstraint(val fullSet: Set<Any>, defaultValue: Set<Any> = setOf()) : DimensionConstraint<Set<Any>>(SetDimensionType, defaultValue) {
    override fun toString() = standardConstraintString("set", fullSet)
    override fun contains(x: Set<Any>): Boolean = fullSet.containsAll(x)
    override fun random(random: Random): Set<Any> {
        val list = fullSet.shuffled()
        return list.subList(0, random.nextInt(fullSet.size)).toSet()
    }
}

/** Generate number from a normal (Gaussian) distribution using the Box-Muller transform. */
private fun Random.nextGaussian(mean: Double, dev: Double): Double {
    var v1: Double
    var s: Double
    do {
        v1 = nextDouble(-1.0, 1.0)
        val v2 = nextDouble(-1.0, 1.0)
        s = v1 * v1 + v2 * v2
    } while (s >= 1 || s == 0.0)
    val multiplier = StrictMath.sqrt(-2 * StrictMath.log(s) / s)
    return mean + v1 * multiplier * dev
}
