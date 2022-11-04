package edu.jhuapl.data.parsnip.datum.compute

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * Field.kt
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
import edu.jhuapl.data.parsnip.datum.DatumCompute
import edu.jhuapl.util.types.SimpleValue
import edu.jhuapl.util.types.atPointer

/**
 * Looks up a value in a field. The field may be a simple key/JSON pointer.
 */
data class Field @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(var field: String) : DatumCompute<Any>, SimpleValue {

    override val simpleValue: Any?
        get() = this.field

    override fun invoke(map: Map<String, *>): Any? = map.atPointer(field)

}
