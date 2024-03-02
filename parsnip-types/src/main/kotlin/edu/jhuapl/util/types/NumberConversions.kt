/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * NumberConversions.kt
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
package edu.jhuapl.util.types

import edu.jhuapl.utilkt.core.fine
import edu.jhuapl.utilkt.core.javaTrim
import java.lang.NumberFormatException
import java.text.NumberFormat
import java.text.ParseException
import java.util.*

/**
 * Methods for converting objects, strings, date/times, and numbers to number types. Target number types may or may not
 * be specified. If unspecified may return an [Int], [Long], or [Double]. Methods come in two varieties: those that
 * throw [IllegalArgumentException] when unable to convert to target type, and those that return null.
 */
private class NumberConversions

private val NUMBER_TYPES = setOf(Long::class.java, Int::class.java, Short::class.java, Byte::class.java, Double::class.java, Float::class.java)

/**
 * Test whether given type is a Number type. Returns true for boxed on unboxed number types.
 * @return true if assignable to a Number
 */
fun Class<*>.numberType() = Number::class.java.isAssignableFrom(this)
        || NUMBER_TYPES.contains(this)

//region STRINGS/OBJECTS TO NUMBERS WITHOUT TARGET TYPE HINTS

/**
 * Converts nullable object input to number. Nulls are converted to nulls, numbers are left as is, and date/time types
 * are converted to longs (epoch milliseconds). Attempts to decode other types to either Integer, Long, or Double if
 * they can be parsed.
 * @return [Number] output or null
 */
fun Any?.toNumberOrNull(): Number? = try {
    when {
        this == null -> null
        this is Number -> this
        this::class.java.timeType() -> toCachedDateTime(Long::class.java)
        else -> toString().toNumberOrNull()
    }
} catch (x: IllegalArgumentException) {
    null
}

/** Decodes value as either int, long, or double, if possible. Returns null otherwise. */
fun String.toNumberOrNull(): Number? = with(javaTrim()) {
    toIntOrNull() ?: toLongOrNull() ?: toDoubleOrNull() ?: lookupNumber() ?: toNumberWithPercent() ?: toNumberWithCommas()
}

private fun String.lookupNumber(): Number? = when (this) {
    "-" -> 0.0
    else -> null
}

private fun String.toNumberWithPercent(): Number? = try {
    NumberFormat.getPercentInstance(Locale.US).parse(this)
} catch (x: ParseException) {
    // ignore this attempt
    null
}

private fun String.toNumberWithCommas(): Number? = try {
    NumberFormat.getNumberInstance(Locale.US).parse(this)
} catch (x: ParseException) {
    // ignore this attempt
    null
}

//endregion

//region STRINGS/OBJECTS TO NUMBERS, WITH TARGET TYPES

/**
 * Converts nullable object input to number. Nulls are converted to nulls, numbers are converted to the target type, and
 * date/time types are converted to longs (epoch milliseconds). Attempts to decode other types to either Integer, Long,
 * or Double if they can be parsed.
 * @return [Number] output or null
 */
fun <N : Number> Any?.toNumberOrNull(target: Class<N>): N? = try {
    when {
        this == null -> null
        this is Number -> this.toNumber(target)
        this::class.java.timeType() -> toCachedDateTime(Long::class.java)?.toNumberOrNull(target)
        else -> toString().toNumberOrNull(target)
    }
} catch (x : IllegalArgumentException) {
    null
}

/**
 * Convert number from a string, throwing exception if invalid.
 * @param <N> target type
 * @return converted value, or null if unable to parse
 * @throws NumberFormatException if not parsable
 * @throws IllegalStateException for nonstandard target types
 */
@Suppress("IMPLICIT_CAST_TO_ANY")
inline fun <reified N : Number> String.toNumber() = toNumber(N::class.java)

/**
 * Convert number from a string, throwing exception if invalid.
 * @param <N> target type
 * @param target target type for conversion
 * @return converted value, or null if unable to parse
 * @throws NumberFormatException if not parsable
 * @throws IllegalStateException for nonstandard target types
 */
@Suppress("IMPLICIT_CAST_TO_ANY")
fun <N : Number> String.toNumber(target: Class<N>): N {
    try {
        return toNumberBasic(target)
    } catch (x: NumberFormatException) {
        // ignore this attempt and try with other number formats
    }
    try {
        return NumberFormat.getNumberInstance(Locale.US).parse(this)!!.toNumber(target)!!
    } catch (x: ParseException) {
        throw NumberFormatException(this)
    }
}

private fun <N : Number> String.toNumberBasic(target: Class<N>): N = with (javaTrim()) {
    when (target) {
        java.lang.Long::class.java -> toLong()
        java.lang.Integer::class.java -> toInt()
        java.lang.Short::class.java -> toShort()
        java.lang.Byte::class.java -> toByte()
        java.lang.Float::class.java -> toFloat()
        java.lang.Double::class.java -> toDouble()
        Long::class.java -> toLong()
        Int::class.java -> toInt()
        Short::class.java -> toShort()
        Byte::class.java -> toByte()
        Float::class.java -> toFloat()
        Double::class.java -> toDouble()
        else -> throw IllegalStateException("Unexpected target type: $target")
    } as N
}

/**
 * Convert number from a string, returning null if invalid.
 * @param <N> target type
 * @param target target type for conversion
 * @return converted value, or null if unable to parse or invalid target type
 */
@Suppress("IMPLICIT_CAST_TO_ANY")
fun <N : Number> String.toNumberOrNull(target: Class<N>): N? = with(javaTrim()) {
    when (target) {
        java.lang.Long::class.java -> toLongOrNull()
        java.lang.Integer::class.java -> toIntOrNull()
        java.lang.Short::class.java -> toShortOrNull()
        java.lang.Byte::class.java -> toByteOrNull()
        java.lang.Float::class.java -> toFloatOrNull()
        java.lang.Double::class.java -> toDoubleOrNull()
        Long::class.java -> toLongOrNull()
        Int::class.java -> toIntOrNull()
        Short::class.java -> toShortOrNull()
        Byte::class.java -> toByteOrNull()
        Float::class.java -> toFloatOrNull()
        Double::class.java -> toDoubleOrNull()
        else -> null
    } as N?
}

//endregion

//region NUMBER TO NUMBER CONVERSION

/**
 * Convert number from one type to another, using the class argument.
 * @param <N> target type
 * @param target target type for conversion (must be Long, Integer, Short, Byte, Float, or Double)
 * @return converted value
 * @throws IllegalStateException if not one of the given types
 */
@Suppress("IMPLICIT_CAST_TO_ANY")
fun <N : Number> Number.toNumber(target: Class<N>): N? = when (target) {
    java.lang.Long::class.java -> toLong()
    java.lang.Integer::class.java -> toInt()
    java.lang.Short::class.java -> toShort()
    java.lang.Byte::class.java -> toByte()
    java.lang.Float::class.java -> toFloat()
    java.lang.Double::class.java -> toDouble()
    Long::class.java -> toLong()
    Int::class.java -> toInt()
    Short::class.java -> toShort()
    Byte::class.java -> toByte()
    Float::class.java -> toFloat()
    Double::class.java -> toDouble()
    else -> throw IllegalStateException("Unexpected target type: $target")
} as N

//endregion
