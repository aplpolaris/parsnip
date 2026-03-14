package edu.jhuapl.data.parsnip

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * AlienEncounterPipelineTest.kt
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
import edu.jhuapl.data.parsnip.io.ParsnipMapper
import edu.jhuapl.data.parsnip.io.parsnipModule
import junit.framework.TestCase
import kotlin.random.Random

/**
 * Generates a randomised batch of alien encounter sensor reports, runs them
 * through the Operation Zorblax BatchEtl pipeline defined in
 * alien-encounter-pipeline.yaml, and pretty-prints the results.
 */
class AlienEncounterPipelineTest : TestCase() {

    fun testAlienEncounterPipeline() {
        // ── Load pipeline from YAML ──────────────────────────────────────────
        val yamlMapper = YAMLMapper().registerModule(parsnipModule())
        val yamlText = AlienEncounterPipelineTest::class.java
            .getResourceAsStream("/alien-encounter-pipeline.yaml")!!
            .bufferedReader().readText()
        val etl = yamlMapper.readValue<BatchEtl>(yamlText)

        // ── Generate random input records ────────────────────────────────────
        val input = generateRandomEncounters(count = 8, seed = 42)

        // Write to a temp JSON file (the "random input file")
        val inputFile = createTempFile(prefix = "alien-encounters-", suffix = ".json")
        ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(inputFile, input)

        // Read back from the file to demonstrate the round-trip
        val inputFromFile: List<Map<String, Any?>> =
            ObjectMapper().readValue(inputFile)

        // ── Apply the ETL ────────────────────────────────────────────────────
        val output = etl.invoke(inputFromFile)

        // ── Print results ────────────────────────────────────────────────────
        println("╔══════════════════════════════════════════════════════════╗")
        println("║         OPERATION ZORBLAX — INPUT SENSOR REPORTS        ║")
        println("╚══════════════════════════════════════════════════════════╝")
        println("Source file : ${inputFile.absolutePath}")
        println("Record count: ${inputFromFile.size}")
        println()
        println(ParsnipMapper.writerWithDefaultPrettyPrinter().writeValueAsString(inputFromFile))

        println()
        println("╔══════════════════════════════════════════════════════════╗")
        println("║         OPERATION ZORBLAX — THREAT ALERT OUTPUT         ║")
        println("╚══════════════════════════════════════════════════════════╝")
        println("Alert count : ${output.size}  (one per detected ability, after extract filters)")
        println()
        println(ParsnipMapper.writerWithDefaultPrettyPrinter().writeValueAsString(output))

        // ── Assertions ───────────────────────────────────────────────────────
        assertTrue("Pipeline produced no output — check extract filters", output.isNotEmpty())
        output.forEach { record ->
            assertNotNull("incident_label missing", record["incident_label"])
            assertNotNull("ability missing",        record["ability"])
            assertNotNull("threat_class missing",   record["threat_class"])
            assertNotNull("processed_at missing",   record["processed_at"])
        }
    }

    // ── Random data generator ────────────────────────────────────────────────

    private fun generateRandomEncounters(count: Int, seed: Int): List<Map<String, Any?>> {
        val rng = Random(seed)

        val species    = listOf("Zorblax", "Glurboid", "Vexomancer", "Schlorpian", "Nebulon")
        val sectors    = listOf("Alpha Centauri", "Epsilon Eridani", "Tau Ceti", "Wolf 359", "Barnard's Star")
        val locations  = listOf("Station Omega", "Lunar Base 7", "Asteroid Belt X-99", "Deep Space Relay 12")
        val statuses   = listOf("WATCH", "ELEVATED", "CRITICAL")
        val outcomes   = listOf("contained", "neutralized", "fled the scene", "captured", "still at large")
        val reporters  = listOf("Commander Zara", "Admiral Krix", "Lt. Fumblor", "Sgt. Bleep", "Ensign Wobble")
        val allAbilities = listOf(
            "phase-shifting", "mind-control", "plasma-breath", "teleportation",
            "time-dilation", "invisibility", "sonic-screech", "gravity-manipulation"
        )

        return (1..count).map { i ->
            val numAbilities = rng.nextInt(1, 4)
            mapOf(
                "incident_id"        to "GX-${4000 + i}",
                "reporter"           to reporters[rng.nextInt(reporters.size)],
                "species"            to species[rng.nextInt(species.size)],
                "threat_level"       to rng.nextInt(1, 11),
                "sector"             to sectors[rng.nextInt(sectors.size)],
                "sensor_subnet"      to "10.${rng.nextInt(0, 256)}.${rng.nextInt(0, 256)}.0/24",
                "alert_status"       to statuses[rng.nextInt(statuses.size)],
                "special_abilities"  to allAbilities.shuffled(rng).take(numAbilities),
                "encounter_location" to locations[rng.nextInt(locations.size)],
                "encounter_outcome"  to outcomes[rng.nextInt(outcomes.size)]
            )
        }
    }
}
