package edu.jhuapl.data.parsnip.decode

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * InstantDecoder.kt
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import edu.jhuapl.util.types.toDateTime
import edu.jhuapl.util.types.toCachedDateTime
import edu.jhuapl.util.types.toInstant
import java.text.DateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.util.*

/**
 * Uses a [DateFormat] to convert a string to an [Instant]. When using formats that do not support a time zone or offset,
 * assumes the system default time zone.
 * @author petereb1
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class InstantDecoder(pattern: String = "yyyy-MM-dd'T'HH:mm:ss") : Decoder<Instant> {

    private var formatter: DateTimeFormatter = DateTimeFormatter.ofPattern(pattern)
    var pattern: String = pattern
        set(pattern) {
            field = pattern
            this.formatter = DateTimeFormatter.ofPattern(pattern)
        }

    override val javaType = Instant::class.java

    override fun decode(input: String): Instant {
        try {
            val acc = formatter.parse(input)
            return when {
                acc.isSupported(ChronoField.NANO_OF_SECOND) && acc.isSupported(ChronoField.INSTANT_SECONDS) -> Instant.from(acc)
                acc.isSupported(ChronoField.HOUR_OF_DAY) && acc.isSupported(ChronoField.DAY_OF_MONTH) -> {
                    val dt = LocalDateTime.from(acc)
                    return if (pattern.contains('Z')) dt.atZone(ZoneId.of("Z")).toInstant() else dt.toInstant()!!
                }
                acc.isSupported(ChronoField.DAY_OF_MONTH) -> LocalDate.from(acc).toInstant()!!
                acc.isSupported(ChronoField.HOUR_OF_DAY) -> LocalTime.from(acc).toInstant()!!
                else -> throw DecoderException("$input not convertible to local date/time or Instant")
            }
        } catch (x: DateTimeException) {
            throw DecoderException(x)
        }
    }

    fun format(obj: Date) = formatter.format(obj.toCachedDateTime(ZonedDateTime::class.java))!!
    fun format(obj: LocalDateTime) = formatter.format(obj.toCachedDateTime(ZonedDateTime::class.java))!!
    fun format(obj: Instant) = formatter.format(obj.toDateTime(ZonedDateTime::class.java))!!

}
