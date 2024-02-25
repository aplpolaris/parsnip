package edu.jhuapl.util.types

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * DateTimeFormats.kt
 * edu.jhuapl.data:parsnip
 * %%
 * Copyright (C) 2024 Johns Hopkins University Applied Physics Laboratory
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

import edu.jhuapl.utilkt.core.fine
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Provides various [DateTimeFormatter] instances and utilities for converting strings to date/time types.
 * @author petereb1
 */
private class DateTimeFormats

private const val MSG_PARSE_FAILED = "Parse failed"

//region FORMAT DEFINITIONS

// 6/1/2012 4:01:36 AM
private val DATE_ONLY_FORMATS = listOf("M/d/yy", "M/d/yyyy", "yyyy/M/d", "M-d-yy", "M-d-yyyy", "yyyy-M-d", "MMMyyyy", "MMM d, yyyy")
private val DATE_TIME_SEP = listOf(" ", "'T'")
private val TIME_ONLY_FORMATS = listOf("h:mm:ss a", "H:mm:ss.SSS", "H:mm:ss", "H:mm")
private val DATE_AND_TIME_FORMATS = DATE_ONLY_FORMATS * DATE_TIME_SEP * TIME_ONLY_FORMATS
private val FORMATTER_CACHE: MutableMap<String, DateTimeFormatter> = mutableMapOf()

val TIME_FORMATS = TIME_ONLY_FORMATS + DATE_AND_TIME_FORMATS
val DATE_FORMATS = DATE_ONLY_FORMATS + DATE_AND_TIME_FORMATS
val DATE_TIME_FORMATS = DATE_AND_TIME_FORMATS + DATE_ONLY_FORMATS + TIME_ONLY_FORMATS

/** Lookup formatter by string, using cache, constructing formatter and adding it if not present. */
internal fun formatter(f: String) = FORMATTER_CACHE.getOrPut(f) { DateTimeFormatter.ofPattern(f) }

/** Given two arrays, creates a "product" formed by concatenating each pair in the two arrays. */
private operator fun Iterable<String>.times(array: Iterable<String>) = flatMap { first -> array.map { second -> first + second } }

//endregion

//region UTILITIES FOR GETTING BEST FORMAT FOR PARSING A SET OF STRINGS

/**
 * Compute and return the best date format from a given collection of date/time strings.
 * @param defFormat default format to return
 * @return best format
 */
fun Collection<String>.bestFormatForParsing(): String? {
    val counts = groupingBy { bestFormatForParsing(it) }.eachCount()
    return counts.minByOrNull { it.value }?.key
}

/**
 * Get classifier for date/time format. Returns the best possible format for a given input.
 * @return date/time classifier
 */
internal fun bestFormatForParsing(input: String): String = DATE_TIME_FORMATS.minByOrNull { DateTimeFormatRank(input, it) }!!

/** Parse using first working format in supplied list, returning empty if none applies. */
internal fun <X> Iterable<String>.firstParsedValueOrNull(parseWithFormat: (String) -> X): X? {
    return tryForEach(null) { parseWithFormat(it) }
}

/** Runs for each block in try/catch for [DateTimeParseException]. Returns first value that runs successfully, or the default if none. */
internal fun <X> Iterable<String>.tryForEach(def: X, run: (String) -> X): X {
    forEach {
        try {
            return run(it)
        } catch (x: DateTimeParseException) {
            fine<DateTimeFormats>(MSG_PARSE_FAILED, x)
        }
    }
    return def
}

/** Encapsulates information needed to rank formats.  */
internal class DateTimeFormatRank(input: String, private val formatString: String) : Comparable<DateTimeFormatRank> {
    private val formatter: DateTimeFormatter = formatter(formatString)
    private var parses: Boolean = false
    private var inFarFuture: Boolean = false
    private var inDistantPast: Boolean = false
    private var patternLength: Int = formatString.length

    init {
        try {
            val d = LocalDateTime.parse(input, formatter)
            val now = LocalDateTime.now()
            parses = true
            inFarFuture = d.year - now.year > 60
            inDistantPast = now.year - d.year > 60
        } catch (x: DateTimeParseException) {
            // leave defaults
        }
    }

    override fun compareTo(other: DateTimeFormatRank): Int {
        val defComparable = { formatString.compareTo(other.formatString) }
        return preferTrue(parses, other.parses, defComparable)
                ?: preferTrue(!inFarFuture, !other.inFarFuture, defComparable)
                ?: preferTrue(!inDistantPast, !other.inDistantPast, defComparable)
                ?: preferLonger(formatString, other.formatString)
                ?: defComparable()
    }

    /** If both values are true, returns null; if both false, returns default; else makes the true flag smaller. */
    private fun preferTrue(flag1: Boolean, flag2: Boolean, def: () -> Int) : Int? = when {
        flag1 && !flag2 -> -1
        !flag1 && flag2 -> 1
        !flag1 && !flag2 -> def()
        else -> null
    }

    /** Prefers the longer string, returns null if the same length. */
    private fun preferLonger(s: String, t: String): Int? = when {
        s.length > t.length -> -1
        s.length < t.length -> 1
        else -> null
    }
}

//endregion
