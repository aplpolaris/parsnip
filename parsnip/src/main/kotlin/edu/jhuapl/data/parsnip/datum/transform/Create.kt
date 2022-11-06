package edu.jhuapl.data.parsnip.datum.transform

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * Create.kt
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

import edu.jhuapl.data.parsnip.datum.Datum
import edu.jhuapl.data.parsnip.datum.DatumTransform

/**
 * Computes multiple output fields using [DatumTransform] instances. These are applied to the input datum to return a
 * new datum with just the fields described by the value encodings. If there are no input fields, keeps all input fields.
 *
 * @param <X> type of input values
 */
open class Create(var fields: MutableList<FieldEncode<*>> = mutableListOf()) : DatumTransform {

    val targetFields: Set<String>
        get() = fields.flatMap { it.target }.toSet()

    override fun invoke(map: Datum): Datum {
        if (fields.isEmpty()) {
            return map
        }
        val res = LinkedHashMap<String, Any?>()
        this.fields.forEach {
            it.transformTo(map, res)
        }
        return res
    }

    /**
     * Looks up the encode function for the given target field.
     * @param field target field
     * @return encode function, or null if none
     */
    operator fun get(field: String) = fields.find { field == it.targetSingle }

}

/** Adds fields generated by "create" to the original map fields. */
class Augment : Create() {
    override fun invoke(map: Datum): Datum = when {
        fields.isEmpty() -> map
        else -> map + super.invoke(map)
    }
}
