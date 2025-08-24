/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * DateTimeConversions.kt
 * edu.jhuapl.data:parsnip
 * %%
 * Copyright (C) 2024 - 2025 Johns Hopkins University Applied Physics Laboratory
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

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import edu.jhuapl.utilkt.core.fine
import edu.jhuapl.utilkt.core.severe
import edu.jhuapl.utilkt.core.warning
import java.time.*
import java.time.format.DateTimeParseException
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.absoluteValue

/**
 * Methods for converting to/from date/time types.
 */
private class DateTimeConversions

//region DATE/TIME TYPE CHECKING

private val TIME_TYPES: Set<Class<*>> = setOf(
        Long::class.javaPrimitiveType!!, java.lang.Long::class.java, Date::class.java, Calendar::class.java, GregorianCalendar::class.java,
        ZonedDateTime::class.java, LocalDateTime::class.java, LocalDate::class.java, LocalTime::class.java,
        Instant::class.java)

/**
 * Tests whether given type is one of the supported date/time types.
 * @return true if a date or time type.
 */
fun Class<*>.timeType() = TIME_TYPES.contains(this)

//endregion

//region OBJECT CONVERSION CACHE

/** Caches previously computed timestamp decodings.  */
private val DECODED_EPOCH_CACHE = CacheBuilder.newBuilder()
        .maximumSize(100000L)
        .build(object : CacheLoader<Any, Long>() {
            @Throws(Exception::class)
            override fun load(key: Any): Long? = key.toEpochMilli()
        })

/** Caches previously computed timestamp decodings.  */
private val DECODED_EPOCH_CACHE_GUESS_MILLI = CacheBuilder.newBuilder()
        .maximumSize(100000L)
        .build(object : CacheLoader<Any, Long>() {
            @Throws(Exception::class)
            override fun load(key: Any): Long? = key.toEpochMilli(true)
        })

/**
 * Attempts to convert an object to an epoch timestamp. This can be slow if automatically converting from a string
 * other than ISO-8601.
 * @param n input object
 * @param guessMilli if true, will attempt to guess the difference between epoch milliseconds and seconds (if relevant)
 * @return timestamp, or null if invalid
 */
fun cachedEpochFrom(n: Any?, guessMilli: Boolean = false): OptionalLong = try {
    val cache = if (guessMilli) DECODED_EPOCH_CACHE_GUESS_MILLI else DECODED_EPOCH_CACHE
    if (n == null) OptionalLong.empty() else OptionalLong.of(cache.get(n))
} catch (ex: ExecutionException) {
    severe<DateTimeConversions>("Unexpected", ex)
    OptionalLong.empty()
} catch (ex: CacheLoader.InvalidCacheLoadException) {
    fine<DateTimeConversions>("Cache may have returned a null value", ex)
    OptionalLong.empty()
}

/**
 * Attempts to convert an object to an epoch timestamp. This can be slow if automatically converting from a string
 * other than ISO-8601.
 * @param n input object
 * @param guessMilli if true, will attempt to guess the difference between epoch milliseconds and seconds (if relevant)
 * @return timestamp, or null if invalid
 */
fun cachedInstantFrom(n: Any?, guessMilli: Boolean = false): Instant? = with(cachedEpochFrom(n, guessMilli)) {
    return if (!isPresent) null else Instant.ofEpochMilli(asLong)
}

//endregion

//region CONVERSION OF ARBITRARY OBJECTS TO DATE/TIMES

/**
 * Decode as an epoch (milliseconds).
 * @param guessMilli if true, attempts to guess whether numbers are millis or seconds
 * @return decoded epoch
 * @throws IllegalArgumentException if this or target is not a time type
 */
fun Any.toEpochMilli(guessMilli: Boolean = false): Long? = toInstant(guessMilli)?.toEpochMilli()

/**
 * Convert from [LocalDate] to [Instant]. Uses the default time zone, and the start of the day.
 */
