package edu.jhuapl.data.parsnip.datum.transform

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * FieldChangesTest.kt
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

import edu.jhuapl.data.parsnip.datum.DatumTransform
import edu.jhuapl.testkt.shouldBe
import edu.jhuapl.util.internal.recycleJsonTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FieldChangesTest {

    @Test
    fun testSerialize() {
        RetainFields().recycleJsonTest<DatumTransform>()

        RemoveFields().recycleJsonTest<DatumTransform>()
        RemoveFields("a").recycleJsonTest<DatumTransform>()

        FlattenFields().recycleJsonTest<DatumTransform>()
        FlattenFields("a", "b", "c").recycleJsonTest<DatumTransform>()
    }

    @Test
    fun testRetainFields() {
        val test = mapOf("a" to 1, "b" to 2)
        RetainFields().invoke(test) shouldBe mutableMapOf<String, Any?>()
        RetainFields("a").invoke(test) shouldBe mutableMapOf("a" to 1)
    }

    @Test
    fun testRemoveFields() {
        val test = mapOf("a" to 1, "b" to 2)
        RemoveFields().invoke(test) shouldBe mutableMapOf("a" to 1, "b" to 2)
        RemoveFields("a").invoke(test) shouldBe mutableMapOf("b" to 2)
    }

    @Test
    fun testFlattenFields() {
        val test = mapOf("key" to 1, "key2" to mapOf("a" to "123", "b" to "456"))
        assertEquals(mapOf("key" to 1, "key2" to mapOf("a" to "123", "b" to "456")), flattenMap(test, "none"))
        assertEquals(mapOf("key" to 1, "key2.a" to "123", "key2.b" to "456"), flattenMap(test, "key2"))
        assertEquals(mapOf("key" to 1, "key2.a" to "123", "key2.b" to "456"), flattenMap(test, "key2", "a"))

        val test2 = mapOf("key" to 1, "key2" to mapOf("a" to mapOf("r" to 5, "b" to 456)))
        assertEquals(mapOf("key" to 1, "key2.a.r" to 5, "key2.a.b" to 456), flattenMap(test2, "key2", "a"))
    }

    private fun flattenMap(input: Map<String, Any>, vararg fields: String): Map<String, Any?> {
        return FlattenFields(*fields).invoke(input)
    }

}
