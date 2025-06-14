package edu.jhuapl.data.parsnip.gen

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty


/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * Dimension.kt
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

/** Alias for fixed list of dimensions. */
typealias DimensionList = List<Dimension<*>>

/** A dimension with a name, a type, and a type constraint, e.g. range. Also has "observable" labels allowing view of this dimension to be filtered. */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class Dimension<X> @JsonCreator constructor(var name: String, var constraint: DimensionConstraint<X>) {
    override fun toString() = "$name: $constraint"
}
