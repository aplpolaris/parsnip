/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * Chain.kt
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
package edu.jhuapl.data.parsnip.datum.compute

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import edu.jhuapl.data.parsnip.datum.Datum
import edu.jhuapl.data.parsnip.datum.DatumCompute
import edu.jhuapl.data.parsnip.decode.Decoder
import edu.jhuapl.data.parsnip.io.DatumComputeDeserializer
import edu.jhuapl.data.parsnip.io.SimpleValueSerializer
import edu.jhuapl.data.parsnip.io.ValueComputeDeserializer
import edu.jhuapl.data.parsnip.value.ValueCompute
import edu.jhuapl.data.parsnip.value.compute.As
import edu.jhuapl.data.parsnip.value.compute.Decode

/**
 * Combines a single [DatumCompute] with 0 or more [ValueCompute]s to compute a value from a datum.
 */
open class Chain<X>(var from: DatumCompute<*> = Constant(null), vararg _process: ValueCompute<*>) : DatumCompute<X> {

    @JsonCreator
    constructor(from: DatumCompute<*>, process: List<ValueCompute<*>>): this(from, *process.toTypedArray())

    var process: MutableList<ValueCompute<*>> = mutableListOf(*_process)

    @get:JsonIgnore
    val targetType: Class<*>?
        get() = cast?.type ?: decoder?.javaType
    @get:JsonIgnore
    val cast: As?
        get() = process.lastOrNull { it is As } as As?
    @get:JsonIgnore
    val decoder: Decoder<*>?
        get() = (process.lastOrNull { it is Decode } as Decode?)?.decoder

    override fun toString(): String {
        return "Chain(from=$from, process=$process)"
    }

    override fun invoke(map: Datum): X? {
        var res = from(map)
        process.forEach { res = it(res) }
        return res as? X
    }

}
