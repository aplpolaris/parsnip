package edu.jhuapl.data.parsnip.datum.transform

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * CreateTest.kt
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
import edu.jhuapl.data.parsnip.datum.compute.Field
import edu.jhuapl.data.parsnip.datum.compute.Template
import edu.jhuapl.data.parsnip.io.ParsnipMapper
import edu.jhuapl.data.parsnip.value.compute.As
import edu.jhuapl.data.parsnip.value.compute.OneHot
import edu.jhuapl.data.parsnip.value.filter.Gte
import edu.jhuapl.testkt.shouldBe
import edu.jhuapl.testkt.printJsonTest
import edu.jhuapl.testkt.recycleJsonTest
import edu.jhuapl.testkt.recycleYamlTest
import edu.jhuapl.testkt.testRecycle
import junit.framework.TestCase
import java.io.IOException

class CreateTest : TestCase() {

    fun testTest() {
        with(Create()) {
            val fe1 = FieldEncode<Any>("text", Template("{/a} {/b} and stuff"), As(String::class.java))
            fields.add(fe1)
            fields.add(FieldEncode<Any>("start", Field("Date"), As(Long::class.java)))

            this["text"] shouldBe fe1

            this(mapOf("a" to 1, "b" to "two")) shouldBe mapOf("text" to "1 two and stuff", "start" to null)
            this(mapOf("a" to 1)) shouldBe mapOf("text" to "1 null and stuff", "start" to null)
        }
    }

    @Throws(IOException::class)
    fun testNestedEncoding() {
        with(Create()) {
            fields.add(FieldEncode<Any>("text", Template("{/a} {/b}"), As(String::class.java)))
            fields.add(FieldEncode<Any>("/text2/nested", Template("{/a} {/b}"), As(String::class.java)))
            fields.add(FieldEncode<Any>("/text3/nested/more", Template("{/a} {/b}"), As(String::class.java)))

            invoke(mapOf("a" to 1, "b" to 2)) shouldBe mapOf("text" to "1 2", "text2" to mapOf("nested" to "1 2"), "text3" to mapOf("nested" to mapOf("more" to "1 2")))
        }
    }

    fun testMultipleTargetFields() {
        with (Create()) {
            fields.add(FieldEncode<Any>(listOf("a", "b", "c"), Field("alpha"), OneHot("a", "b", "c")))
            invoke(mapOf("alpha" to "a")) shouldBe mapOf("a" to 1, "b" to 0, "c" to 0)
            invoke(mapOf("alpha" to "b")) shouldBe mapOf("a" to 0, "b" to 1, "c" to 0)
            invoke(mapOf("alpha" to "d")) shouldBe mapOf("a" to 0, "b" to 0, "c" to 0)
        }
    }

    @Throws(IOException::class)
    fun testSerialize() {
        Create().printJsonTest()
        Create().recycleJsonTest()

        with (Create()) {
            fields.add(FieldEncode<Any>("text", Template("{/a} {/b} and stuff"), As(String::class.java)))
            fields.add(FieldEncode<Any>("start", Field("Date"), As(Long::class.java)))
            fields.add(FieldEncode<Any>(listOf("a", "b", "c"), Field("alpha"), OneHot("a", "b", "c")))
            fields.add(FieldEncode<Any>("nada"))
            fields.add(FieldEncode<Any>("flag", Field("number"), Gte(5)))
            printJsonTest()
            recycleJsonTest()
            recycleYamlTest()

            val create2 = testRecycle(this)
            create2.targetFields shouldBe setOf("text", "start", "a", "b", "c", "nada", "flag")
            create2.fields[2].targetMultipleFields shouldBe true

            recycleYamlTest().invoke(mapOf()) shouldBe mapOf("text" to "null null and stuff", "start" to null, "a" to 0, "b" to 0, "c" to 0, "nada" to null, "flag" to false)
        }
    }

    fun testDeserialize() {
        val create = """
            { 
              "t": {"Field": "x"},
              "flag": {"Field": "x", "Gte":"5"}
            }
        """.trimIndent()
        val createObj = ParsnipMapper.readValue<Create>(create)

        createObj.invoke(mapOf("x" to 1)) shouldBe mapOf("t" to 1, "flag" to false)
    }

}
