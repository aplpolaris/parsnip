/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * MonteCarlo.kt
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
package edu.jhuapl.data.parsnip.gen

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import edu.jhuapl.data.parsnip.datum.Datum
import kotlin.random.Random

/** Generates repeatable random datums. */
class MonteCarlo(val random: Random = Random, val dimensions: DimensionList = listOf()) {

    constructor(dimensions: DimensionList) : this(Random, dimensions)
    constructor(vararg dimension: Dimension<*>) : this(Random, listOf(*dimension))

    @JsonCreator
    constructor(encodedDimensions: Map<String, String>) : this(Random, encodedDimensions.toDimensionList())

    @get:JsonValue
    val encodedDimensions
        get() = dimensions.map { it.name to it.constraint.toString() }.toMap()

    /** Get a random value for the current configuration. */
    operator fun invoke(): Datum = dimensions.map { it.name to it.constraint.random(random) }.toMap()

}

private fun Map<String, String>.toDimensionList() = map { Dimension(it.key, DimensionConstraint.valueOf(it.value)) }
