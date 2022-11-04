package edu.jhuapl.data.parsnip.datum.transform

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * FieldEncodeTest.kt
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

import edu.jhuapl.data.parsnip.datum.compute.Constant
import edu.jhuapl.data.parsnip.datum.compute.Field
import edu.jhuapl.data.parsnip.decode.StandardDecoders
import edu.jhuapl.data.parsnip.value.ValueCompute
import edu.jhuapl.data.parsnip.value.compute.ValueFilterCompute
import edu.jhuapl.data.parsnip.value.compute.As
import edu.jhuapl.data.parsnip.value.compute.Decode
import edu.jhuapl.data.parsnip.value.filter.NotEqual
import edu.jhuapl.testkt.shouldBe
import edu.jhuapl.testkt.shouldThrow
import junit.framework.TestCase
import org.junit.Test

class FieldEncodeTest : TestCase() {

    @Test
    fun testFieldEncodeConstructor() {
        { FieldEncode<Any>(listOf()) } shouldThrow IllegalArgumentException::class

        FieldEncode<Any>("f").apply {
            targetSingle shouldBe "f"
            targetMultipleFields shouldBe false
            target shouldBe listOf("f")
            from shouldBe Constant(null)
            process shouldBe listOf<ValueCompute<*>>()
            targetType shouldBe null
            cast shouldBe null
            decoder shouldBe null
        }

        FieldEncode<Any>("f", Field("x"), NotEqual(5)).apply {
            targetSingle shouldBe "f"
            targetMultipleFields shouldBe false
            target shouldBe listOf("f")
            from shouldBe Field("x")
            process.map { it.javaClass } shouldBe listOf<Class<*>>(ValueFilterCompute::class.java, As::class.java)
            targetType shouldBe Boolean::class.java
            cast shouldBe As(Boolean::class.java)
            decoder shouldBe null
        }

        FieldEncode<Any>("f", Field("x"), As("String")).apply {
            targetSingle shouldBe "f"
            from shouldBe Field("x")
            process shouldBe listOf<ValueCompute<*>>(As(String::class.java))
            targetType shouldBe String::class.java
            cast shouldBe As(String::class.java)
            decoder shouldBe null
        }

        FieldEncode<Any>("f", Field("x"), Decode("STRING")).apply {
            targetSingle shouldBe "f"
            from shouldBe Field("x")
            process shouldBe listOf<ValueCompute<*>>(Decode(StandardDecoders.STRING))
            targetType shouldBe String::class.java
            cast shouldBe null
            decoder shouldBe StandardDecoders.STRING
        }

        FieldEncode<Any>(listOf("f1", "f2")).apply {
            targetSingle shouldBe "f1"
            targetMultipleFields shouldBe true
            target shouldBe listOf("f1", "f2")
        }
    }

    @Test
    fun testFieldEncodeInvoke() {
        FieldEncode<Any>("f").invoke(mapOf("x" to 1)) shouldBe null
        FieldEncode<Any>("f", Field("x"), As("String")).invoke(mapOf("x" to 1)) shouldBe "1"
        FieldEncode<Any>("f", Field("x"), Decode("STRING")).invoke(mapOf("x" to 1)) shouldBe "1"
        FieldEncode<Any>(listOf("f1", "f2")).invoke(mapOf("x" to 1)) shouldBe null
        FieldEncode<Any>(listOf("f1", "f2"), Field("x"), As("String")).invoke(mapOf("x" to 1)) shouldBe "1"
    }

    @Test
    fun testFieldInvokeChange() {
        FieldEncode<Any>("f", Change(monitor = "x", groupBy = null)).invoke(mapOf("x" to 1)) shouldBe mapOf("x" to 1)

        val change = Change(null, "x", Transition(from = null, to = null, put = mapOf("change" to true)))
        FieldEncode<Any>("f", change).invoke(mapOf("x" to 1)) shouldBe mapOf("x" to 1, "change" to true)
        FieldEncode<Any>("f", change).invoke(mapOf("x" to 1)) shouldBe null
        FieldEncode<Any>("f", change).invoke(mapOf("x" to 2)) shouldBe mapOf("x" to 2, "change" to true)
        FieldEncode<Any>("f", change).invoke(mapOf("x" to 2)) shouldBe null
        FieldEncode<Any>("f", change).invoke(mapOf("x" to 2)) shouldBe null
        FieldEncode<Any>("f", change).invoke(mapOf("x" to 1)) shouldBe mapOf("x" to 1, "change" to true)
    }

    @Test
    fun testValueFilter() {
        FieldEncode<Any>("f", Field("x"), NotEqual(5)) .invoke(mapOf("x" to 1)) shouldBe true
        FieldEncode<Any>("f", Field("x"), NotEqual(5)) .invoke(mapOf("x" to 5)) shouldBe false
    }

}
