/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * DatumFunctions.kt
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
package edu.jhuapl.data.parsnip.datum

import edu.jhuapl.util.types.atPointer

typealias Datum = Map<String, Any?>
typealias MutableDatum = MutableMap<String, Any?>

/** Computes a scalar output value for a datum input value. */
interface DatumCompute<out Y> : (Datum) -> Y?

/** Computes a boolean output value for a datum input value. */
interface DatumFilter : (Datum) -> Boolean

/** Computes a single datum output value (or null) for a datum input value. */
interface DatumTransform : DatumCompute<Datum>

/** Computes multiple (zero or more) output values for a single input value. */
interface MultiDatumTransform : (Datum) -> List<Datum>

/** Converts a single datum compute to a multi-datum compute. */
fun DatumTransform.asMultiDatumTransform(): MultiDatumTransform = MultiDatumTransformWrapper(this)

/** Allows using a [DatumTransform] as a [MultiDatumTransform]. */
class MultiDatumTransformWrapper(var base: DatumTransform) : MultiDatumTransform {
    override fun invoke(d: Datum) = listOfNotNull(base(d))
}

//region Datum EXTENSION FUNCTIONS

/**
 * Retrieves value, using either the key if present or attempting JSON pointer if not.
 * @param key key or JSON pointer
 * @return value if found, else null
 */
fun Datum.atFieldOrPointer(key: String): Any? = when {
    containsKey(key) -> this[key]
    else -> atPointer(key, Any::class.java)
}

/**
 * Flattens a number of field names within an input map. Any nested fields matching one of these will be expanded.
 * @param flatFields fields to flatten
 * @param fieldSep separator to use when joining field names, typically "."
 * @return result of flattening object
 */
fun Datum.flattenFieldNames(flatFields: Collection<String>?, fieldSep: String): Datum {
    if (flatFields.isNullOrEmpty()) {
        return this
    }
    val copy = this.toMutableMap()
    flatFields.forEach { f ->
        val value = copy[f]
        if (value is Map<*, *>) {
            val valueMap = ensureStringKeys(value).flattenFieldNames(flatFields, fieldSep)
            valueMap.forEach { (k, v) -> copy[f + fieldSep + k] = v }
            copy.remove(f)
        }
    }
    return copy
}

/** Utility ensuring keys are converted to strings if they are not already. */
private fun ensureStringKeys(map: Map<*, *>): Datum {
    return when {
        map.keys.all { it is String } -> map as Datum
        else -> map.mapKeys { x -> x.toString() }
    }
}

//endregion
