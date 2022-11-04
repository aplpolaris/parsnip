package edu.jhuapl.data.parsnip.value.compute

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * IpComputeTest.kt
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

class IpComputeTest : TestCase() {

    @Test
    @Throws(IOException::class)
    fun testSerialize() {
        IpToInt.recycleJsonTest<ValueCompute<Int?>>()
        IpFromInt.recycleJsonTest<ValueCompute<String>>()
    }

    @Test
    fun testInvoke() {
        IpToInt("0.0.0.0") shouldBe 0
        IpFromInt(4) shouldBe "0.0.0.4"
    }

}