fun LocalDate.toInstant(): Instant = atStartOfDay(ZoneId.systemDefault()).toInstant()

/**
 * Convert from [LocalTime] to [Instant]. Uses the default time zone, and the current date.
 */
fun LocalTime.toInstant(): Instant = atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant()

/**
 * Convert from [LocalDateTime] to [Instant]. Uses the default time zone.
 */
fun LocalDateTime.toInstant(): Instant = atZone(ZoneId.systemDefault()).toInstant()

/**
 * Convert from src type to [Instant]. Uses [ZoneId.systemDefault] for the time zone where one is required but unavailable.
 * If called on a numeric type, uses [Instant.ofEpochMilli]. If called on a string, attempts to parse the string as a
 * date/time (which can be slow).
 * @param guessMilli if true, attempts to guess whether numbers are millis or seconds
 * @return instant, or null if unable to parse to a date/time [Instant]
 */
internal fun Any.toInstant(guessMilli: Boolean = false): Instant? = when (this) {
    is Number -> guessNumberInstant(toLong(), guessMilli)
    is Date -> toInstant()
    is Calendar -> toInstant()
    is LocalDateTime -> atZone(ZoneId.systemDefault()).toInstant()
    is ZonedDateTime -> toInstant()
    is LocalDate -> atStartOfDay(ZoneId.systemDefault()).toInstant()
    is LocalTime -> atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant()
    is Instant -> this
    is String -> toInstantOrNull()
    else -> null
}

private fun guessNumberInstant(n: Long, guessMilli: Boolean) = when {
    !guessMilli || isBestGuessMilli(n) -> Instant.ofEpochMilli(n)
    else -> Instant.ofEpochSecond(n)
}

private fun isBestGuessMilli(n: Number) = n.toLong().absoluteValue > 99999999999L

/**
 * Decode as an epoch (milliseconds).
 * @return decoded epoch, or null if unable to parse
 */
fun String.toEpochMilliOrNull(): Long? = toInstantOrNull()?.toEpochMilli()

//region DATE-TIME PARSER DEFINITIONS/CACHING LAST ONE THAT WORKED

typealias InstantParser = (String) -> Instant?

/** Parses strings using [Instant#parse].  */
private val PARSE_INSTANT: InstantParser = firstParserThatWorks({ Instant.parse(it) })

/** Parses strings to [ZonedDateTime] using one of the [DATE_AND_TIME_FORMATS]. */
private val DATE_TIME_PARSERS = DATE_AND_TIME_FORMATS.map { f -> firstParserThatWorks(
    { ZonedDateTime.parse(it, formatter(f)).toInstant() },
    { LocalDateTime.parse(it, formatter(f)).atZone(ZoneId.systemDefault()).toInstant() }
) }

/** Parses strings to [LocalDate] using one of the [DATE_ONLY_FORMATS]. */
private val DATE_PARSERS = DATE_ONLY_FORMATS.map { f -> firstParserThatWorks({
    LocalDate.parse(it, formatter(f)).toInstant()
}) }

/** Parses strings to [LocalTime] using one of the [TIME_ONLY_FORMATS]. */
private val TIME_PARSERS = TIME_ONLY_FORMATS.map { f -> firstParserThatWorks({
    LocalTime.parse(it, formatter(f)).toInstant()
}) }

/** All available parsers, in order of preference. */
private val PARSERS = listOf(PARSE_INSTANT) + DATE_TIME_PARSERS + DATE_PARSERS + TIME_PARSERS
/** Caching the last working parser to speed up repeated calls with the same format. */
private var LAST_WORKING_PARSER: InstantParser? = null
/** Internal count of number of parse attempts. */
private val COUNT = AtomicInteger()

// try each parser, returning value of first that succeeds, else null
private fun firstParserThatWorks(vararg parsers: InstantParser): InstantParser = { str ->
    parsers.asSequence().mapNotNull {
        try {
            it(str)
        } catch (x: DateTimeParseException) {
            // ignore and try next
            null
        }
    }.firstOrNull()
}

