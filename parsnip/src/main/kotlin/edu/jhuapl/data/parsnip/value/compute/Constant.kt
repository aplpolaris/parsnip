/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * Constant.kt
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
package edu.jhuapl.data.parsnip.value.compute

import com.fasterxml.jackson.annotation.JsonCreator
import edu.jhuapl.data.parsnip.value.ValueCompute
import edu.jhuapl.util.types.SimpleValue
import edu.jhuapl.utilkt.core.log
import java.util.logging.Level

/** Returns the input value. */
class Identity : ValueCompute<Any?> {
    override fun invoke(p: Any?) = p
}

/** Returns a constant value. */
data class Constant @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(var value: Any?) : ValueCompute<Any>, SimpleValue {
    override val simpleValue: Any?
        get() = value

    override fun invoke(p: Any?) = value
}

/** Passes value through after logging it. */
data class LogValue @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(var level: String = "INFO") : ValueCompute<Any?> {
    override fun invoke(p: Any?) = p.also { log(it) }
    private fun log(p: Any?) = log<LogValue>(Level.parse(level), "$p")
}
