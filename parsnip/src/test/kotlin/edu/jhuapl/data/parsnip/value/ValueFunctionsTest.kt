package edu.jhuapl.data.parsnip.value

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * ValueFunctionsTest.kt
 * edu.jhuapl.data:parsnip
 * %%
 * Copyright (C) 2019 - 2025 Johns Hopkins University Applied Physics Laboratory
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
import edu.jhuapl.data.parsnip.io.ParsnipMapper
import edu.jhuapl.data.parsnip.value.filter.*
import edu.jhuapl.testkt.shouldBe
import edu.jhuapl.testkt.recycleJsonTest
import edu.jhuapl.util.types.ObjectOrdering
import junit.framework.TestCase
import java.io.IOException
import java.lang.IllegalArgumentException
import kotlin.test.assertFailsWith

class ValueFunctionsTest : TestCase() {

    fun testCompare() {
        assertEquals(0, ObjectOrdering.compare("a", "a"))
        assertEquals(-1, ObjectOrdering.compare("a", "b"))
    }

    fun testEqual() {
        assertEquals(true, Equal(null).invoke(null))
        assertEquals(false, Equal(10).invoke(null))
        assertEquals(true, Equal(10).invoke(10))
        assertEquals(true, Equal(10).invoke(10.0))
        assertEquals(true, Equal(10).invoke("10"))
        assertEquals(false, Equal(10).invoke("101"))
    }

    fun testNotEqual() {
        assertEquals(false, NotEqual(null).invoke(null))
        assertEquals(true, NotEqual(10).invoke(null))
        assertEquals(false, NotEqual(10).invoke(10))
        assertEquals(false, NotEqual(10).invoke(10.0))
        assertEquals(false, NotEqual(10)("10"))
        assertEquals(true, NotEqual(10).invoke("101"))
    }

    fun testNot() {
        assertEquals(false, Not(Equal(null)).invoke(null))
        assertEquals(true, Not(Equal(10)).invoke(null))
        assertEquals(false, Not(Equal(10)).invoke(10))
        assertEquals(false, Not(Equal(10)).invoke(10.0))
        assertEquals(false, !Equal(10)("10"))
        assertEquals(true, Not(Equal(10)).invoke("101"))
    }

    fun testAndGteLte() {
        assertEquals(false, (Gte("a") and Lte("b"))(null))
        assertEquals(true, And(Gte("a"), Lte("b")).invoke("a"))
        assertEquals(true, And(Gte("a"), Lte("b")).invoke("ab"))
        assertEquals(false, And(Gte("a"), Lte("b")).invoke("ba"))
    }

    fun testOrGtLt() {
        assertEquals(false, Or(Lt("b"), Lt(4)).invoke(null))
        assertEquals(true, Or(Lt("b"), Lt(4)).invoke("a"))
        assertEquals(true, (Lt("b") or Lt(4))(3))
        assertEquals(false, Or(Lt("b"), Lt(4)).invoke(4))
    }

    fun testContains() {
        assertEquals(false, Contains("test").invoke(null))
        assertEquals(true, Contains("test").invoke("test"))
        assertEquals(true, Contains("test").invoke("testing"))
        assertEquals(false, Contains("test").invoke("Testing"))
    }

    fun testOneOf() {
        assertEquals(false, OneOf(1, "2", "three").invoke(null))
        assertEquals(true, OneOf(1, "2", "three").invoke(1))
        assertEquals(true, OneOf(1, "2", "three").invoke("1"))
        assertEquals(true, OneOf(1, "2", "three").invoke(2))
        assertEquals(true, OneOf(1, "2", "three").invoke("three"))
        assertEquals(false, OneOf(1, "2", "three").invoke("more than three"))
    }

    fun testRange() {
        assertEquals(false, Range(1.0, 6.6).invoke(null))
        assertEquals(true, Range(1.0, 6.6).invoke(1.0))
        assertEquals(true, Range(1.0, 6.6).invoke(1))
        assertEquals(true, Range(1.0, 6.6).invoke(4.0))
        assertEquals(false, Range(1.0, 6.6).invoke(8.0))
        assertEquals(true, Range(1.0, 6.6).invoke("2.2"))
    }

    fun testTestIP() {
        IsIP(null) shouldBe false
        assertEquals(true, IsIP("1.1.1.1"))
        assertEquals(false, IsIP("Mock Invalid"))
    }

    fun testTestCidr() {
        IsCidr(null) shouldBe false
        assertEquals(true, IsCidr("1.1.1.1/24"))
        assertEquals(false, IsCidr("Mock Invalid)"))
    }

