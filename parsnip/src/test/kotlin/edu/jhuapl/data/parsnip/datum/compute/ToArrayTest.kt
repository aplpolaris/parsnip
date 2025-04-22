package edu.jhuapl.data.parsnip.datum.compute

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * ToArrayTest.kt
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
import edu.jhuapl.testkt.printJsonTest
import junit.framework.TestCase
import org.junit.Test

class ToArrayTest : TestCase() {

    @Test
    fun testSerialize() {
        ToArray().printJsonTest()
        ToArray(true).printJsonTest()
        ToArray(listOf("a", "b", "c"), true).printJsonTest()
    }

    @Test
    fun testInvoke() {
        ToArray(true).invoke(mapOf("a" to listOf(1, 2, 3))).toList() shouldBe listOf(1, 2, 3)
        ToArray(true).invoke(mapOf("a" to listOf(1, 2, listOf(3, 4)), "b" to 5)).toList() shouldBe listOf(1, 2, listOf(3, 4), 5)
        ToArray(false).invoke(mapOf("a" to listOf(1, 2), "b" to 5)).toList() shouldBe listOf(listOf(1, 2), 5)
    }

}
