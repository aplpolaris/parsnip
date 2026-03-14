package edu.jhuapl.data.parsnip

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * EtlIntegrationTest.kt
 * edu.jhuapl.data:parsnip
 * %%
 * Copyright (C) 2019 - 2026 Johns Hopkins University Applied Physics Laboratory
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
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.readValue
import edu.jhuapl.data.parsnip.datum.Datum
import edu.jhuapl.data.parsnip.datum.compute.Field
import edu.jhuapl.data.parsnip.datum.filter.DatumFieldFilter
import edu.jhuapl.data.parsnip.datum.transform.*
import edu.jhuapl.data.parsnip.io.ParsnipMapper
import edu.jhuapl.data.parsnip.io.parsnipModule
import edu.jhuapl.data.parsnip.value.filter.Equal
import edu.jhuapl.data.parsnip.value.filter.Gte
import edu.jhuapl.data.parsnip.value.filter.Lt
import edu.jhuapl.data.parsnip.value.filter.Range
import edu.jhuapl.testkt.shouldBe
import junit.framework.TestCase
import java.io.File

/**
 * Integration test that exercises a complete ETL workflow:
 * 1. Reads sample employee data from a JSONL input file
 * 2. Loads an ETL pipeline configured in YAML with five distinct steps:
 *    - Step 1 (extract): filter for active employees only
 *    - Step 2 (transform): add a "dept_code" field via conditional department-to-code mapping
 *    - Step 3 (transform): add a "salary_band" field via conditional salary-range mapping
 *    - Step 4 (transform): remove the "status" field
 *    - Step 5 (load): create the final output with selected fields in a defined order
 * 3. Executes the multi-step pipeline on each record
 * 4. Writes the transformed records to an output JSONL file
 * 5. Validates the final result against expected output
 */
class EtlIntegrationTest : TestCase() {

    private val resourceDir = "src/test/resources/etl-integration"
    private val inputFile = "$resourceDir/input.jsonl"
    private val pipelineFile = "$resourceDir/pipeline.yaml"
    private val expectedOutputFile = "$resourceDir/expected-output.jsonl"

    companion object {
        private val objectMapper = ObjectMapper()
    }

    /** Builds the ETL pipeline with five distinct steps using the Parsnip API. */
    private fun buildPipeline(): Etl {
        val etl = Etl()

        // Step 1 (extract): filter for active employees only
        etl.extract = DatumFieldFilter().put("status", Equal("active"))

        // Step 2 (transform): add dept_code via conditional mapping of department to numeric code
        etl.transform.add(Mapping(listOf(
            Mapping.MappingInst(mapOf("department" to Equal("Engineering")), mapOf("dept_code" to 1)),
            Mapping.MappingInst(mapOf("department" to Equal("Marketing")), mapOf("dept_code" to 2)),
            Mapping.MappingInst(mapOf("department" to Equal("HR")), mapOf("dept_code" to 3))
        )))

        // Step 3 (transform): add salary_band via conditional mapping of salary ranges
        etl.transform.add(Mapping(listOf(
            Mapping.MappingInst(mapOf("salary" to Lt(70000)), mapOf("salary_band" to "junior")),
            Mapping.MappingInst(mapOf("salary" to Range(70000, 99999)), mapOf("salary_band" to "mid")),
            Mapping.MappingInst(mapOf("salary" to Gte(100000)), mapOf("salary_band" to "senior"))
        )))

        // Step 4 (transform): remove the "status" field from the record
        etl.transform.add(RemoveFields("status"))

        // Step 5 (load): create output with selected fields in a defined order
        etl.load.fields.add(FieldEncode<Any>("id", Field("id")))
        etl.load.fields.add(FieldEncode<Any>("name", Field("name")))
        etl.load.fields.add(FieldEncode<Any>("department", Field("department")))
        etl.load.fields.add(FieldEncode<Any>("dept_code", Field("dept_code")))
        etl.load.fields.add(FieldEncode<Any>("salary", Field("salary")))
        etl.load.fields.add(FieldEncode<Any>("salary_band", Field("salary_band")))

        return etl
    }

    /** Reads all records from a JSONL file, one JSON object per line. */
    private fun readJsonl(path: String): List<Datum> =
        File(path).readLines()
            .filter { it.isNotBlank() }
            .map { ParsnipMapper.readValue(it) }

    /** Writes records to a JSONL file, one JSON object per line. */
    private fun writeJsonl(path: String, records: List<Datum>) {
        File(path).printWriter().use { writer ->
            records.forEach { writer.println(objectMapper.writeValueAsString(it)) }
        }
    }

    /**
     * Tests the complete ETL integration workflow using a YAML pipeline file:
     * reads input JSONL, applies the pipeline loaded from YAML, writes output JSONL, and validates.
     */
    fun testPipelineExecution() {
        // load the ETL pipeline from the YAML configuration file
        val yamlMapper = YAMLMapper().registerModule(parsnipModule())
        val etl = yamlMapper.readValue<Etl>(File(pipelineFile))

        // verify the pipeline has the expected five-step structure
        etl.extract.fieldFilters.size shouldBe 1
        etl.transform.size shouldBe 3
        etl.load.fields.size shouldBe 6

        // read input records from JSONL file
        val inputRecords = readJsonl(inputFile)
        inputRecords.size shouldBe 5

        // execute the pipeline on each input record (null results are filtered out)
        val outputRecords = inputRecords.mapNotNull { etl(it) }

        // write the transformed records to a temporary output file
        val outputPath = "target/etl-integration-output.jsonl"
        writeJsonl(outputPath, outputRecords)

        // validate: only active employees pass the extract filter (Bob is inactive)
        outputRecords.size shouldBe 4

        // validate: output records have the expected fields and values
        val expectedRecords = readJsonl(expectedOutputFile)
        outputRecords.size shouldBe expectedRecords.size
        outputRecords.forEachIndexed { i, record ->
            record shouldBe expectedRecords[i]
        }
    }

    /**
     * Tests that the pipeline YAML file round-trips correctly through serialization,
     * and that the serialized YAML from the API matches what is in the pipeline file.
     */
    fun testPipelineYamlRoundTrip() {
        val yamlMapper = YAMLMapper().registerModule(parsnipModule())

        // build the pipeline from the API
        val etl = buildPipeline()
        val generatedYaml = yamlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(etl)

        // parse the generated YAML back and re-serialize to confirm idempotence
        val etl2 = yamlMapper.readValue<Etl>(generatedYaml)
        val roundTripYaml = yamlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(etl2)
        roundTripYaml shouldBe generatedYaml

        // confirm the file-based YAML also round-trips
        val fileYaml = File(pipelineFile).readText()
        val etlFromFile = yamlMapper.readValue<Etl>(fileYaml)
        val reserializedYaml = yamlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(etlFromFile)
        println(reserializedYaml)
    }

}
