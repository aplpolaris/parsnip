package edu.jhuapl.data.parsnip

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * BatchEtlTest.kt
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

import com.fasterxml.jackson.module.kotlin.readValue
import edu.jhuapl.data.parsnip.datum.asMultiDatumTransform
import edu.jhuapl.data.parsnip.io.ParsnipMapper
import edu.jhuapl.data.parsnip.datum.filter.DatumFieldFilter
import edu.jhuapl.data.parsnip.datum.compute.Constant
import edu.jhuapl.data.parsnip.datum.compute.Field
import edu.jhuapl.data.parsnip.datum.compute.Template
import edu.jhuapl.data.parsnip.datum.transform.Change
import edu.jhuapl.data.parsnip.datum.transform.FieldEncode
import edu.jhuapl.data.parsnip.datum.transform.FlattenFields
import edu.jhuapl.data.parsnip.datum.transform.Transition
import edu.jhuapl.data.parsnip.value.compute.As
import edu.jhuapl.data.parsnip.value.compute.Lookup
import edu.jhuapl.data.parsnip.value.filter.IsCidr
import edu.jhuapl.data.parsnip.value.filter.OneOf
import edu.jhuapl.data.parsnip.value.filter.Range
import edu.jhuapl.testkt.shouldBe
import edu.jhuapl.testkt.prettyPrintJsonTest
import edu.jhuapl.testkt.prettyPrintYamlTest
import junit.framework.TestCase
import java.io.IOException

class BatchEtlTest : TestCase() {

    @Throws(IOException::class)
    fun testSerialize() {
        val etl = BatchEtl()
        etl.extract = DatumFieldFilter()
                .put("x", OneOf("a", 1, true))
                .put("a", Range(1, "3"))
                .put("b", IsCidr)
        etl.transform.add(FlattenFields("alpha", "beta").asMultiDatumTransform())
        etl.transform.add(Change("/body/sensor", "/body/state",
                Transition("off", "on", mapOf("_state" to "turned on"))).asMultiDatumTransform())
        etl.load.fields.add(FieldEncode<Any>("text", Template("{/a} {/b} and stuff"), As(String::class.java)))
        etl.load.fields.add(FieldEncode<Any>("sensor", Constant("my sensor")))
        etl.load.fields.add(FieldEncode<Any>("state", Field("xx"), Lookup(mapOf("x" to 1, "y" to "two"))))

        etl.prettyPrintJsonTest()
        etl.prettyPrintYamlTest()
        val s = ParsnipMapper.writerWithDefaultPrettyPrinter().writeValueAsString(etl)

        val etl2 = ParsnipMapper.readValue<BatchEtl>(s)
        etl2.extract.fieldFilters.size shouldBe 3
        etl2.transform.size shouldBe 2
        etl2.load.fields.size shouldBe 3

        val s2 = ParsnipMapper.writeValueAsString(etl2)
        println(s2)

        val etl3 = ParsnipMapper.readValue(s2, BatchEtl::class.java)
        val s3 = ParsnipMapper.writeValueAsString(etl3)
        s3 shouldBe s2
    }

}
