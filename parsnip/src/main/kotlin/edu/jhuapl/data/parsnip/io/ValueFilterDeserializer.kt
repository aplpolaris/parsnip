package edu.jhuapl.data.parsnip.io

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * ValueFilterDeserializer.kt
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
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonMappingException
import edu.jhuapl.data.parsnip.value.ValueFilter
import edu.jhuapl.data.parsnip.value.filter.*

import java.io.IOException

/**
 * Handles serialization of value filter classes.
 */
class ValueFilterDeserializer() : JsonDeserializer<ValueFilter>() {

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(parser: JsonParser, context: DeserializationContext): ValueFilter? {
        var token = parser.currentToken
        return when (token) {
            JsonToken.VALUE_NULL -> null
            JsonToken.START_OBJECT -> {
                val type = filterType(parser.nextFieldName(), context)
                parser.nextValue()
                val res = constructFilter(type, parser, context)
                while (token != JsonToken.END_OBJECT) {
                    token = parser.nextToken()
                }
                res
            }
            JsonToken.START_ARRAY -> parser.readValueAs(OneOf::class.java)
            else -> parser.readValueAs(Equal::class.java)
        }
    }

    @Throws(JsonMappingException::class)
    private fun filterType(typeString: String, context: DeserializationContext): Class<out ValueFilter> {
        return try {
            Class.forName(packageName(ValueFilter::class.java) + ".filter." + typeString) as Class<out ValueFilter>
        } catch (x: ClassNotFoundException) {
            throw context.instantiationException(ValueFilter::class.java, typeString)
        }

    }

    @Throws(IOException::class)
    private fun constructFilter(type: Class<out ValueFilter>, parser: JsonParser, context: DeserializationContext): ValueFilter {
        return when (type) {
            // using the not function prevents double negatives
            Not::class.java -> deserialize(parser, context)!!.not()
            And::class.java -> And(*constructFilterArray(parser, context))
            Or::class.java -> Or(*constructFilterArray(parser, context))
//            Range::class.java -> Range(*parser.readArray())
            else -> parser.readValueAs(type)
        }
    }

    private inline fun <reified T> JsonParser.readArray() = readValueAs(Array<T>::class.java)

    @Throws(IOException::class)
    private fun constructFilterArray(parser: JsonParser, context: DeserializationContext): Array<ValueFilter> {
        return when (parser.currentToken) {
            JsonToken.START_ARRAY -> context.readValue(parser, Array<ValueFilter>::class.java)
            else -> deserialize(parser, context)?.let { arrayOf(it) } ?: emptyArray()
        }
    }

}
