package edu.jhuapl.data.parsnip.datum.compute

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * ChainTest.kt
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

import edu.jhuapl.data.parsnip.datum.DatumCompute
import edu.jhuapl.data.parsnip.value.compute.Add
import edu.jhuapl.data.parsnip.value.compute.As
import edu.jhuapl.data.parsnip.value.compute.Decode
import edu.jhuapl.testkt.shouldBe
import edu.jhuapl.testkt.recycleJsonTest
import org.junit.Test

class ChainTest {

    @Test
    fun testSerialize() {
        Chain<Any>().recycleJsonTest<DatumCompute<Any>>()
        Chain<Any>(Field("a")).recycleJsonTest<DatumCompute<Any>>()
        val t2 = Chain<Int>(Field("a"), Add(100)).recycleJsonTest<DatumCompute<Int>>() as Chain<*>
        t2.from::class.java shouldBe Field::class.java
        t2.process[0]::class.java shouldBe Add::class.java
    }

    @Test
    fun testInvoke() {
        Chain<Any>().invoke(mapOf("x" to 1)) shouldBe null
        Chain<Any>(Field("x"), As("String")).invoke(mapOf("x" to 1)) shouldBe "1"
        Chain<Any>(Field("x"), Decode("STRING")).invoke(mapOf("x" to 1)) shouldBe "1"
        Chain<Any>().invoke(mapOf("x" to 1)) shouldBe null
        Chain<Any>(Field("x"), As("String")).invoke(mapOf("x" to 1)) shouldBe "1"
    }

}
