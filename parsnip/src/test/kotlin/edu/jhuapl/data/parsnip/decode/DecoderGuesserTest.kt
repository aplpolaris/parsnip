package edu.jhuapl.data.parsnip.decode

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * DecoderGuesserTest.kt
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

import edu.jhuapl.data.parsnip.decode.StandardDecoders.*
import edu.jhuapl.testkt.shouldBe
import org.junit.Test
import java.time.*
import java.time.format.DateTimeParseException
import kotlin.test.assertFailsWith

class DecoderGuesserTest {

    @Test
    fun testBestDecoder() {
        bestDecoder(listOf(null, null, "a")) shouldBe NULL
        bestDecoder(listOf("a", "b", "c")) shouldBe STRING
        bestDecoder(listOf(1, 2, 3.0)) shouldBe INTEGER
        bestDecoder(listOf(1, 2.1, 3.0)) shouldBe DOUBLE
        bestDecoder(listOf("1/1/02", "10/1/03")) shouldBe DATE
//        bestDecoder(listOf("Jan 1, 2002", "Oct 1, 2003")) shouldBe DATE
        bestDecoder(listOf("10:00", "11:00")) shouldBe TIME
        bestDecoder(listOf("1.2.3.4", "5.6.6.7")) shouldBe IP_ADDRESS
    }

    @Test
    fun testTimestamp() {
        assertFailsWith(DateTimeParseException::class) { Instant.parse("") }

        assertTimestamp(2016, 5, 9, 13, 14, 0, 0, "5/9/16 13:14:00")
        assertTimestamp(1980, 9, 11, 0, 0, 0, 0, "Sep 11, 1980")
        assertTimestamp(2017, 10, 31, 16, 40, 10, 132000000, "2017-10-31T16:40:10.132")
        assertTimestampFails(2017, 10, 31, 16, 40, 10, 130000000, "2017-10-31T16:40:10.13")
        //todo - these next two tests need to be adapted for zulu offset (daylight savings)
        assertTimestamp(2017, 10, 31, 12, 41, 13, 196000000, "2017-10-31T16:41:13.196Z")
        assertTimestamp(2017, 10, 31, 12, 41, 13, 196471100, "2017-10-31T16:41:13.1964711Z")
    }

    @Suppress("unused")
    private fun assertTimestamp(yr: Int, mo: Int, day: Int, hr: Int, min: Int, sec: Int, nanos: Int, date: String) {
        val d = LocalDateTime.of(yr, mo, day, hr, min, sec, nanos)
        DATE_TIME.decode(date) shouldBe d.toLocalInstant()
    }

    @Suppress("unused")
    private fun assertTimestampFails(yr: Int, mo: Int, day: Int, hr: Int, min: Int, sec: Int, millis: Int, date: String) {
        assertFailsWith(DecoderException::class) { TIME.decode(date) }
    }

    private fun LocalDateTime.toLocalInstant() = atZone(ZoneId.systemDefault()).toInstant()

    @Test
    fun testDate() {
        DATE.decode("5/9/16 13:14:00") shouldBe LocalDate.of(2016, 5, 9)
        DATE.decode("Jan 1, 1980") shouldBe LocalDate.of(1980, 1, 1)
        DATE.decode("2012/01/01") shouldBe LocalDate.of(2012, 1, 1)
    }

    @Test
    fun testTime() {
        TIME.decode("5/9/16 13:14:00") shouldBe LocalTime.of(13, 14, 0)
        assertFailsWith(DecoderException::class) { TIME.decode("Sep 11, 1980") }
    }

}
