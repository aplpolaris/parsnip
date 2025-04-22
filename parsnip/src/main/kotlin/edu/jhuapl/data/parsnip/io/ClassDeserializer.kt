/*-
 * #%L
 * parsnip-1.0.0-SNAPSHOT
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
package edu.jhuapl.data.parsnip.io

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import java.io.IOException

/** Used for deserialization of class strings, allowing primitive strings to omit the "java.lang" prefix. */
object ClassDeserializer : JsonDeserializer<Class<*>>() {

    @Throws(IOException::class)
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Class<*> {
        val name = (p.codec.readTree<TreeNode>(p) as JsonNode).asText()
        if (PRIMITIVE_LOOKUP.containsKey(name)) {
            return PRIMITIVE_LOOKUP[name]!!
        }
        try {
            return Class.forName("java.lang.$name")
        } catch (ex: ClassNotFoundException) {
            // ignore
        }
        try {
            return Class.forName("java.util.$name")
        } catch (ex: ClassNotFoundException) {
            // ignore
        }
        return try {
            Class.forName(name)
        } catch (ex: ClassNotFoundException) {
            throw IOException(ex)
        }
    }

    private val PRIMITIVE_LOOKUP = mapOf(
        "long" to Long::class.javaPrimitiveType,
        "int" to Int::class.javaPrimitiveType,
        "short" to Short::class.javaPrimitiveType,
        "byte" to Byte::class.javaPrimitiveType,
        "float" to Float::class.javaPrimitiveType,
        "double" to Double::class.javaPrimitiveType,
        "boolean" to Boolean::class.javaPrimitiveType,
        "void" to Void.TYPE
    )

}
