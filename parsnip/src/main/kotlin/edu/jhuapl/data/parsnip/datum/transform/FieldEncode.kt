/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * FieldEncode.kt
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
package edu.jhuapl.data.parsnip.datum.transform

import edu.jhuapl.data.parsnip.datum.Datum
import edu.jhuapl.data.parsnip.datum.DatumCompute
import edu.jhuapl.data.parsnip.datum.compute.Chain
import edu.jhuapl.data.parsnip.datum.compute.Constant
import edu.jhuapl.data.parsnip.datum.compute.asList
import edu.jhuapl.data.parsnip.value.ValueCompute
import edu.jhuapl.data.parsnip.value.ValueFilter
import edu.jhuapl.data.parsnip.value.compute.ValueFilterCompute
import edu.jhuapl.data.parsnip.value.compute.As
import edu.jhuapl.util.types.nestedPut
import edu.jhuapl.utilkt.core.fine

/**
 * Describes an encoding that maps to a target field. Includes a (required) [DatumCompute] to pull from source data, and a number
 * of (optional) [ValueCompute]s to further process the value before it is returned. Not designed for serialization.
 * When used in [Create] or [Augment], the [target] field is assumed to be either a plain field or a JSON pointer.
 */
class FieldEncode<X>(_target: List<String>,
                     _from: DatumCompute<*> = Constant(null),
                     vararg _process: ValueCompute<*>
) : Chain<X>(_from, *_process) {

    constructor(target: String, from: DatumCompute<*> = Constant(null), vararg process: ValueCompute<*>) : this(listOf(target), from, *process)
    constructor(target: String, from: DatumCompute<*> = Constant(null), booleanValue: ValueFilter) : this(listOf(target), from, ValueFilterCompute(booleanValue), As(Boolean::class.java))

    var target: List<String> = checkNotEmpty(_target)
        set(value) {
            field = checkNotEmpty(value)
        }
    val targetSingle
        get() = target[0]
    val targetMultipleFields
        get() = target.size > 1

    override fun toString(): String {
        return "FieldEncode(target='$target', from=$from, process=$process)"
    }

    /** Transforms given datum using the field encoding, and adds to result. */
    internal fun transformTo(map: Datum, res: MutableMap<String, Any?>) {
        try {
            val value = invoke(map)
            if (targetMultipleFields) {
                val listValue = value.asList()
                target.forEachIndexed { i, field -> res.nestedPut(field, listValue.getOrNull(i)) }
            } else {
                res.nestedPut(targetSingle, value)
            }
        } catch (x: Exception) {
            fine<FieldEncode<*>>("Failed to compute $this for value $map")
        }
    }

}

//region HELPER CLASSES AND EXTENSION FUNCTIONS

private fun <X> checkNotEmpty(list: List<X>): List<X> {
    if (list.isEmpty()) {
        throw IllegalArgumentException("Must have at least one target field.")
    }
    return list
}

//endregion
