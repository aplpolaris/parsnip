/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * PatchTest.kt
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
package edu.jhuapl.data.parsnip.gen

import edu.jhuapl.data.parsnip.datum.Datum
import edu.jhuapl.testkt.shouldBe
import org.junit.Test

class PatchTest {

    @Test
    fun testPatch() {
        val template = mapOf("a" to listOf(mutableMapOf("test" to "one")), "b" to mutableMapOf("b2" to "what"))
        template.patch(mapOf("/b/b2" to 10)) shouldBe mapOf("a" to listOf(mapOf("test" to "one")), "b" to mapOf("b2" to 10))
        template.patch(mapOf("/a/0/test" to 10)) shouldBe mapOf("a" to listOf(mapOf("test" to 10)), "b" to mapOf("b2" to 10))
    }

    @Test
    fun testPatchSequence() {
        val template = mapOf("a" to listOf(mapOf("test" to "one", "else" to "5")), "b" to "what")
        val mc = MonteCarlo(Dimension("/a/0/test", FiniteIntRangeConstraint(1, 5)))

        generateSequence { mc() }.patch(template).take(10).forEach { println(it) }
    }

}
