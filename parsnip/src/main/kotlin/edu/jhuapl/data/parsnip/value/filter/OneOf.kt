package edu.jhuapl.data.parsnip.value.filter

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * OneOf.kt
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

import com.fasterxml.jackson.annotation.JsonCreator
import edu.jhuapl.data.parsnip.value.ValueFilter
import edu.jhuapl.util.types.SimpleValue

/** Tests if value is contained in any of a given list of values. Uses [Equal] for testing. */
class OneOf @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(vararg these: Any) : ValueFilter, SimpleValue {

    var list = these

    override val simpleValue
        get() = list

    override fun invoke(o: Any?) = list.any { Equal(o).invoke(it) }

}
