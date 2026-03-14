/*-
 * #%L
 * parsnip-2.0.1-SNAPSHOT
 * %%
 * Copyright (C) 2019 - 2026 Johns Hopkins University Applied Physics Laboratory
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

import edu.jhuapl.testkt.shouldBe
import junit.framework.TestCase

class SortTest : TestCase() {

    private val data = listOf(
        mapOf("a" to 2),
        mapOf("a" to null),
        mapOf("a" to 1),
        mapOf("a" to null),
        mapOf("a" to 3)
    )

    fun testSortBy_nullsLast() {
        SortBy("a").invoke(data).map { it["a"] } shouldBe listOf(1, 2, 3, null, null)
    }

    fun testSortByDescending_nullsFirst() {
        // sortedByDescending reverses the comparator, so nulls appear first in descending order
        SortByDescending("a").invoke(data).map { it["a"] } shouldBe listOf(null, null, 3, 2, 1)
    }

}
