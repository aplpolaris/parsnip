/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * Fold.kt
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
package edu.jhuapl.data.parsnip.datum.transform

import com.fasterxml.jackson.annotation.JsonProperty
import edu.jhuapl.data.parsnip.datum.Datum
import edu.jhuapl.data.parsnip.datum.MultiDatumTransform

/**
 * Split a datum into multiple datums, one for each separate field given.
 * The result will have two additional fields added to the input datum for each field: one with the key of the field and one with the value.
 * For instance, ``{a: 20, b: 10}`` might fold into two datums: ``{a: 20, b: 10, key: a, value: 20}`` and ``{a: 20, b: 10, key: b, value: 10}``.
 */
data class Fold(@JsonProperty("fields") var fields: List<String>, @JsonProperty("as") var `as`: List<String> = listOf("key", "value")): MultiDatumTransform {

    constructor(vararg fields: String) : this(listOf(*fields))

    override fun invoke(p1: Datum) = fields.map {
        p1.toMutableMap() + mapOf(`as`.getOrElse(0) { "key" } to it, `as`.getOrElse(1) { "value" } to p1[it])
    }

}
