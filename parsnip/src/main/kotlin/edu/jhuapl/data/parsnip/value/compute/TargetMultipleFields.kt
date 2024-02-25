/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * TargetMultipleFields.kt
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
package edu.jhuapl.data.parsnip.value.compute

import com.fasterxml.jackson.annotation.JsonCreator
import edu.jhuapl.data.parsnip.value.ValueCompute
import edu.jhuapl.util.types.SimpleValue
import java.lang.IllegalStateException

/** Serialization helper that indicates result should target multiple fields. Used for serialization purposes only. */
class TargetMultipleFields @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(override var simpleValue: List<String>) : SimpleValue, ValueCompute<Nothing> {
    override fun invoke(p1: Any?): Nothing? = throw IllegalStateException("This method should never be called.")
}
