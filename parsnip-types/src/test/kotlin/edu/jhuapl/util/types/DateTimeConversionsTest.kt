/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * DateTimeFormatsTest.kt
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

import edu.jhuapl.testkt.shouldBe
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoField
import kotlin.test.assertFailsWith

class DateTimeConversionsTest {

    @Test
    fun testParsers() {
        LocalDate.parse("Jun2012", DateTimeFormatterBuilder()
            .appendPattern("MMMyyyy")
            .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
            .toFormatter()
        ) shouldBe LocalDate.of(2012, 6, 1)

        LocalTime.parse("4:01:36 AM", DateTimeFormatterBuilder()
            .appendPattern("h:mm:ss a")
            .toFormatter()
        ) shouldBe LocalTime.of(4, 1, 36)

        Instant.parse("2025-01-01T00:00:00.000Z") shouldBe
                LocalDateTime.of(2025, 1, 1, 0, 0).toInstant(ZoneOffset.UTC)

        // not the right ISO8601 format, so Instant.parse fails
        assertFailsWith(DateTimeParseException::class) {
            Instant.parse("2025-01-01T00:00:00.000+0000")
        }

        // ZDT requires a timezone
        assertFailsWith(DateTimeParseException::class) {
            ZonedDateTime.parse("6/1/2012 4:01:36 AM", formatter("M/d/yyyy h:mm:ss a"))
        }
    }

    @Test
    fun testToLocalDateOrNull() {
        println("Testing formats: $DATE_ONLY_FORMATS")

        // test all date formats above
        "6/1/12".toLocalDateOrNull() shouldBe LocalDate.of(2012, 6, 1)
        "6/1/2012".toLocalDateOrNull() shouldBe LocalDate.of(2012, 6, 1)
        "2012/6/1".toLocalDateOrNull() shouldBe LocalDate.of(2012, 6, 1)
        "6-1-12".toLocalDateOrNull() shouldBe LocalDate.of(2012, 6, 1)
        "6-1-2012".toLocalDateOrNull() shouldBe LocalDate.of(2012, 6, 1)
        "2012-6-1".toLocalDateOrNull() shouldBe LocalDate.of(2012, 6, 1)
        "Jun2012".toLocalDateOrNull() shouldBe LocalDate.of(2012, 6, 1)
        "Jun 1, 2012".toLocalDateOrNull() shouldBe LocalDate.of(2012, 6, 1)
        "June 1, 2012".toLocalDateOrNull() shouldBe LocalDate.of(2012, 6, 1)

        // works with date/time parsing also (fall back from time parsing)
        "2012-6-1 16:01:36.123Z".toLocalDateOrNull() shouldBe LocalDate.of(2012, 6, 1)
        "2012-6-1T16:01:36.123Z".toLocalDateOrNull() shouldBe LocalDate.of(2012, 6, 1)
        "6/1/2012 4:01:36 AM".toLocalDateOrNull() shouldBe LocalDate.of(2012, 6, 1)

        // test some that don't parse
        "June2012".toLocalDateOrNull() shouldBe null
        "13/1/2012".toLocalDateOrNull() shouldBe null
        "not a date".toLocalDateOrNull() shouldBe null
    }

    @Test
    fun testToLocalTimeOrNull() {
        println("Testing formats: $TIME_ONLY_FORMATS")

        "4:01:36 AM".toLocalTimeOrNull() shouldBe LocalTime.of(4, 1, 36)
        "16:01:36.123".toLocalTimeOrNull() shouldBe LocalTime.of(16, 1, 36, 123_000_000)
        "16:01:36".toLocalTimeOrNull() shouldBe LocalTime.of(16, 1, 36)
        "16:01".toLocalTimeOrNull() shouldBe LocalTime.of(16, 1)

        // zone offsets are ignored when parsing times
        "16:01:36.123Z".toLocalTimeOrNull() shouldBe LocalTime.of(16, 1, 36, 123_000_000)
        "16:01:36.123+0000".toLocalTimeOrNull() shouldBe LocalTime.of(16, 1, 36, 123_000_000)

        // works with ISO8601 date/time also
        "2012-6-1 16:01:36.123Z".toLocalTimeOrNull() shouldBe LocalTime.of(16, 1, 36, 123_000_000)
        "2012-6-1T16:01:36.123Z".toLocalTimeOrNull() shouldBe LocalTime.of(16, 1, 36, 123_000_000)

        // test some that don't parse
        "25:01".toLocalTimeOrNull() shouldBe null
        "16:01:36.123+000".toLocalTimeOrNull() shouldBe null
        "not a time".toLocalTimeOrNull() shouldBe null
    }

    @Test
    fun testToInstantOrNull() {
        println("Testing formats: $DATE_AND_TIME_FORMATS")

        // test some combinations above the above, with and without T separator
        "6/1/2012 4:01:36 AM".toInstantOrNull() shouldBe
                LocalDateTime.of(2012, 6, 1, 4, 1, 36).toInstant()
        "6/1/2012T4:01:36 AM".toInstantOrNull() shouldBe
                LocalDateTime.of(2012, 6, 1, 4, 1, 36).toInstant()
        "Jun 1, 2012 16:01".toInstantOrNull() shouldBe
                LocalDate.of(2012, 6, 1).atTime(16, 1).toInstant()
        "Jun 1, 2012T16:01".toInstantOrNull() shouldBe
                LocalDate.of(2012, 6, 1).atTime(16, 1).toInstant()

        // test some that don't parse
        "6/1/2012 25:01".toInstantOrNull() shouldBe null
        "not a date time".toInstantOrNull() shouldBe null
    }

    @Test
    fun testToInstantOrNull_zonedUtc() {
        "2012-6-1 16:01:36.123+00:00".toInstantOrNull() shouldBe
                ZonedDateTime.of(2012, 6, 1, 16, 1, 36, 123_000_000, ZoneOffset.UTC).toInstant()
        "2012-6-1T16:01:36.123+00:00".toInstantOrNull() shouldBe
                ZonedDateTime.of(2012, 6, 1, 16, 1, 36, 123_000_000, ZoneOffset.UTC).toInstant()

        "2025-01-01T00:00:00.000+0000".toInstantOrNull() shouldBe
                ZonedDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant()
        "2025-01-01T00:00:00.000Z".toInstantOrNull() shouldBe
                ZonedDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant()
    }

    @Test
    fun testToInstantOrNull_zonedOther() {
        // test alternate time zone
        "2025-01-01T00:00:00.000+0800".toInstantOrNull() shouldBe
                ZonedDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(8)).toInstant()
    }

    @Test
    fun testDateTimeFormatRank() {
        // test that this runs
        DateTimeFormatRank("6/1/2012 3:59:34 AM", "MMM/dd/yyyy")
    }

}
