package edu.jhuapl.data.parsnip.datum.compute

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * Condition.kt
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

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import edu.jhuapl.data.parsnip.datum.Datum
import edu.jhuapl.data.parsnip.datum.DatumCompute
import edu.jhuapl.data.parsnip.datum.filter.DatumFieldFilter
import edu.jhuapl.data.parsnip.value.ValueCompute
import edu.jhuapl.data.parsnip.value.ValueFilter
import edu.jhuapl.util.types.SimpleValue
import edu.jhuapl.util.types.nestedPutAll

/**
 * Computes a conditional value depending upon the input. Returns value associated with the first condition that passes.
 * If no condition passes, returns null.
 */
class Condition @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(var mappings: List<ConditionMappingInst>) : DatumCompute<Any?>, SimpleValue {

    override val simpleValue
        get() = mappings

    override operator fun invoke(map: Datum) = mappings.firstOrNull { it.appliesTo(map) }?.value?.invoke(map)

    /** Describes a single filter to apply to input data, and a resulting set of values to put.  */
    class ConditionMappingInst @JsonCreator constructor(@JsonProperty("when") condition: Map<String, ValueFilter>,
                                                        @JsonProperty("value") val value: DatumCompute<Any?>) {
        @JsonIgnore
        var appliesTo: DatumFieldFilter = DatumFieldFilter(condition)
        var filter: MutableMap<String, ValueFilter>
            @JsonProperty("when")
            get() = appliesTo.fieldFilters
            set(fieldFilters) {
                appliesTo.fieldFilters = fieldFilters
            }
    }

}
