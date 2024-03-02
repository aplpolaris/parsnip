package edu.jhuapl.data.parsnip.datum.transform

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * ChangeTest.kt
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

import edu.jhuapl.testkt.shouldBe
import edu.jhuapl.testkt.printPlainMapperJsonTest
import edu.jhuapl.testkt.recyclePlainMapperJsonTest
import junit.framework.TestCase
import java.io.IOException

class ChangeTest : TestCase() {

    @Throws(IOException::class)
    fun testSerialize() {
        Change("a", "b").printPlainMapperJsonTest()
        Change("a", "b").recyclePlainMapperJsonTest()

        val inst = Change(null, "/path/to/field",
                Transition("off", "on", mapOf("trigger" to "turned on")),
                Transition("on", "off", mapOf("trigger" to "turned off")))
        inst.printPlainMapperJsonTest()
        inst.recyclePlainMapperJsonTest()
    }

    fun testInvoke() {
        val inst2 = Change(null, "b")
        inst2(mapOf("b" to "x"))!!["b"] shouldBe "x"
        inst2(mapOf("b" to "x")) shouldBe null
        inst2(mapOf("b" to "y"))!!["b"] shouldBe "y"

        val inst = Change(null, "b",
                Transition("off", "on", mapOf("trigger" to "turned on")),
                Transition("on", "off", mapOf("trigger" to "turned off")))
        inst(mapOf("b" to "off")) shouldBe null
        inst(mapOf("b" to "off")) shouldBe null
        inst(mapOf("b" to "on"))!!["trigger"] shouldBe "turned on"
        inst(mapOf("b" to "off"))!!["trigger"] shouldBe "turned off"
        inst(mapOf("b" to "c")) shouldBe null
        inst(mapOf("b" to "on")) shouldBe null
    }

}
