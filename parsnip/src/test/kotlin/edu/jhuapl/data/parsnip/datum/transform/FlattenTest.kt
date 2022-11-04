package edu.jhuapl.data.parsnip.datum.transform

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * FlattenTest.kt
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

import edu.jhuapl.data.parsnip.datum.MultiDatumTransform
import edu.jhuapl.testkt.shouldBe
import edu.jhuapl.util.internal.recycleJsonTest
import junit.framework.TestCase
import org.junit.Test
import java.io.IOException

class FlattenTest : TestCase() {

    @Test
    @Throws(IOException::class)
    fun testSerialize() {
        Flatten().recycleJsonTest<MultiDatumTransform>()
        Flatten("a").recycleJsonTest<MultiDatumTransform>()
        Flatten(listOf("a", "b")).recycleJsonTest<MultiDatumTransform>()
        Flatten(listOf("a", "b"), listOf("alt", "balt")).recycleJsonTest<MultiDatumTransform>()
    }

    @Test
    fun testInvoke() {
        val flatten = Flatten(listOf("a", "b"), collate = false)
        flatten(mapOf("a" to 0, "b" to 2)) shouldBe listOf(mapOf("a" to 0, "b" to 2))
        flatten(mapOf("a" to listOf(0, 1), "b" to 2)) shouldBe listOf(mapOf("a" to 0, "b" to 2), mapOf("a" to 1, "b" to 2))
        flatten(mapOf("a" to listOf(0, 1), "b" to listOf(2, 3, 4))).size shouldBe 6

        val flatten2 = Flatten(listOf("a", "b"), listOf("alt"), collate = false)
        flatten2(mapOf("a" to listOf(0, 1), "b" to 2)) shouldBe listOf(mapOf("alt" to 0, "b" to 2), mapOf("alt" to 1, "b" to 2))

        val flatten3 = Flatten(listOf("a", "b"), listOf("x", "y"), collate = true)
        flatten3(mapOf("a" to listOf(0, 1), "b" to 2)) shouldBe listOf(mapOf("x" to 0, "y" to 2), mapOf("x" to 1, "y" to null))
        flatten3(mapOf("a" to listOf(0, 1), "b" to listOf(2, 3, 4))).size shouldBe 3
    }
}
