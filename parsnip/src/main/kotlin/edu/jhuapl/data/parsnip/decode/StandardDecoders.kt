/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * StandardDecoders.kt
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
package edu.jhuapl.data.parsnip.decode

import edu.jhuapl.util.types.*
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/**
 * Set of standard decoders for common data types.
 */
enum class StandardDecoders(decoderPair: DecoderPair<*>, val timeType: Boolean = false) : Decoder<Any> {
    NULL(Nothing::class.java decodedBy { null }),
    STRING(String::class.java decodedBy { it }),
    BOOLEAN(Boolean::class.java decodedBy { it.javaTrim().toBoolean() }),
    LONG(Long::class.java decodedBy { it.javaTrim().toLongOrNull() }),
    INTEGER(Int::class.java decodedBy { it.javaTrim().toIntOrNull() }),
    SHORT(Short::class.java decodedBy { it.javaTrim().toShortOrNull() }),
    BYTE(Byte::class.java decodedBy { it.javaTrim().toByteOrNull() }),
    DOUBLE(Double::class.java decodedBy { it.javaTrim().toDoubleOrNull() }),
    FLOAT(Float::class.java decodedBy { it.javaTrim().toFloatOrNull() }),
    IP_ADDRESS(String::class.java decodedBy { it.toIpAddressOrNull() }),
    DOMAIN_NAME(String::class.java decodedBy { it.toDomainNameOrNull() }),
    HEX_STRING(String::class.java decodedBy { it.toHexStringOrNull() }),
    LIST(List::class.java decodedBy { it.toList() }),
    DATE_TIME(Instant::class.java decodedBy { it.toInstantOrNull() }, timeType = true),
    DATE(LocalDate::class.java decodedBy { it.toLocalDateOrNull() }, timeType = true),
    TIME(LocalTime::class.java decodedBy { it.toLocalTimeOrNull() }, timeType = true),
    EPOCH(Long::class.java decodedBy { it.toEpochMilliOrNull() }, timeType = true);

    override val javaType = decoderPair.javaType
    val decoder = decoderPair.decoder

    override fun decode(input: String): Any = decoder(input) ?: throw DecoderException("Converted value to null: $input")
}

fun of(type: Class<*>) = StandardDecoders.values().find { it.javaType == type || it.javaType.isPrimitiveWithWrapperType(type) }

/** This class ensures compatibility of the Java type and the decoder return type. */
private class DecoderPair<X>(val javaType: Class<out X>, val decoder: (String) -> X?)

private infix fun <X> Class<out X>.decodedBy(decoder: (String) -> X?) = DecoderPair(this, decoder)

private fun String.toIpAddressOrNull(): String? = javaTrim()
private fun String.toDomainNameOrNull(): String? = javaTrim()
private fun String.toHexStringOrNull(): String? = with(javaTrim()) { if (startsWith("0x")) this else "0x$this" }
private fun String.toList(sep: Char = ','): List<String> = javaTrim().split(sep).map { it.javaTrim() }

private fun String.javaTrim() = this.trim { it <= ' ' }