/**
 * Get the first working parser, returning the parser in the first argument and the decoded value in the second. If no
 * parsers work, both values will be null. If the last one that worked still works, prefer that.
 */
private fun String.findWorkingParser(): Pair<InstantParser?, Instant?> {
    LAST_WORKING_PARSER?.invoke(this)?.let {
        // still works, so return invoked value
        return LAST_WORKING_PARSER to it
    }
    COUNT.incrementAndGet()
    PARSERS.filter { it != LAST_WORKING_PARSER }.forEach {
        val x = it.invoke(this)
        if (x != null) {
            return it to x
        }
    }
    return null to null
}

//endregion

/**
 * Decode as an [Instant]. First attempts to use [Instant]s parse operation, then checks for additional options in
 * [DateTimeFormats].
 * @return decoded instant, or null if unable to parse
 */
fun String.toInstantOrNull(): Instant? {
    val (parser, instant) = findWorkingParser()
    if (parser != null) {
        LAST_WORKING_PARSER = parser
    }
    return instant
}

/**
 * Decode as an [LocalDate], using a few different options for parsing.
 * @return decoded date, or null if unable to parse
 */
fun String.toLocalDateOrNull(): LocalDate? =
    (DATE_ONLY_FORMATS + DATE_AND_TIME_FORMATS).firstParsedValueOrNull { LocalDate.parse(this, formatter(it)) }
        ?: PARSE_INSTANT(this)?.toDateTime(LocalDate::class.java)

/**
 * Decode as an [LocalTime], using a few different options for parsing.
 * @return decoded time, or null if unable to parse
 */
fun String.toLocalTimeOrNull(): LocalTime? =
    (TIME_ONLY_FORMATS + DATE_AND_TIME_FORMATS).firstParsedValueOrNull { LocalTime.parse(this, formatter(it)) }
        ?: PARSE_INSTANT(this)?.toDateTime(LocalTime::class.java)

//endregion

//region CONVERSION TO TARGET DATE/TIME TYPES

/**
 * Converts a date/time object from one type to another. Uses [ZoneId.systemDefault] for the time zone where one is required but unavailable.
 * If called on a numeric type, uses [Instant.ofEpochMilli].
 * @param <X> target type
 * @param target target type
 * @param guessMilli if true and this is a number, attempt to guess between milliseconds/seconds
 * @return true if a date or time type.
 * @throws IllegalArgumentException if this or target is not a time type
 */
fun <X> Any.toCachedDateTime(target: Class<X>, guessMilli: Boolean = false): X? {
    require(TIME_TYPES.contains(target))
    return cachedInstantFrom(this, guessMilli)?.toDateTime(target)
}

/**
 * Convert [Instant] to target date/time type. Uses [ZoneId.systemDefault] for the time zone where one is required but unavailable.
 * @throws IllegalArgumentException if target is not a valid date/time type
 */
fun <X> Instant.toDateTime(target: Class<X>): X = when (target) {
    Long::class.java -> toEpochMilli()
    Date::class.java -> Date.from(this)
    Calendar::class.java -> GregorianCalendar.from(ZonedDateTime.ofInstant(this, ZoneId.systemDefault()))
    LocalDateTime::class.java -> LocalDateTime.ofInstant(this, ZoneId.systemDefault())
    ZonedDateTime::class.java -> ZonedDateTime.ofInstant(this, ZoneId.systemDefault())
    LocalDate::class.java -> LocalDateTime.ofInstant(this, ZoneId.systemDefault()).toLocalDate()
    LocalTime::class.java -> LocalDateTime.ofInstant(this, ZoneId.systemDefault()).toLocalTime()
    Instant::class.java -> this
    else -> throw IllegalArgumentException()
} as X

//endregion
