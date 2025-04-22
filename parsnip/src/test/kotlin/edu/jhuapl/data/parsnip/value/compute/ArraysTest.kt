package edu.jhuapl.data.parsnip.value.compute

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * ArraysTest.kt
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
import edu.jhuapl.testkt.printPlainMapperJsonTest
import junit.framework.TestCase
import org.junit.Test
import java.io.IOException

class ArraysTest : TestCase() {

    @Test
    @Throws(IOException::class)
    fun testSerialize() {
        FlattenList("value", "index").printPlainMapperJsonTest()
        FlattenMatrix("value", "i", "j").printPlainMapperJsonTest()
    }

    @Test
    fun testFlattenList() {
        with (FlattenList("value", "index")) {
            invoke(1) shouldBe 1
            invoke(listOf(1, 2)) shouldBe listOf(mapOf("value" to 1, "index" to 0), mapOf("value" to 2, "index" to 1))
        }
    }

    @Test
    fun testFlattenMatrix() {
        with (FlattenMatrix("value", "i", "j")) {
            invoke(1) shouldBe 1
            invoke(listOf(1, 2)) shouldBe listOf(1, 2)
            invoke(listOf(listOf(0), listOf(1, 2))) shouldBe listOf(mapOf("value" to 0, "i" to 0, "j" to 0),
                    mapOf("value" to 1, "i" to 1, "j" to 0), mapOf("value" to 2, "i" to 1, "j" to 1))
        }
    }

}
