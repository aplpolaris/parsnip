/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * ObjectPointersTest.kt
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
package edu.jhuapl.util.types

import edu.jhuapl.testkt.shouldBe
import org.junit.Test
import java.awt.Point

class ObjectPointersTest {

    @Test
    fun testAtPointer() {
        Point(1, 2).atPointer("x") shouldBe 1.0
        Point(1, 2).atPointer("/x") shouldBe 1.0
        Point(1, 2).atPointer("y") shouldBe 2.0
        Point(1, 2).atPointer("z") shouldBe null
    }

    @Test
    fun testNestedPut() {
        val map = mutableMapOf<String, Any?>()
        map.nestedPut("a", 1)
        map shouldBe mutableMapOf("a" to 1)
        map.nestedPut("/b/c", 1)
        map shouldBe mutableMapOf("a" to 1, "b" to mutableMapOf("c" to 1))
        map.nestedPut("/b/c", 2)
        map shouldBe mutableMapOf("a" to 1, "b" to mutableMapOf("c" to 2))
        map.nestedPut("/b/d/e", 1)
        map shouldBe mutableMapOf("a" to 1, "b" to mutableMapOf("c" to 2, "d" to mutableMapOf("e" to 1)))
    }

    @Test
    fun testNestedPutArrays() {
        val map = mutableMapOf<String, Any?>("b" to listOf(mutableMapOf("x" to 1)))
        map.nestedPut("/b/0/x", 2)
        map shouldBe mutableMapOf("b" to listOf(mutableMapOf("x" to 2)))
    }

}
