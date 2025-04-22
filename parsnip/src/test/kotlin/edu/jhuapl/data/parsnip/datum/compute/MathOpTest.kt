package edu.jhuapl.data.parsnip.datum.compute

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * MathOpTest.kt
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

import edu.jhuapl.data.parsnip.datum.Datum
import edu.jhuapl.data.parsnip.datum.DatumCompute
import edu.jhuapl.data.parsnip.datum.compute.Operate.*
import edu.jhuapl.testkt.shouldBe
import edu.jhuapl.testkt.recycleJsonTest
import org.junit.Test
import java.util.*

class MathOpTest {

    @Test
    fun testSerialize() {
        MathOp().recycleJsonTest<DatumCompute<Any>>()
        MathOp(op = NEGATE, fields = listOf("a")).recycleJsonTest<DatumCompute<Any>>()
        MathOp(op = ADD, fields = listOf("a", "b", "c")).recycleJsonTest<DatumCompute<Any>>()
    }

    @Test
    fun testApply() {
        assertEquals(-1, mapOf("a" to 1, "b" to 2, "c" to null), NEGATE, "a", "b")
        assertEquals(2, mapOf("a" to 0, "b" to 2, "c" to null), ADD, "a", "b")
        assertEquals(-2, mapOf("a" to 0, "b" to 2, "c" to null), SUBTRACT, "a", "b")
        assertEquals(-5, mapOf("a" to 0, "b" to "2", "c" to "3"), SUBTRACT, "a", "b", "c")
        assertEquals(6, mapOf("a" to 3, "b" to 2, "c" to null), MULTIPLY, "a", "b")
        assertEquals(0, mapOf("a" to 0, "b" to 2, "c" to null), DIVIDE, "a", "b")
        assertEquals("invalid", mapOf("a" to 2, "b" to 0, "c" to null), DIVIDE, "a", "b")
        assertEquals(0, mapOf("a" to 0, "b" to 2, "c" to null), MIN, "a", "b")
        assertEquals(2, mapOf("a" to 0, "b" to 2, "c" to null), MAX, "a", "b")
        assertEquals(1.0, mapOf("a" to 0, "b" to "2.0", "c" to null), AVERAGE, "a", "b")
        assertEquals(1.0, mapOf("a" to 0, "b" to 2, "c" to null), STD_DEV, "a", "b")

        assertEquals("invalid", mapOf("a" to 2, "b" to 0, "c" to null), DIVIDE, "invalid", "a", "b")
        assertEquals("invalid", mapOf("a" to 0, "b" to "2", "c" to null), SUBTRACT, "invalid", "a", "b", "c")
    }

    @Test
    fun testApplyBools() {
        assertEquals(false, mapOf("a" to 0, "b" to 1), EQUAL, "a", "b")
        assertEquals(true, mapOf("a" to 0, "b" to 0), EQUAL, "a", "b")
        assertEquals(false, mapOf("a" to 0, "b" to 1), GT, "a", "b")
        assertEquals(true, mapOf("a" to 0, "b" to 1), GT, "b", "a")
    }

    @Test
    fun testApplyDates() {
        assertEquals(Date(0L), mapOf("a" to Date(0L), "b" to Date(6L), "c" to null), MIN, "a", "b")
        assertEquals(Date(6L), mapOf("a" to Date(0L), "b" to Date(6L), "c" to null), MAX, "a", "b")
        assertEquals(Date(3L), mapOf("a" to Date(0L), "b" to Date(6L), "c" to null), AVERAGE, "a", "b")
        assertEquals(-6L, mapOf("a" to Date(0L), "b" to Date(6L), "c" to null), SUBTRACT, "a", "b")
    }

    private fun assertEquals(res: Any, m: Datum, op: Operate, vararg fields: String) {
        MathOp(op, listOf(*fields), "invalid").invoke(m) shouldBe res
    }

}
