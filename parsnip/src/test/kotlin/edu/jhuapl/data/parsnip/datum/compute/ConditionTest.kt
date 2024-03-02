package edu.jhuapl.data.parsnip.datum.compute

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * ConditionTest.kt
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

import com.fasterxml.jackson.module.kotlin.readValue
import edu.jhuapl.data.parsnip.datum.DatumCompute
import edu.jhuapl.data.parsnip.io.ParsnipMapper
import edu.jhuapl.data.parsnip.value.filter.Gte
import edu.jhuapl.data.parsnip.value.filter.Lt
import edu.jhuapl.testkt.shouldBe
import edu.jhuapl.testkt.recycleJsonTest
import org.junit.Test

class ConditionTest {

    @Test
    fun testSerialize() {
        Condition(listOf()).recycleJsonTest<DatumCompute<Any?>>()
        Condition(listOf(
                Condition.ConditionMappingInst(mapOf("x" to Gte(5)), Field("a")),
                Condition.ConditionMappingInst(mapOf("x" to Lt(5)), Constant(1))
        )).recycleJsonTest<DatumCompute<Any?>>()

        val cond = ParsnipMapper.readValue<DatumCompute<*>>("""{"Condition":[{"when":{"x":{"Gte":5}}, "value":{"Constant": 5}}]}""")
        cond(mapOf<String, Any?>("x" to 0)) shouldBe null
        cond(mapOf<String, Any?>("x" to 10)) shouldBe 5
        cond.recycleJsonTest<DatumCompute<Any?>>()
    }

    @Test
    fun testApply() {
        with(Condition(listOf())) {
            invoke(mapOf()) shouldBe null
            invoke(mapOf("x" to 1, "a" to "yes", "b" to "no")) shouldBe null
        }

        with(Condition(listOf(
                Condition.ConditionMappingInst(mapOf("x" to Gte(5)), Field("a")),
                Condition.ConditionMappingInst(mapOf("x" to Lt(5)), Field("b"))
        ))) {
            invoke(mapOf()) shouldBe null
            invoke(mapOf("x" to 10, "a" to "yes", "b" to "no")) shouldBe "yes"
            invoke(mapOf("x" to 1, "a" to "yes", "b" to "no")) shouldBe "no"
        }
    }

}
