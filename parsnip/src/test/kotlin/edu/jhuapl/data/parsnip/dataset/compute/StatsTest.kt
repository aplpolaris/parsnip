/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * StatsTest.kt
 * edu.jhuapl.data:parsnip
 * %%
 * Copyright (C) 2019 - 2024 Johns Hopkins University Applied Physics Laboratory
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
package edu.jhuapl.data.parsnip.dataset.compute

import edu.jhuapl.data.parsnip.dataset.DataSetCompute
import edu.jhuapl.data.parsnip.dataset.stats
import edu.jhuapl.data.parsnip.datum.Datum
import edu.jhuapl.util.internal.recycleJsonTest
import junit.framework.TestCase
import edu.jhuapl.testkt.shouldBe

class StatsTest : TestCase() {

    fun testSerialize() {
        ArgMin("a").recycleJsonTest<DataSetCompute<Datum>>()
        ArgMax("a").recycleJsonTest<DataSetCompute<Datum>>()
    }

    fun testStats_Invoke() {
        listOf(mapOf("a" to 1), mapOf("a" to 2)).stats("a").count shouldBe 2L
        listOf(mapOf("a" to 1), mapOf("a" to 2)).stats("a").average shouldBe 1.5

        stats("a").invoke(listOf(mapOf("a" to 1), mapOf("a" to 2)))!!.count shouldBe 2L
    }

    fun testArgMin_Invoke() {
        ArgMin("a").invoke(listOf(mapOf("a" to 1), mapOf("a" to 2))) shouldBe mapOf("a" to 1)
    }

    fun testArgMax_Invoke() {
        ArgMax("a").invoke(listOf(mapOf("a" to 1), mapOf("a" to 2))) shouldBe mapOf("a" to 2)
    }

}
