package edu.jhuapl.data.parsnip.value

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * DateTimeFilterTest.kt
 * edu.jhuapl.data:parsnip
 * %%
 * Copyright (C) 2019 - 2026 Johns Hopkins University Applied Physics Laboratory
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

import edu.jhuapl.data.parsnip.value.filter.*
import edu.jhuapl.testkt.recycleJsonTest
import edu.jhuapl.testkt.shouldBe
import junit.framework.TestCase
import java.time.Instant
import java.time.LocalDate
import kotlin.test.assertFailsWith

class DateTimeFilterTest : TestCase() {

    // region IsDate tests

    fun testIsDate_null() {
        IsDate(null) shouldBe false
    }

    fun testIsDate_validDateStrings() {
        IsDate("2021-01-15") shouldBe true
        IsDate("6/1/2012") shouldBe true
        IsDate("Jun 1, 2012") shouldBe true
    }

    fun testIsDate_dateTimeStrings() {
        // date-time strings can also be interpreted as dates
        IsDate("2021-01-15T10:00:00Z") shouldBe true
        IsDate("2021-01-15 10:00:00") shouldBe true
    }

    fun testIsDate_invalidStrings() {
        IsDate("not a date") shouldBe false
        IsDate("") shouldBe false
        IsDate("13/32/2021") shouldBe false
    }

    fun testIsDate_localDateObject() {
        IsDate(LocalDate.of(2021, 1, 15)) shouldBe true
    }

    // endregion

    // region IsDateTime tests

    fun testIsDateTime_null() {
        IsDateTime(null) shouldBe false
    }

    fun testIsDateTime_isoInstant() {
        IsDateTime("2021-01-15T10:00:00Z") shouldBe true
    }

    fun testIsDateTime_isoWithOffset() {
        IsDateTime("2021-01-15T10:00:00+05:30") shouldBe true
        IsDateTime("2021-01-15T10:00:00-08:00") shouldBe true
    }

    fun testIsDateTime_localDateTime() {
        IsDateTime("2021-01-15T10:00:00") shouldBe true
        IsDateTime("6/1/2012 4:01:36 AM") shouldBe true
    }

    fun testIsDateTime_dateOnly() {
        // date-only strings are also valid instants (at start of day)
        IsDateTime("2021-01-15") shouldBe true
    }

    fun testIsDateTime_invalidStrings() {
        IsDateTime("not a datetime") shouldBe false
        IsDateTime("") shouldBe false
    }

    fun testIsDateTime_instantObject() {
        IsDateTime(Instant.now()) shouldBe true
    }

    // endregion

    // region Before tests

    fun testBefore_null() {
        Before("2021-01-15T00:00:00Z")(null) shouldBe false
    }

    fun testBefore_strictlyBefore() {
        Before("2021-01-15T00:00:00Z")("2021-01-14T23:59:59Z") shouldBe true
    }

    fun testBefore_equalNotBefore() {
        Before("2021-01-15T00:00:00Z")("2021-01-15T00:00:00Z") shouldBe false
    }

    fun testBefore_after() {
        Before("2021-01-15T00:00:00Z")("2021-01-16T00:00:00Z") shouldBe false
    }

    fun testBefore_invalidInput() {
        Before("2021-01-15T00:00:00Z")("not a date") shouldBe false
    }

    fun testBefore_timezoneOffset() {
        // "2021-01-15T01:00:00+02:00" is "2021-01-14T23:00:00Z", which is before "2021-01-15T00:00:00Z"
        Before("2021-01-15T00:00:00Z")("2021-01-15T01:00:00+02:00") shouldBe true
    }

    fun testBefore_invalidTimestampThrows() {
        assertFailsWith<IllegalArgumentException> { Before("not a timestamp") }
    }

    // endregion

    // region After tests

    fun testAfter_null() {
        After("2021-01-15T00:00:00Z")(null) shouldBe false
    }

    fun testAfter_strictlyAfter() {
        After("2021-01-15T00:00:00Z")("2021-01-16T00:00:00Z") shouldBe true
    }

    fun testAfter_equalNotAfter() {
        After("2021-01-15T00:00:00Z")("2021-01-15T00:00:00Z") shouldBe false
    }

    fun testAfter_before() {
        After("2021-01-15T00:00:00Z")("2021-01-14T00:00:00Z") shouldBe false
    }

    fun testAfter_invalidInput() {
        After("2021-01-15T00:00:00Z")("not a date") shouldBe false
    }

    fun testAfter_timezoneOffset() {
        // "2021-01-15T23:00:00+08:00" is "2021-01-15T15:00:00Z", which is after "2021-01-15T00:00:00Z"
        After("2021-01-15T00:00:00Z")("2021-01-15T23:00:00+08:00") shouldBe true
    }

    fun testAfter_invalidTimestampThrows() {
        assertFailsWith<IllegalArgumentException> { After("not a timestamp") }
    }

    // endregion

    // region Between tests

    fun testBetween_null() {
        Between("2021-01-01T00:00:00Z", "2021-12-31T23:59:59Z")(null) shouldBe false
    }

    fun testBetween_inside() {
        Between("2021-01-01T00:00:00Z", "2021-12-31T23:59:59Z")("2021-06-15T12:00:00Z") shouldBe true
    }

    fun testBetween_atStartInclusive() {
        Between("2021-01-01T00:00:00Z", "2021-12-31T23:59:59Z")("2021-01-01T00:00:00Z") shouldBe true
    }

    fun testBetween_atEndInclusive() {
        Between("2021-01-01T00:00:00Z", "2021-12-31T23:59:59Z")("2021-12-31T23:59:59Z") shouldBe true
    }

    fun testBetween_beforeStart() {
        Between("2021-01-01T00:00:00Z", "2021-12-31T23:59:59Z")("2020-12-31T23:59:59Z") shouldBe false
    }

    fun testBetween_afterEnd() {
        Between("2021-01-01T00:00:00Z", "2021-12-31T23:59:59Z")("2022-01-01T00:00:00Z") shouldBe false
    }

    fun testBetween_invalidInput() {
        Between("2021-01-01T00:00:00Z", "2021-12-31T23:59:59Z")("not a date") shouldBe false
    }

    fun testBetween_timezoneOffset() {
        // "2021-06-15T14:00:00+02:00" is "2021-06-15T12:00:00Z", which is within the range
        Between("2021-01-01T00:00:00Z", "2021-12-31T23:59:59Z")("2021-06-15T14:00:00+02:00") shouldBe true
    }

    fun testBetween_invalidStartTimestampThrows() {
        assertFailsWith<IllegalArgumentException> { Between("not a timestamp", "2021-12-31T23:59:59Z") }
    }

    fun testBetween_invalidEndTimestampThrows() {
        assertFailsWith<IllegalArgumentException> { Between("2021-01-01T00:00:00Z", "not a timestamp") }
    }

    // endregion

    // region Serialization tests

    fun testSerialize() {
        testSerialize(IsDate)
        testSerialize(IsDateTime)
        testSerialize(Before("2021-01-15T00:00:00Z"))
        testSerialize(After("2021-01-15T00:00:00Z"))
        testSerialize(Between("2021-01-01T00:00:00Z", "2021-12-31T23:59:59Z"))
    }

    private fun testSerialize(vf: ValueFilter) = vf.recycleJsonTest()

    // endregion
}
