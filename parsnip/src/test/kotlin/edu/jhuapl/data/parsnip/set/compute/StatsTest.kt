/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * StatsTest.kt
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
package edu.jhuapl.data.parsnip.set.compute

import edu.jhuapl.testkt.shouldBe
import edu.jhuapl.testkt.shouldThrow
import junit.framework.TestCase
import java.lang.NullPointerException

class StatsTest : TestCase() {

    fun testStats() {
        Sum(listOf<Int>()) shouldBe 0.0
        Sum(listOf(1, 2, 3)) shouldBe 6
        Sum(listOf(1, 2.0, 3)) shouldBe 6.0

        Average(listOf(1, 2.0, 3)) shouldBe 2.0
        Mean(listOf(1, 2.0, 3)) shouldBe 2.0
        Min(listOf(1, 2.0, 3)) shouldBe 1.0
        Max(listOf(1, 2.0, 3)) shouldBe 3.0
    }

    fun testExceptions() {
        Average(listOf()) shouldBe Double.NaN

        { Sum(listOf(1, null)) } shouldThrow NullPointerException::class
        { Sum(listOf(1, "two")) } shouldThrow NullPointerException::class

        Sum(listOf(1, "2")) shouldBe 3
    }

}
