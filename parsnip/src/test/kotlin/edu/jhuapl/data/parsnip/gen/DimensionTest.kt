/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * DimensionTest.kt
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
package edu.jhuapl.data.parsnip.gen

import edu.jhuapl.testkt.shouldBe
import org.junit.Test

class DimensionTest {

    @Test
    fun testValueOf() {
        DimensionConstraint.valueOf("false").defaultValue shouldBe false
        DimensionConstraint.valueOf("true").defaultValue shouldBe true
        DimensionConstraint.valueOf("set(a)").toString() shouldBe AllowedElementSetConstraint(setOf("a"), setOf("a")).toString()
        DimensionConstraint.valueOf("set(; a)").toString() shouldBe AllowedElementSetConstraint(setOf("a"), setOf()).toString()
        DimensionConstraint.valueOf("int.range(0, 100)").toString() shouldBe FiniteIntRangeConstraint(0, 100, 0).toString()
    }

}
