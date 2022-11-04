package edu.jhuapl.data.parsnip.datum.compute

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * FieldTest.kt
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
import edu.jhuapl.util.internal.printJsonTest
import junit.framework.TestCase
import org.junit.Test
import java.io.IOException

class FieldTest : TestCase() {

    @Test
    @Throws(IOException::class)
    fun testSerialize() {
        Field("a").printJsonTest()
    }

    @Test
    fun testApply() {
        val field = Field("a")
        field(mapOf("a" to 0, "b" to 2, "c" to null)) shouldBe 0
        field(mapOf("a" to null, "b" to 2, "c" to null)) shouldBe null
        field(mapOf("b" to 2, "c" to null)) shouldBe null
    }
}
