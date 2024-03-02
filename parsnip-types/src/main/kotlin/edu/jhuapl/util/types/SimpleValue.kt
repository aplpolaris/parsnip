/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * SimpleValue.kt
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
package edu.jhuapl.util.types

import com.fasterxml.jackson.annotation.JsonIgnore

/** Marker interface for classes that have a single simple value that can be used for serialization. */
interface SimpleValue {

    /**
     * Return simple value representation for use with simple serialization.
     * @return simple value, or this if there is no simple value
     */
    @get:JsonIgnore
    val simpleValue: Any?

}
