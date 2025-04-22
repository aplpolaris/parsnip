package edu.jhuapl.data.parsnip.decode

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * InstantDecoderTest.kt
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

import edu.jhuapl.testkt.shouldBe
import org.junit.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.*
import kotlin.test.assertFailsWith

class InstantDecoderTest {

    @Test
    fun testCanTransform() {
        println("canTransform")
        val inst = InstantDecoder()

        assertTimestamp(inst, 2001, 11, 9, 23, 12, 0, 0, "2001-11-09T23:12:00")

        val inst2 = InstantDecoder("yyyy-MM-dd'T'HH:mm:ss.SS")
        assertTimestampFails(inst2, 2017, 10, 31, 16, 40, 10, 132000000, "2017-10-31T16:40:10.132")
        assertTimestamp(inst2, 2017, 10, 31, 16, 40, 10, 130000000, "2017-10-31T16:40:10.13")
        assertTimestampFails(inst2, 2017, 10, 31, 16, 40, 10, 100000000, "2017-10-31T16:40:10.1")
        assertTimestampFails(inst2, 2017, 10, 31, 12, 41, 13, 196000000, "2017-10-31T16:41:13.196Z")
        assertTimestampFails(inst2, 2017, 10, 31, 12, 41, 13, 196000000, "2017-10-31T16:41:13.1964711Z")

        val inst3 = InstantDecoder("yyyy-MM-dd'T'HH:mm:ss.S")
        assertTimestampFails(inst3, 2017, 10, 31, 16, 40, 10, 132000000, "2017-10-31T16:40:10.132")
        assertTimestampFails(inst3, 2017, 10, 31, 16, 40, 10, 130000000, "2017-10-31T16:40:10.13")
        assertTimestamp(inst3, 2017, 10, 31, 16, 40, 10, 100000000, "2017-10-31T16:40:10.1")
        assertTimestampFails(inst3, 2017, 10, 31, 12, 41, 13, 196000000, "2017-10-31T16:41:13.196Z")
        assertTimestampFails(inst3, 2017, 10, 31, 12, 41, 13, 196000000, "2017-10-31T16:41:13.1964711Z")

        val inst4 = InstantDecoder("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        assertTimestampFails(inst4, 2017, 10, 31, 16, 40, 10, 132000000, "2017-10-31T16:40:10.132")
        assertTimestampFails(inst4, 2017, 10, 31, 16, 40, 10, 130000000, "2017-10-31T16:40:10.13")
        assertTimestampFails(inst4, 2017, 10, 31, 16, 40, 10, 100000000, "2017-10-31T16:40:10.1")
        assertTimestamp(inst4, 2017, 10, 31, 12, 41, 13, 196000000, "2017-10-31T16:41:13.196Z")
        assertTimestampFails(inst4, 2017, 10, 31, 12, 41, 13, 196000000, "2017-10-31T16:41:13.1964711Z")

        val inst5 = InstantDecoder("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
        assertTimestamp(inst5, 2017, 10, 31, 12, 41, 13, 196471000, "2017-10-31T16:41:13.196471Z")
        assertTimestampFails(inst5, 2017, 10, 31, 12, 41, 13, 196000000, "2017-10-31T16:41:13.196Z")

        val inst6 = InstantDecoder("EEE MMM d HH:mm:ss yyyy")
        assertTimestamp(inst6, 2018, 5, 14, 10, 0, 0, 0, "Mon May 14 10:00:00 2018")
    }

    @Test
    fun testCanTransform_Date() {
        assertTimestamp(InstantDecoder("yyyy/MM/dd"), 2012, 1, 1, 0, 0, 0, 0, "2012/01/01")
    }

    private fun assertTimestamp(dec: InstantDecoder, yr: Int, mo: Int, day: Int, hr: Int, min: Int, sec: Int, nanos: Int, date: String) {
        dec.decode(date) shouldBe LocalDateTime.of(yr, mo, day, hr, min, sec, nanos).toLocalInstant()
    }

    private fun assertTimestampFails(dec: InstantDecoder, yr: Int, mo: Int, day: Int, hr: Int, min: Int, sec: Int, nanos: Int, date: String) {
        assertFailsWith(DecoderException::class) { dec.decode(date) }
    }

    @Test
    fun testFormat() {
        val obj = Date(100, 10, 9, 8, 7, 6)
        InstantDecoder().format(obj) shouldBe "2000-11-09T08:07:06"
        InstantDecoder("MM/dd/yyyy").format(obj) shouldBe "11/09/2000"
        InstantDecoder("MM/d/yyyy H:mm:ss").format(obj) shouldBe "11/9/2000 8:07:06"
    }

    @Test
    fun testDecode() {
        assertFailsWith(DecoderException::class) { InstantDecoder().decode("") }
        assertFailsWith(DecoderException::class) { InstantDecoder().decode("1/2/3") }
        assertFailsWith(DecoderException::class) { InstantDecoder().decode("not a date") }
        InstantDecoder().decode("2001-11-09T23:12:00") shouldBe LocalDateTime.of(2001, 11, 9, 23, 12, 0).toLocalInstant()
        InstantDecoder("yyyy-MM-d'T'HH:mm").decode("2000-11-9T23:12") shouldBe LocalDateTime.of(2000, 11, 9, 23, 12, 0).toLocalInstant()
        InstantDecoder("MM/d/yyyy HH:mm").decode("11/9/2001 23:12") shouldBe LocalDateTime.of(2001, 11, 9, 23, 12, 0).toLocalInstant()
    }

    private fun LocalDateTime.toLocalInstant() = atZone(ZoneId.systemDefault()).toInstant()

    @Test
    fun testFormatter() {
        printFormatted(DateTimeFormatter.ISO_INSTANT)

        printFormatted(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS['Z']"))

        val fmt1 = DateTimeFormatterBuilder()
                .parseCaseInsensitive().parseLenient()
                .appendPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSVV")
                .toFormatter()
        printFormatted(fmt1)

        val fmt2 = DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd'T'HH:mm:ss")
                .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
                .parseLenient()
                .toFormatter()
        printFormatted(fmt2)
    }

    fun printFormatted(fmt: DateTimeFormatter) {
        println("\n---\n$fmt")
        tryPrint { fmt.format(LocalDateTime.now()) }
        tryPrint { fmt.format(Instant.now()) }

        tryPrint { fmt.parse("2017-10-31T16:40:10.1") }
        tryPrint { fmt.parse("2017-10-31T16:40:10.123") }
        tryPrint { fmt.parse("2017-10-31T16:40:10.1234567") }

        tryPrint { LocalDateTime.parse("2017-10-31T16:40:10.1", fmt) }
        tryPrint { LocalDateTime.parse("2017-10-31T16:40:10.123", fmt) }
        tryPrint { LocalDateTime.parse("2017-10-31T16:40:10.1234567", fmt) }

        tryPrint { fmt.parse("2017-10-31T16:40:10.1Z") }
        tryPrint { fmt.parse("2017-10-31T16:40:10.123Z") }
        tryPrint { fmt.parse("2017-10-31T16:40:10.1234567Z") }

        tryPrint { LocalDateTime.parse("2017-10-31T16:40:10.1Z", fmt) }
        tryPrint { LocalDateTime.parse("2017-10-31T16:40:10.123Z", fmt) }
        tryPrint { LocalDateTime.parse("2017-10-31T16:40:10.1234567Z", fmt) }
    }

    private fun tryPrint(input: () -> Any?) = try {
        println(input())
    } catch (x: Exception) {
        println(x)
    }

}
