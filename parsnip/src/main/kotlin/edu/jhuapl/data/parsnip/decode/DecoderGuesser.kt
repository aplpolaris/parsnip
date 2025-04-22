package edu.jhuapl.data.parsnip.decode

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * DecoderGuesser.kt
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

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import edu.jhuapl.data.parsnip.decode.StandardDecoders.*
import edu.jhuapl.util.classifier.Classifier
import edu.jhuapl.util.classifier.bestGuess
import edu.jhuapl.util.types.IPV4_PATTERN
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.sqrt

// can be expensive, so leave as flag
private const val scoreHex = false

/** Caches previously computed timestamp decodings.  */
private val LIKELIHOOD_CACHE = CacheBuilder.newBuilder()
        .maximumSize(100000L)
        .expireAfterWrite(600, TimeUnit.SECONDS)
        .build(object : CacheLoader<String, TypeLikelihoods>() {
            @Throws(Exception::class)
            override fun load(key: String): TypeLikelihoods = TypeLikelihoods(key)
        })

/**
 * Classifies a string as represented by one of several standard decoder types.
 */
object DecoderGuesser : Classifier<String?, StandardDecoders>(values()) {
    override fun score(value: String?, decoder: StandardDecoders) = when {
        value == null && decoder == NULL -> 1f
        value == null -> 0f
        else -> DecoderTypeLikelihoodGuesser.score(LIKELIHOOD_CACHE[value], decoder)
    }
}

/**
 * Classifies a string as represented by one of several standard decoder types.
 */
private object DecoderTypeLikelihoodGuesser : Classifier<TypeLikelihoods, StandardDecoders>(values()) {
    override fun score(value: TypeLikelihoods, decoder: StandardDecoders): Float {
        with (value) {
            return when (decoder) {
                NULL -> if (cNull) 1f else 0f
                STRING -> when {
                    cInt || cFloat || cBoolS || cBoolN || cIP -> .25f
                    cDate || cTime || cDomain || likelyList -> .5f
                    hex -> .5f + .5f / sqrt(length.toDouble()).toFloat()
                    cNull || cNonAlpha -> .75f
                    else -> 1f
                }
                BOOLEAN -> if (cBoolS) 1f else if (cBoolN) .7f else 0f
                INTEGER -> if (cInt) 1f else if (cIntF) .8f else 0f
                LONG, SHORT, BYTE -> if (cInt) .8f else if (cIntF) .4f else 0f
                DOUBLE, FLOAT -> if (cInt) .9f else if (cIntF) .95f else if (cFloat) 1f else 0f
                DATE -> if (cDate) (if (cTime) .7f else .9f) else 0f
                TIME -> if (cTime) (if (cDate) .7f else .9f) else 0f
                DATE_TIME -> if (cTime && cDate) .9f else if (cTime || cDate) .5f else 0f
                EPOCH -> if (cTime && cDate) .85f else if (cTime || cDate) .45f else 0f
                IP_ADDRESS -> if (cIP) 1f else 0f
                DOMAIN_NAME -> if (cDomain) 1f else 0f
                LIST -> if (likelyList) .9f else 0f
                HEX_STRING -> if (hex && scoreHex) 1 - 1 / length.toFloat() else 0f
            }
        }
    }
}

//region UTILITIES

/**
 * Compute the best decoder for the given set of sample data, obtained by guessing the best decoder for each value separately
 * and combining the results. Returns null only if the input set of samples is empty.
 */
fun bestDecoder(samples: Iterable<Any?>, guessTypes: Boolean = true): Decoder<*>? =
        samples.groupBy { bestDecoder(it, guessTypes) }.maxByOrNull { it.value.size }?.key

/**
 * Get the best decoder for the given value. If [guessTypes] is true, attempts to guess the type based on an input string
 * pattern. If [guessTypes] is false, uses only the object type to determine the best decoder.
 */
private fun bestDecoder(o: Any?, guessTypes: Boolean = true): Decoder<*> = when {
    o == null -> NULL
    o is String && guessTypes -> DecoderGuesser.bestGuess(o)
    else -> of(o::class.java) ?: STRING
}

//endregion

//region SIMPLE TESTS

private data class TypeLikelihoods(val x: String) {
    val trimmed = x.javaTrim()
    val length = trimmed.length
    val cNull = mightBeNull(trimmed)
    val cInt = trimmed.toIntOrNull() != null
    val cIntF = isFloatNearInteger(trimmed)
    val cFloat = trimmed.toDoubleOrNull() != null
    val cBoolS = isBoolean(trimmed)
    val cBoolN = isBooleanNumber(trimmed)
    val cIP = looksLikeIp(trimmed)
    val cDomain = looksLikeDomain(trimmed)
    val cDate = mightHaveDate(trimmed)
    val cTime = mightHaveTime(trimmed)
    val cNonAlpha = hasNoLetters(trimmed)
    val likelyList = likelyList(trimmed)
    val hex = scoreHex && isHexDigits(trimmed)
}

private val TIME_PATTERN = "\\d+:\\d+(:\\d)?(\\.\\d+)?".toRegex().toPattern()
private val DATE_PATTERN = "(\\d+[/\\-.]\\d+|\\d+[/\\-.]\\d+[/\\-.]\\d+)".toRegex().toPattern()
private val NO_LETTER_PATTERN = "[^A-Za-z]*".toRegex().toPattern()
private val HEX_LETTER_PATTERN = "[A-Fa-f0-9]+".toRegex().toPattern()
private val DOMAIN_PATTERN = "[\\w\\-]+(\\.[\\w\\-]+)+".toRegex().toPattern()

fun mightBeNull(s: String?) = s.isNullOrEmpty() || arrayOf("null", "n/a", "none").any { it.equals(s, ignoreCase = true) }
fun isFloatNearInteger(s: String): Boolean = with(s.toDoubleOrNull()) { this != null && abs(Math.round(this) - this) < 1E-8 }
private fun isBooleanNumber(s: String) = "0" == s || "1" == s
private fun isBoolean(s: String): Boolean = arrayOf("true", "false").any { it.equals(s, ignoreCase = true) }
private fun hasNoLetters(s: String) = NO_LETTER_PATTERN.matcher(s).matches()
private fun isHexDigits(s: String) = HEX_LETTER_PATTERN.matcher(s).matches() && s.toIntOrNull() != null
private fun mightHaveTime(s: String) = TIME_PATTERN.matcher(s).find()
private fun mightHaveDate(s: String) = DATE_PATTERN.matcher(s).find()
private fun looksLikeIp(s: String) = IPV4_PATTERN.matcher(s).matches()
private fun looksLikeDomain(s: String) = DOMAIN_PATTERN.matcher(s).matches()

/**
 * Test if string looks like a list.
 * @param s input
 * @param sep assumed list element separator
 * @return true if comma-delimited and suitable matches between elements
 */
private fun likelyList(s: String, sep: Char = ','): Boolean {
    if (s.length < 3) {
        return false
    }
    val split = s.split(sep).map { it.javaTrim() }
    if (split.size == 1) {
        return false
    }
    val types = mutableMapOf<StandardDecoders, Int>()
    for (sub in split) {
        val best = DecoderGuesser.bestGuess(sub)
        types[best] = types.getOrDefault(best, 0) + 1
    }
    return types.size == 1 && types.keys.first() != STRING
}

private fun String.javaTrim() = this.trim { it <= ' ' }

//endregion
