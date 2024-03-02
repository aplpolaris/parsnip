package edu.jhuapl.data.parsnip

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * EtlTest.kt
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
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import edu.jhuapl.data.parsnip.datum.compute.*
import edu.jhuapl.data.parsnip.io.ParsnipMapper
import edu.jhuapl.data.parsnip.datum.filter.DatumFieldFilter
import edu.jhuapl.data.parsnip.datum.transform.Change
import edu.jhuapl.data.parsnip.datum.transform.FieldEncode
import edu.jhuapl.data.parsnip.datum.transform.FlattenFields
import edu.jhuapl.data.parsnip.datum.transform.Transition
import edu.jhuapl.data.parsnip.value.compute.As
import edu.jhuapl.data.parsnip.value.compute.IpToInt
import edu.jhuapl.data.parsnip.value.compute.Lookup
import edu.jhuapl.data.parsnip.value.filter.*
import edu.jhuapl.testkt.shouldBe
import edu.jhuapl.testkt.prettyPrintJsonTest
import edu.jhuapl.testkt.prettyPrintYamlTest
import junit.framework.TestCase
import java.io.IOException

class EtlTest : TestCase() {

    @Throws(IOException::class)
    fun testSerialize() {
        val etl = Etl()
        etl.extract = DatumFieldFilter()
                .put("x", OneOf("a", 1, true))
                .put("a", Range(1, "3"))
                .put("b", IsCidr)
                .put("c", IsNotNull)
        etl.transform.add(FlattenFields("alpha", "beta"))
        etl.transform.add(Change("/body/sensor", "/body/state",
                Transition("off", "on", mapOf("_state" to "turned on"))))
        etl.load.fields.add(FieldEncode<String>("text", Template("{/a} {/b} and stuff"), As(String::class.java)))
        etl.load.fields.add(FieldEncode<String>("sensor", Constant("my sensor")))
        etl.load.fields.add(FieldEncode<Any>("state", Field("xx"), Lookup(mapOf("x" to 1, "y" to "two"))))
        etl.load.fields.add(FieldEncode<Int>("source", Field("source"), IpToInt))
        etl.load.fields.add(FieldEncode<Any>("conditional result", Condition(listOf(
                Condition.ConditionMappingInst(mapOf("x" to Gte(5)), Field("a")),
                Condition.ConditionMappingInst(mapOf("x" to Lt(5)), Field("b"))
        ))))

        etl.prettyPrintJsonTest()
        etl.prettyPrintYamlTest()

        val s = ParsnipMapper.writerWithDefaultPrettyPrinter().writeValueAsString(etl)

        val etl2 = ParsnipMapper.readValue<Etl>(s)
        etl2.extract.fieldFilters.size shouldBe 4
        etl2.extract.fieldFilters["c"]!!.javaClass shouldBe IsNotNull::class.java
        etl2.extract.fieldFilters["c"]!!.invoke(3) shouldBe true
        etl2.extract.fieldFilters["c"]!!.invoke(null) shouldBe false
        etl2.transform.size shouldBe 2
        etl2.load.fields.size shouldBe 5

        val s2 = ParsnipMapper.writeValueAsString(etl2)
        println(s2)

        val etl3 = ParsnipMapper.readValue(s2, Etl::class.java)
        val s3 = ParsnipMapper.writeValueAsString(etl3)
        assertEquals(s2, s3)
    }

    fun testDatesAsLongs() {
        val res = Etl()
        res.load.fields.add(FieldEncode<Long>("start", Field("Date"), As(Long::class.java)))
        res.load.fields.add(FieldEncode<String>("sensor", Constant("my sensor")))
        val input = mapOf("Date" to "09/11/1980 02:03", "SrcPort" to "25", "Sensor" to "a sensor")
        assertEquals(337500180000L, res(input)!!["start"])
    }

    fun testMapToObject() {
        val etl = Etl()
        etl.load.fields.add(FieldEncode<String>("/name/first", Field("a")))
        etl.load.fields.add(FieldEncode<String>("/name/last", Field("b")))
        etl.load.fields.add(FieldEncode<String>("/hairColor", Field("c")))

        val input = mapOf("a" to "Albert", "b" to "Einstein", "c" to "gray")
        val output = etl.invoke(input)
        etl.prettyPrintYamlTest()
        println(output)

        val person = ObjectMapper().registerModule(KotlinModule()).convertValue(output, Person::class.java)
        println(person)
    }

    data class Person(var name: PersonName, var hairColor: String)
    data class PersonName(var first: String, var last: String)

}
