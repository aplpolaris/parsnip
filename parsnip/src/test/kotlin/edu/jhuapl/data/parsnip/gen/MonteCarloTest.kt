/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * MonteCarloTest.kt
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
package edu.jhuapl.data.parsnip.gen

import edu.jhuapl.util.internal.recycleJsonTest
import org.junit.Test

class MonteCarloTest {

    fun testMc() =
            MonteCarlo(dimensions = listOf(
                    Dimension("name", EnumConstraint(StringDimensionType, "a", "b", "c")),
                    Dimension("value", FiniteIntRangeConstraint(1, 10)),
                    Dimension("x", FiniteDoubleRangeConstraint(-5.0, 4.0)),
                    Dimension("y", FiniteDoubleRangeConstraint(-2.0, 1.0)),
                    Dimension("z", NormalRangeConstraint(90.0, 3.0)),
                    Dimension("?", BooleanConstraint()),
                    Dimension("set", AllowedElementSetConstraint(setOf("x", "y", "z")))
            ))

    @Test
    fun testSerialize() {
        val mc2 = testMc().recycleJsonTest()
        generateSequence { mc2() }.take(10).forEach { println(it) }
    }

    @Test
    fun testDimensionsToString() {
        testMc().dimensions.forEach { println(it) }
    }

    @Test
    fun testConstraintsFromString() {
        testMc().dimensions
                .onEach { println(it) }
                .map { it.constraint.toString() }
                .map { DimensionConstraint.valueOf(it) }
                .forEach { println(it) }
    }

    @Test
    fun testInvoke() {
        val mc = testMc()
        generateSequence { mc() }.take(10).forEach { println(it) }
    }

}
