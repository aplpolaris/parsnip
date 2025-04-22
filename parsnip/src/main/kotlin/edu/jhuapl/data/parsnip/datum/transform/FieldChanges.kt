package edu.jhuapl.data.parsnip.datum.transform

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * FieldChanges.kt
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

import com.fasterxml.jackson.annotation.JsonCreator
import edu.jhuapl.data.parsnip.datum.Datum
import edu.jhuapl.data.parsnip.datum.DatumTransform
import edu.jhuapl.data.parsnip.datum.flattenFieldNames
import edu.jhuapl.util.types.SimpleValue

/** Retains selected fields from data. */
class RetainFields @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(vararg ff: String) : FieldsDatumTransform(*ff) {
    override fun invoke(map: Datum)= map.toMutableMap().apply { keys.retainAll(fields) }
}

/** Removes selected fields from data. */
class RemoveFields @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(vararg ff: String) : FieldsDatumTransform(*ff) {
    override fun invoke(map: Datum) = map.toMutableMap().apply { keys.removeAll(fields) }
}

/**
 * Flattens selected fields in a map (un-nesting the map).
 */
class FlattenFields @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(vararg ff: String) : FieldsDatumTransform(*ff) {
    override fun invoke(map: Datum) = map.flattenFieldNames(listOf(*fields), ".")
}

/** Generic class that operates on a set of fields */
abstract class FieldsDatumTransform(vararg ff: String) : DatumTransform, SimpleValue {

    var fields: Array<String> = arrayOf(*ff)

    override val simpleValue: Array<String>
        get() = fields

}
