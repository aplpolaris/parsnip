/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * DatasetFunctionsKtTest.kt
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
package edu.jhuapl.data.parsnip.dataset

import edu.jhuapl.testkt.shouldBe
import org.junit.Test

class DatasetFunctionsKtTest {

    @Test
    fun testTopTuples() {
        val dataset: DataSet = listOf(mapOf("a" to 3), mapOf("a" to 4), mapOf("a" to 4))
        val result = topTuples(listOf("a", "b"), "Count", 100).invoke(dataset)!!.toSet()
        result shouldBe setOf(mapOf("a" to 3, "b" to null, "Count" to 1), mapOf("a" to 4, "b" to null, "Count" to 2))
    }

}
