/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * DatumFieldFilter.kt
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
package edu.jhuapl.data.parsnip.datum.filter

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import edu.jhuapl.data.parsnip.datum.Datum
import edu.jhuapl.data.parsnip.datum.DatumFilter
import edu.jhuapl.data.parsnip.datum.atFieldOrPointer
import edu.jhuapl.data.parsnip.value.ValueFilter
import edu.jhuapl.data.parsnip.value.filter.Equal

/**
 * Filter that applies separate filters to multiple fields in a [Datum]. Serialization is designed to support flexible
 * representations of [ValueFilter] contents.
 */
class DatumFieldFilter @JsonCreator constructor(filters: Map<String, ValueFilter> = mutableMapOf()) : DatumFilter {

    @JsonValue
    var fieldFilters: MutableMap<String, ValueFilter> = filters.toMutableMap()

    constructor(vararg pairs: Pair<String, ValueFilter>) : this(mutableMapOf(*pairs))
    constructor(k: String, v: Any?) : this(k to valueFilter(v))
    constructor(k1: String, v1: Any?, k2: String, v2: Any?) : this(k1 to valueFilter(v1), k2 to valueFilter(v2))
    constructor(k1: String, v1: Any?, k2: String, v2: Any?, k3: String, v3: Any?) : this(k1 to valueFilter(v1), k2 to valueFilter(v2), k3 to valueFilter(v3))

    fun put(s: String, f: ValueFilter): DatumFieldFilter {
        fieldFilters[s] = f
        return this
    }

    override fun invoke(map: Datum) = fieldFilters.all { it.value(map.atFieldOrPointer(it.key)) }
}

private fun valueFilter(v: Any?) = if (v is ValueFilter) v else Equal(v)
