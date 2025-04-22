package edu.jhuapl.data.parsnip.io

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * NameObjectDeserializer.kt
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

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonMappingException

import java.io.IOException

/** Deserializes elements as target type, when the object is wrapped in a simple string representing the type. */
abstract class NameObjectDeserializer<X>(private val nameLookup: (String) -> Class<out X>) : JsonDeserializer<X>() {

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(parser: JsonParser, context: DeserializationContext): X? {
        var token = parser.currentToken
        return when (token) {
            JsonToken.VALUE_NULL -> null
            JsonToken.START_OBJECT -> {
                val type: Class<out X> = classFrom(parser.nextFieldName(), context)
                parser.nextValue()
                val res = parser.readValueAs(type)
                while (token != JsonToken.END_OBJECT) {
                    token = parser.nextToken()
                }
                res
            }
            else -> throw context.instantiationException(this::class.java, "Expected an object but was $token")
        }
    }

    @Throws(JsonMappingException::class)
    protected fun classFrom(typeString: String, context: DeserializationContext): Class<out X> {
        return try {
            classFromSimpleName(typeString)
        } catch (x: ClassNotFoundException) {
            throw context.instantiationException(this::class.java, typeString)
        }
    }

    @Throws(ClassNotFoundException::class)
    fun classFromSimpleName(typeName: String) = nameLookup(typeName)

}

/** Deserializes elements as target type, when the object is wrapped in a simple string representing the type. */
abstract class NameObjectDeserializerWithAlternate<X, Y>(
    private val nameLookup: (String) -> Class<out X>,
    private val altNameLookup: (String) -> Class<out Y>,
    private val altTransform: (Y) -> X,
) : JsonDeserializer<X>() {

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(parser: JsonParser, context: DeserializationContext): X? {
        var token = parser.currentToken
        return when (token) {
            JsonToken.VALUE_NULL -> null
            JsonToken.START_OBJECT -> {
                val fieldName = parser.nextFieldName()
                val xType = classFrom(fieldName)
                val type: Class<*> = xType ?: altClassFrom(fieldName, context)
                parser.nextValue()
                val res = parser.readValueAs(type)
                while (token != JsonToken.END_OBJECT) {
                    token = parser.nextToken()
                }
                if (xType == null) altTransform(res as Y) else res as X
            }
            else -> throw context.instantiationException(this::class.java, "Expected an object but was $token")
        }
    }

    @Throws(JsonMappingException::class)
    protected fun classFrom(typeString: String): Class<out X>? {
        return try {
            nameLookup(typeString)
        } catch (x: ClassNotFoundException) {
            null
        }
    }

    @Throws(JsonMappingException::class)
    protected fun altClassFrom(typeString: String, context: DeserializationContext): Class<out Y> {
        return try {
            altNameLookup(typeString)
        } catch (x: ClassNotFoundException) {
            throw context.instantiationException(this::class.java, typeString)
        }
    }

}
