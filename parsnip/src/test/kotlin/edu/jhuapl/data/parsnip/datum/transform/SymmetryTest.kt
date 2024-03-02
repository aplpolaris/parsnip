package edu.jhuapl.data.parsnip.datum.transform

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * SymmetryTest.kt
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

import com.google.common.collect.ImmutableMap
import edu.jhuapl.util.internal.printJsonTest
import org.junit.Test

import org.junit.Assert.*

class SymmetryTest {

    @Test
    fun testSerialize() {
        Symmetry().printJsonTest()
        Symmetry("x" to "y", "a" to "b").printJsonTest()
    }

    @Test
    fun testInvoke() {
        val instance = Symmetry()
        instance.transform = ImmutableMap.of("ip1", "ip2", "port1", "port2")
        val testDatum = ImmutableMap.of("ip1", "1", "ip2", "2", "port1", "p1", "port2", "p2", "val", "x")
        val result = instance(testDatum)
        assertEquals(ImmutableMap.of("ip2", "1", "ip1", "2", "port2", "p1", "port1", "p2", "val", "x"), result)
        assertEquals(testDatum, instance(instance(testDatum)))
    }

}
