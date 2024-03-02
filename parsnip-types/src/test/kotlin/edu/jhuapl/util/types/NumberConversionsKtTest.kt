/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * NumberConversionsKtTest.kt
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
package edu.jhuapl.util.types

import edu.jhuapl.testkt.shouldBe
import junit.framework.TestCase
import java.io.File
import java.io.FileReader
import java.text.NumberFormat
import kotlin.test.assertFailsWith

class NumberConversionsKtTest : TestCase() {

    fun testNumberType() {
        Long::class.java.numberType() shouldBe true
        java.lang.Long::class.java.numberType() shouldBe true
        String::class.java.numberType() shouldBe false
    }

    fun testToNumber() {
        "0".toNumber(Int::class.java) shouldBe 0
        "0".toNumber(java.lang.Integer::class.java) shouldBe 0
        "00".toNumber(java.lang.Integer::class.java) shouldBe 0
        " 0".toNumber(java.lang.Integer::class.java) shouldBe 0
        "0".toNumber(java.lang.Double::class.java) shouldBe 0.0

        0.toNumber(Int::class.java) shouldBe 0
        0.toNumber(Float::class.java) shouldBe 0f
        0.toNumber(Long::class.java) shouldBe 0L

        listOf("nonsense", "", "-", "-NaN", "+NaN").forEach {
            assertFailsWith(NumberFormatException::class) { it.toNumber<Integer>() }
        }

        // probably shouldn't...
        "NaN".toNumber(java.lang.Integer::class.java) shouldBe 0

        listOf("nonsense", "", "-").forEach {
            assertFailsWith(NumberFormatException::class) { it.toNumber<Double>() }
        }

        "-NaN".toNumber<Double>() shouldBe Double.NaN
        "+NaN".toNumber<Double>() shouldBe Double.NaN
        "NaN".toNumber<Double>() shouldBe Double.NaN
    }

    fun testPercent() {
        NumberFormat.getPercentInstance().format(0.0) shouldBe "0%"
        NumberFormat.getPercentInstance().format(1.0) shouldBe "100%"
        NumberFormat.getPercentInstance().parse("100%") shouldBe 1L
        NumberFormat.getPercentInstance().parse("101%") shouldBe 1.01

        // more mathematically correct for these to evaluate to 10, but target 1000 here since that is what the Java method does
        "1,000%".toNumber<Int>() shouldBe 1000
        "1,000%".toNumber<Double>() shouldBe 1000.0
    }

    fun testFormattedNumbers() {
        "1,000".toNumber<Int>() shouldBe 1000
        "1,000".toNumber<Double>() shouldBe 1000.0
    }

    fun testToNumberOrNull() {
        "0".toNumberOrNull() shouldBe 0
        "0.0".toNumberOrNull() shouldBe 0.0
        "1000".toNumberOrNull() shouldBe 1000
        "1000000000".toNumberOrNull() shouldBe 1000000000
        "1000000000000".toNumberOrNull() shouldBe 1000000000000L
        "NaN".toNumberOrNull() shouldBe Double.NaN
        "+NaN".toNumberOrNull() shouldBe Double.NaN
        "-NaN".toNumberOrNull() shouldBe Double.NaN
        "-".toNumberOrNull() shouldBe 0.0

        "1,000".toNumberOrNull() shouldBe 1000L
        "100%".toNumberOrNull() shouldBe 1L
        "101%".toNumberOrNull() shouldBe 1.01
        "1,000%".toNumberOrNull() shouldBe 10L
        "1,000,000.1%".toNumberOrNull() shouldBe 10000.001
        "1000000,000,000,000.0%".toNumberOrNull() shouldBe 10000000000000L

        0.toNumberOrNull(Int::class.java) shouldBe 0
        0.toNumberOrNull(Float::class.java) shouldBe 0f
        0.toNumberOrNull(Long::class.java) shouldBe 0L

        "NaN".toNumberOrNull(java.lang.Integer::class.java) shouldBe null
    }

}
