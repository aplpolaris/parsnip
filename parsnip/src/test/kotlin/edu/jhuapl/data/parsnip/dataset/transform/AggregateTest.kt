/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * AggregateTest.kt
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
package edu.jhuapl.data.parsnip.dataset.transform

import edu.jhuapl.data.parsnip.dataset.DataSetTransform
import edu.jhuapl.data.parsnip.dataset.count
import edu.jhuapl.data.parsnip.datum.Datum
import edu.jhuapl.data.parsnip.set.compute.Min
import edu.jhuapl.testkt.shouldBe
import edu.jhuapl.testkt.shouldThrow
import edu.jhuapl.testkt.recycleJsonTest
import junit.framework.TestCase

class AggregateTest : TestCase() {

    fun testSerialize() {
        count(listOf("a"), "count").recycleJsonTest<DataSetTransform>()
        Aggregate(listOf("b"), Min, "a", "a_min").recycleJsonTest<DataSetTransform>()
    }

    fun testAggregateCount() {
        count(listOf("a"), "count").invoke(listOf()) shouldBe emptyList<Datum>()

        val sampleInput = listOf(mapOf("a" to 1, "b" to 2), mapOf("a" to 2, "b" to 2), mapOf("a" to 3, "b" to 2))
        count(listOf(), "count").invoke(sampleInput) shouldBe listOf(mapOf("count" to 3))
        count(listOf("b"), "count").invoke(sampleInput) shouldBe listOf(mapOf("b" to 2, "count" to 3))
        count(listOf("a"), "count").invoke(sampleInput) shouldBe listOf(mapOf("a" to 1, "count" to 1), mapOf("a" to 2, "count" to 1), mapOf("a" to 3, "count" to 1))
        count(listOf("a", "b"), "count").invoke(sampleInput) shouldBe listOf(mapOf("a" to 1, "b" to 2, "count" to 1), mapOf("a" to 2, "b" to 2, "count" to 1), mapOf("a" to 3, "b" to 2, "count" to 1))
    }

    fun testAggregateMin() {
        { Aggregate(listOf(), Min, null, "x") } shouldThrow IllegalArgumentException::class

        val sampleInput = listOf(mapOf("a" to 1, "b" to 2), mapOf("a" to 2, "b" to 2), mapOf("a" to 3, "b" to 2))
        Aggregate(listOf(), Min, "a", "a_min").invoke(sampleInput) shouldBe listOf(mapOf("a_min" to 1))
        Aggregate(listOf("b"), Min, "a", "a_min").invoke(sampleInput) shouldBe listOf(mapOf("b" to 2, "a_min" to 1))
    }

}
