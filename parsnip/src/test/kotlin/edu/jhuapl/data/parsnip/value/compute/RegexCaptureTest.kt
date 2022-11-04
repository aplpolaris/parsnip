package edu.jhuapl.data.parsnip.value.compute

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * RegexCaptureTest.kt
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

import edu.jhuapl.testkt.shouldBe
import edu.jhuapl.util.internal.printPlainMapperJsonTest
import junit.framework.TestCase
import org.junit.Test
import java.io.IOException

class RegexCaptureTest : TestCase() {

    @Test
    @Throws(IOException::class)
    fun testSerialize() {
        RegexCapture("(.*)", "x").printPlainMapperJsonTest()
        RegexCapture("([0-9]*)\\.([0-9]*)", listOf("x", "y")).printPlainMapperJsonTest()
    }

    @Test
    fun testInvoke() {
        RegexCapture("([0-9]*)\\.([0-9]*)", listOf("x", "y")).invoke("123.456") shouldBe mapOf("x" to "123", "y" to "456")
        RegexCapture("([0-9]*)\\.([0-9]*)", listOf("x")).invoke("123.456") shouldBe mapOf("x" to "123")
        RegexCapture("([0-9]*)\\.([0-9]*)", listOf("", "y")).invoke("123.456") shouldBe mapOf("y" to "456")
    }

    @Test
    fun `test bad regex`() {
        RegexCapture("(").invoke("") shouldBe null
        RegexCapture("([0-9]*)\\.([0-9]*)", listOf("x", "y")).invoke("123") shouldBe null
    }

}
