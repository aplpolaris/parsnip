/*-
 * #%L
 * parsnip-1.0.0-SNAPSHOT
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
package edu.jhuapl.data.parsnip.io

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import java.io.IOException

/** Used for deserialization of class strings, allowing primitive strings to omit the "java.lang" prefix. */
object ClassSerializer : JsonSerializer<Class<*>>() {
    @Throws(IOException::class)
    override fun serialize(value: Class<*>, gen: JsonGenerator, serializers: SerializerProvider) {
        var nm = value.name
        if (nm.startsWith("java.lang.") || nm.startsWith("java.util.")) {
            nm = nm.substring(10)
        }
        gen.writeString(nm)
    }
}
