package edu.jhuapl.util.types

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * IpsTest.kt
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
import junit.framework.TestCase
import org.junit.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IpsTest : TestCase() {

    @Test
    fun testTest() {
        println("test")

        assertIpMatch("1.2.3.4")
        assertIpMatch("1.2.3.004")
        assertIpMatch("127.0.0.1")
        assertIpMatch("192.168.1.1")
        assertIpMatch("192.168.1.255")
        assertIpMatch("255.255.255.255")
        assertIpMatch("0.0.0.0")
        assertIpMatch("1.1.1.01")

        assertIpFail("1.2.3.400")
        assertIpFail("1.2.3.4.5")
        assertIpFail("1.2.3.00004")
        assertIpFail("1.2.3.a")
        assertIpFail("1.2.3. 4")
        assertIpFail("30.168.1.255.1")
        assertIpFail("127.1")
        assertIpFail("192.168.1.256")
        assertIpFail("-1.2.3.4")
        assertIpFail("3...3")
    }

    @Test
    fun testContains() {
        println("contains")
        assertTrue("1.2.3.0/24".cidrContains("1.2.3.0"))
        assertFalse("1.2.3.0/24".cidrContains("1.2.4.0"))
        assertFailsWith(IllegalArgumentException::class) { "1.2.3.0/24".cidrContains("eggs") }
    }

    @Test
    fun testToInt() {
        ipToInt("0.0.0.0") shouldBe 0
        ipToInt("0.0.0.1") shouldBe 1
        ipToInt("1.0.0.0") shouldBe (1 shl 24)
    }

    @Test
    fun testToString() {
        println("toString")
        ipFromInt(1 shl 8) shouldBe "0.0.1.0"
    }
}

private fun assertIpMatch(s: String) = assertTrue { s.isIpv4() }
private fun assertIpFail(s: String) = assertFalse { s.isIpv4() }