    fun testIpContainedIn() {
        // this test will throw an NPE if called from Java; in Kotlin it cannot be called
//        assertFailsWith<NullPointerException> { IpContainedIn(null).invoke("1.1.1.0") }
        IpContainedIn("1.1.1.1/24").invoke(null) shouldBe false
        IpContainedIn("1.1.1.1/24").invoke("Mock Invalid") shouldBe false
        assertFailsWith<IllegalArgumentException> { IpContainedIn("Mock Invalid").invoke("1.1.1.0") }
        assertEquals(true, IpContainedIn("1.1.1.1/24").invoke("1.1.1.0"))
        assertEquals(false, IpContainedIn("1.1.1.1/24").invoke("1.1.0.0"))
    }

    fun testCidrContains() {
        // this test will throw an NPE if called from Java; in Kotlin it cannot be called
//        assertFailsWith<NullPointerException> { CidrContains(null).invoke("1.1.1.1/24") }
        CidrContains("1.1.1.0").invoke(null) shouldBe false
        CidrContains("1.1.1.0").invoke("Mock Invalid") shouldBe false
        assertFailsWith<IllegalArgumentException> { CidrContains("Mock Invalid").invoke("1.1.1.1/24") }
        assertEquals(true, CidrContains("1.1.1.0").invoke("1.1.1.1/24"))
        assertEquals(false, CidrContains("1.1.0.0").invoke("1.1.1.1/24"))
    }

    fun testEqualDeserialization() {
        val equalEtlObj = Equal(10)
        val equalEtlOutputStr = ParsnipMapper.writeValueAsString(equalEtlObj)
        val equalEtlObjRecreated = ParsnipMapper.readValue(equalEtlOutputStr, ValueFilter::class.java)
        assertEquals(true, equalEtlObjRecreated.invoke("10"))
    }

    fun testNotDeserialization() {
        val notEtlObj = Not(Equal(10))
        val notEtlOutputStr = ParsnipMapper.writeValueAsString(notEtlObj)
        val notEtlObjRecreated = ParsnipMapper.readValue(notEtlOutputStr, ValueFilter::class.java)
        assertEquals(true, notEtlObjRecreated.invoke(101))
    }

    fun testAndGteLteDeserialization() {
        val andGteLteEtlObj = And(Gte("a"), Lte("b"))
        val andGteLteEtlOutputStr = ParsnipMapper.writeValueAsString(andGteLteEtlObj)
        val andGteLteEtlObjRecreated = ParsnipMapper.readValue(andGteLteEtlOutputStr, ValueFilter::class.java)
        assertEquals(true, andGteLteEtlObjRecreated.invoke("a"))
    }

    fun testOrGtLtDeserialization() {
        val orGtLtEtlObj = Or(Lt("b"), Lt(4))
        val orGtLtEtlOutputStr = ParsnipMapper.writeValueAsString(orGtLtEtlObj)
        val orGtLtEtlObjRecreated = ParsnipMapper.readValue(orGtLtEtlOutputStr, ValueFilter::class.java)
        assertEquals(true, orGtLtEtlObjRecreated.invoke("a"))
    }

    fun testContainsDeserialization() {
        val containsEtlObj = Contains("test")
        val containsEtlOutputStr = ParsnipMapper.writeValueAsString(containsEtlObj)
        val containsEtlObjRecreated = ParsnipMapper.readValue(containsEtlOutputStr, ValueFilter::class.java)
        assertEquals(true, containsEtlObjRecreated.invoke("testing"))
    }

    fun testOneOfDeserialization() {
        val oneOfEtlObj = OneOf(1, "2", "three")
        val oneOfEtlOutputStr = ParsnipMapper.writeValueAsString(oneOfEtlObj)
        val oneOfEtlObjRecreated = ParsnipMapper.readValue(oneOfEtlOutputStr, ValueFilter::class.java)
        assertEquals(true, oneOfEtlObjRecreated.invoke(2))
    }

    fun testRangeDeserialization() {
        val rangeEtlObj = Range(1.0, 6.6)
        val rangeEtlOutputStr = ParsnipMapper.writeValueAsString(rangeEtlObj)
        val rangeEtlObjRecreated = ParsnipMapper.readValue(rangeEtlOutputStr, ValueFilter::class.java)
        assertEquals(true, rangeEtlObjRecreated.invoke(2.0))
    }

    @Throws(IOException::class)
    fun testSerialize() {
        testSerialize(Equal(10))
        testSerialize(!Equal(10))
        testSerialize(Gte("a") and Lte("b"))
        testSerialize(Lt("b") or Lt(4))
        testSerialize(Contains("test"))
        testSerialize(OneOf(1, 2, "three"))
        testSerialize(Range(1.0, 6.6))

        testSerialize(IsIP)
        testSerialize(IsCidr)
        testSerialize(IpContainedIn("1.1.1.1/24"))
        testSerialize(CidrContains("1.1.1.1"))

        println(ObjectMapper().convertValue(arrayOf(1, 2), Range::class.java))
    }

    @Throws(IOException::class)
    private fun testSerialize(vf: ValueFilter) {
        vf.recycleJsonTest()
    }

}
