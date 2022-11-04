/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * OneHotTest.kt
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
package edu.jhuapl.data.parsnip.value.compute

import edu.jhuapl.data.parsnip.value.ValueCompute
import edu.jhuapl.testkt.contentShouldBe
import edu.jhuapl.util.internal.recycleJsonTest
import org.junit.Test
import java.io.IOException

class OneHotTest {

    @Test
    @Throws(IOException::class)
    fun testSerialize() {
        OneHot<Any>().recycleJsonTest<ValueCompute<IntArray>>()
        OneHot("a", "b").recycleJsonTest<ValueCompute<IntArray>>()
        OneHot("a", null).recycleJsonTest<ValueCompute<IntArray>>()
    }

    @Test
    operator fun invoke() {
        OneHot<Any>()("a") contentShouldBe intArrayOf()
        OneHot("a", "b")("a") contentShouldBe intArrayOf(1, 0)
        OneHot("a", "b")("b") contentShouldBe intArrayOf(0, 1)
        OneHot("a", "b")("c") contentShouldBe intArrayOf(0, 0)
        OneHot("a", "b")(null) contentShouldBe intArrayOf(0, 0)
        OneHot("a", null)(null) contentShouldBe intArrayOf(0, 1)
    }

}
