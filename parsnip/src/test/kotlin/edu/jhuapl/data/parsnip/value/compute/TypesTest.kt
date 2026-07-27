/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * TypesTest.kt
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
package edu.jhuapl.data.parsnip.value.compute

import edu.jhuapl.testkt.shouldBe
import edu.jhuapl.testkt.printJsonTest
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

class TypesTest {

    @Test
    fun testAs_Serialize() {
        As(Long::class.java).printJsonTest()
        As("long").printJsonTest()
        As("Long").printJsonTest()
    }

    @Test
    fun testAs_Invoke() {
        As(Date::class.java).invoke("2012-07-16 19:35:06.000") shouldBe Date(112, 6, 16, 19, 35, 6)
        // Input has no zone/offset, so decoding intentionally applies the JVM's default zone
        // (see InstantDecoder) -- compute the expected value the same way instead of a
        // hardcoded literal tied to one specific zone (this used to assume America/New_York,
        // which made the test fail on UTC CI runners; see issue #45).
        val expectedMillis = LocalDateTime.parse("2012-07-16T19:35:06.000")
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        As(Long::class.java).invoke("2012-07-16 19:35:06.000") shouldBe expectedMillis
    }

    @Test
    fun testAsInstant_Serialize() {
        DecodeInstant("yyyy-MM-dd HH:mm:ss.SSS").printJsonTest()
    }

    @Test
    fun testAsInstant_Invoke() {
        DecodeInstant("yyyy-MM-dd HH:mm:ss.SSS").invoke("2012-07-16 19:35:06.000") shouldBe
                LocalDateTime.parse("2012-07-16T19:35:06.000").atZone(ZoneId.systemDefault()).toInstant()
    }

}
