package edu.jhuapl.data.schema

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * DataSchemaTest.kt
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
import edu.jhuapl.data.parsnip.schema.schema
import edu.jhuapl.testkt.shouldBe
import edu.jhuapl.testkt.recycleJsonTest
import junit.framework.TestCase

fun sampleSchema() = schema("event") {
    string("source")
    string("destination")
    long("start")
    long("stop")
    string("type")
    string("subtype")
    string("schema")
}

class DataSchemaTest : TestCase() {

    @Throws(Exception::class)
    fun testSerializeToJson() {
        sampleSchema().recycleJsonTest()
    }

    fun testFields() {
        val schema = sampleSchema()
        schema.fieldNames shouldBe mutableSetOf("source", "destination", "start", "stop", "type", "subtype", "schema")
        schema.numericFields.keys shouldBe mutableSetOf("start", "stop")
        schema.categoricalFields.keys shouldBe mutableSetOf("source", "destination", "type", "subtype", "schema")
    }

//    @Throws(Exception::class)
//    fun testReadSchema() {
//        val path: URL = DataImporterTest::class.java.getResource("resources/indicator_schema.json")
//        val res = ParsnipMapper.readValue(path, DataSchema::class.java)
//        assertEquals(9, res.fields.size)
//        println(res)
//    }

//    @Throws(Exception::class)
//    fun testFrom_Datum() {
//        val d: Datum = MapDatum.of("a", 1, "b", "string", "c", true)
//        val ds: DataSchema = DataSchema.from(d)
//        assertEquals(Arrays.asList<Any>(
//                FieldSchema("a", Int::class.java),
//                FieldSchema("b", String::class.java),
//                FieldSchema("c", Boolean::class.java)), ds.fields)
//    }
}
