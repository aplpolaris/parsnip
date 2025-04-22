package edu.jhuapl.data.parsnip.datum.transform

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * FoldTest.kt
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

import edu.jhuapl.data.parsnip.datum.MultiDatumTransform
import edu.jhuapl.testkt.shouldBe
import edu.jhuapl.testkt.recycleJsonTest
import junit.framework.TestCase
import org.junit.Test
import java.io.IOException

class FoldTest : TestCase() {

    @Test
    @Throws(IOException::class)
    fun testSerialize() {
        Fold().recycleJsonTest<MultiDatumTransform>()
        Fold("a").recycleJsonTest<MultiDatumTransform>()
        Fold(listOf("a", "b")).recycleJsonTest<MultiDatumTransform>()
        Fold(listOf("a", "b"), listOf("alt", "balt")).recycleJsonTest<MultiDatumTransform>()
    }

    @Test
    fun testInvoke() {
        Fold("a", "b").invoke(mapOf("a" to 0, "b" to 2)) shouldBe
                listOf(mapOf("a" to 0, "b" to 2, "key" to "a", "value" to 0), mapOf("a" to 0, "b" to 2, "key" to "b", "value" to 2))

        Fold(listOf("a", "b"), listOf("indicator")).invoke(mapOf("a" to 0, "b" to 2)) shouldBe
                listOf(mapOf("a" to 0, "b" to 2, "indicator" to "a", "value" to 0), mapOf("a" to 0, "b" to 2, "indicator" to "b", "value" to 2))
    }
}
