package edu.jhuapl.data.parsnip.value.compute

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * MathTest.kt
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

import edu.jhuapl.data.parsnip.value.ValueCompute
import edu.jhuapl.testkt.shouldBe
import edu.jhuapl.util.internal.recycleJsonTest
import junit.framework.TestCase
import org.junit.Test
import java.io.IOException

class MathTest : TestCase() {

    @Test
    @Throws(IOException::class)
    fun testSerialize() {
        Multiply(100).recycleJsonTest<ValueCompute<Any>>()
        Add(50.0).recycleJsonTest<ValueCompute<Any>>()
        Subtract(40L).recycleJsonTest<ValueCompute<Any>>()
        Divide(2).recycleJsonTest<ValueCompute<Any>>()

        Linear().recycleJsonTest<ValueCompute<Double>>()
        Linear(1 to 2, 2 to 5).recycleJsonTest<ValueCompute<Double>>()
    }

    @Test
    fun testOperate() {
        Multiply(100)("2.0") shouldBe 200.0
        Add(10)(10) shouldBe 20
        Subtract(5)(2) shouldBe -3
        Divide(13)(20L) shouldBe 1L
    }

    @Test
    fun testLinear() {
        val func = Linear(1 to 2, 2 to 5)
        func(1) shouldBe 2.0
        func(1.5) shouldBe 3.5
        func(2.0) shouldBe 5.0
        func("2") shouldBe 5.0
        func("a") shouldBe null
    }

}
