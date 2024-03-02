package edu.jhuapl.data.parsnip.datum.compute

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * TemplateTest.kt
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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import edu.jhuapl.util.internal.printJsonTest
import junit.framework.TestCase
import org.junit.Test
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime

class TemplateTest : TestCase() {

    @Test
    fun testSerialize() {
        Template("").printJsonTest()
        Template("{/a} then {/b/c}").printJsonTest()

        println(ObjectMapper().convertValue<Template>("{/a} then {/b/c}").simpleValue)
        println(jacksonObjectMapper().convertValue<Template>("{/a} then {/b/c}").simpleValue)
    }

    @Test
    fun testApply() {
        val tf = Template("{/a} then {/b/c}")

        assertEquals("null then null", tf(emptyMap<String, Any>()))
        assertEquals("one then null", tf(mapOf("a" to "one")))
        assertEquals("one then two", tf(mapOf("a" to "one", "b" to mapOf("c" to "two"))))

        val tf2 = Template("/a;/b")
        assertEquals("one", tf2(mapOf("a" to "one")))
        assertEquals("two", tf2(mapOf("b" to "two")))
        assertEquals("one", tf2(mapOf("a" to "one", "b" to "two")))
        assertEquals(null, tf2(emptyMap<String, Any>()))

        val inst = ZonedDateTime.of(2000, 1, 1, 0, 0, 0, 0, UTC)
        assertEquals("2000-01-01T00:00Z then null", tf(mapOf("a" to inst)))
    }

}
