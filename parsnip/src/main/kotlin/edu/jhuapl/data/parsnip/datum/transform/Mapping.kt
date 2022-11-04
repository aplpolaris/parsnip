package edu.jhuapl.data.parsnip.datum.transform

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * Mapping.kt
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

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import edu.jhuapl.data.parsnip.datum.Datum
import edu.jhuapl.data.parsnip.datum.filter.DatumFieldFilter
import edu.jhuapl.data.parsnip.datum.DatumTransform
import edu.jhuapl.data.parsnip.value.ValueFilter
import edu.jhuapl.util.types.SimpleValue
import edu.jhuapl.util.types.nestedPutAll

/** Monitors input value content as filters, writes content to output. Supports multiple filters.  */
class Mapping @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(var mappings: List<MappingInst>) : DatumTransform, SimpleValue {

    override val simpleValue
        get() = mappings

    override operator fun invoke(map: Datum): Datum? {
        val res = map.toMutableMap()
        mappings.filter { it.appliesTo(map) }.forEach { res.nestedPutAll(it.put) }
        return res
    }

    /** Describes a single filter to apply to input data, and a resulting set of values to put.  */
    class MappingInst @JsonCreator constructor(@JsonProperty("when") condition: Map<String, ValueFilter>,
                                               @param:JsonProperty("put") var put: Map<String, Any?>) {
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
