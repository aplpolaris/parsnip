package edu.jhuapl.data.parsnip.datum.compute

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * CalculateTest.kt
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
import com.googlecode.blaisemath.parser.BooleanGrammar
import com.googlecode.blaisemath.parser.RealGrammar
import edu.jhuapl.util.internal.printJsonTest
import edu.jhuapl.util.internal.recycleJsonTest
import junit.framework.TestCase
import org.junit.Test
import kotlin.test.assertFails

class CalculateTest : TestCase() {

    @Test
    fun testSerialize() {
        Calculate("").printJsonTest()
        Calculate("{/a} + {/b/c}").printJsonTest()

        println(ObjectMapper().convertValue<Calculate>("{/a} + {/b/c}").simpleValue)
        println(jacksonObjectMapper().convertValue<Calculate>("{/a} + {/b/c}").simpleValue)
    }

    @Test
    fun testInvalid() {
        Calculate("{/ not closed")
        Calculate("{ not a template }")
        Calculate("%%--+*not math")
    }

    @Test
    fun testGrammar() {
        assertFails { RealGrammar.getParser().parseTree("true or false").value }
        assertEquals(true, RealGrammar.getParser().parseTree("7 > 5").value) // this is a library fluke
        assertFails { RealGrammar.getParser().parseTree("true or (5 > 7)").value }
        assertFails { RealGrammar.getParser().parseTree("(1 > 0) or (7 > 5)").value }

        assertEquals(true, BooleanGrammar.getParser().parseTree("true or false").value)
        assertFails { BooleanGrammar.getParser().parseTree("7 > 5").value }

        assertFails { BooleanGrammar.getParser().parseTree("true or (5 > 7)").value }
    }

    @Test
    fun testBoolean() {
        assertEquals(null, Calculate("{/a}").invoke(mapOf("a" to true)))
        assertEquals(null, Calculate("{/a} or {/b}").invoke(mapOf("a" to true, "b" to false)))
        assertEquals(null, Calculate("{/a} and {/b}").invoke(mapOf("a" to true, "b" to false)))

        assertEquals(true, CalculateBoolean("true").invoke(mapOf("a" to 1)))
        assertEquals(true, CalculateBoolean("true or false").invoke(mapOf("a" to 1)))
        assertEquals(null, CalculateBoolean("2 > 1").invoke(mapOf("a" to 2)))

        assertEquals(true, CalculateBoolean("{/a}").invoke(mapOf("a" to true)))
        assertEquals(true, CalculateBoolean("{/a} or {/b}").invoke(mapOf("a" to true, "b" to false)))
        assertEquals(false, CalculateBoolean("{/a} and {/b}").invoke(mapOf("a" to true, "b" to false)))
        assertEquals(null, CalculateBoolean("{/a} > 0").invoke(mapOf("a" to 1))) // no support for mixed parsing
    }

    @Test
    fun testApply() {
        assertEquals(2.0, Calculate("{/a}").invoke(mapOf("a" to 2)))
        assertEquals(2.0, Calculate("{a}").invoke(mapOf("a" to 2)))
        assertEquals(null, Calculate("{a}").invoke(mapOf("a" to null)))

        val tf = Calculate("{/a} + {/b/c}")
        assertEquals(null, tf(emptyMap<String, Any>()))
        assertEquals(null, tf(mapOf("a" to 1)))
        assertEquals(3.0, tf(mapOf("a" to 1, "b" to mapOf("c" to "2"))))

        val tf2 = Calculate("{/a} + 2")
        assertEquals(null, tf2(emptyMap<String, Any>()))
        assertEquals(3.0, tf2(mapOf("a" to 1)))

        assertEquals(3.0, tf2(mapOf("a" to "100%")))
        assertEquals(1002.0, tf2(mapOf("a" to "1,000")))
        assertEquals(2.0, tf2(mapOf("a" to "-")))
    }

    @Test
    fun testApply2() {
        val tf3 = Calculate("{/Cases - last 7 days}/7")
        assertEquals(null, tf3(emptyMap<String, Any>()))
        assertEquals(0.0, tf3(mapOf("Cases - last 7 days" to 0)))
        assertEquals(1.0, tf3(mapOf("Cases - last 7 days" to 7)))
        assertEquals(null, tf3(mapOf("Cases - last 7 days" to null)))
    }

}
