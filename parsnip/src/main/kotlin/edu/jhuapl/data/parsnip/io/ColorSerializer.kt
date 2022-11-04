/*-
 * #%L
 * parsnip-1.0.0-SNAPSHOT
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
package edu.jhuapl.data.parsnip.io

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import java.awt.Color
import java.io.IOException

/** Serializes colors as json strings, using #RRGGBB notation. */
object ColorSerializer : JsonSerializer<Color>() {

    @Throws(IOException::class)
    override fun serialize(value: Color, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(encode(value))
    }

    /** Convert color to string. Results in #RRGGBB or #RRGGBBAA, depending on whether or not the color has an alpha channel. */
    private fun encode(c: Color) = when (c.alpha) {
        255 -> String.format("#%02x%02x%02x", c.red, c.green, c.blue)
        else -> String.format("#%02x%02x%02x%02x", c.red, c.green, c.blue, c.alpha)
    }

}
