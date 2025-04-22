/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * DatumFilterDeserializer.kt
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
package edu.jhuapl.data.parsnip.io

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonMappingException
import edu.jhuapl.data.parsnip.datum.DatumFilter
import edu.jhuapl.data.parsnip.datum.filter.And
import edu.jhuapl.data.parsnip.datum.filter.DatumFieldFilter
import edu.jhuapl.data.parsnip.datum.filter.Not
import edu.jhuapl.data.parsnip.datum.filter.Or
import java.util.Stack

import java.io.IOException

/**
 * Handles serialization of Datum filter classes.
 * @author Christopher Paul
 */
class DatumFilterDeserializer : JsonDeserializer<DatumFilter>() {

    private var stackTypes = Stack<Class<out DatumFilter>?>()

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(parser: JsonParser, context: DeserializationContext): DatumFilter? {
        var token = parser.currentToken
        var type: Class<out DatumFilter>? = null
        return when (token) {
            JsonToken.VALUE_NULL -> null
            JsonToken.START_OBJECT -> {
                val nextField: String = parser.nextFieldName() ?: return DatumFieldFilter()
                // if logical comb filter type proceed
                try {
                    type = filterType(nextField)
                    stackTypes.push(type)
                    parser.nextValue()
                }
                // else leave type as null and we'll handle
                catch (x: ClassNotFoundException) {}

                val res = constructFilter(type, parser, context)
                token = parser.currentToken()
                while (token != JsonToken.END_OBJECT) {
                    token = parser.nextToken()
                }
                if (type != null) { stackTypes.pop() }
                res
            }
            JsonToken.START_ARRAY -> {
                // deserialize all DatumFieldFilters in the array
                val datumFieldFilterMutList: MutableList<DatumFilter> = ArrayList()
                token = parser.nextValue()
                while (token != JsonToken.END_ARRAY) {
                    datumFieldFilterMutList.add(deserialize(parser, context)!!)
                    while (parser.currentToken() == JsonToken.END_OBJECT) {
                        token = parser.nextValue()
                    }
                }

                val datumFieldFilterArr = datumFieldFilterMutList.toTypedArray()
                // Done with array so move on
                parser.nextToken()

                // Now use those DatumFieldFilters to construct a Logical Combination Object
                when (stackTypes.peek()) {
                    And::class.java -> And(*datumFieldFilterArr)
                    Or::class.java -> Or(*datumFieldFilterArr)
                    else -> throw IllegalStateException("Should not occur")
                }
            }
            else -> parser.readValueAs(DatumFilter::class.java)
        }
    }

    @Throws(JsonMappingException::class)
    private fun filterType(typeString: String): Class<out DatumFilter> {
        return Class.forName(packageName(DatumFieldFilter::class.java) + "." + typeString) as Class<out DatumFilter>
    }

    @Throws(IOException::class)
    private fun constructFilter(type: Class<out DatumFilter>?, parser: JsonParser, context: DeserializationContext): DatumFilter? {
        return when (type) {
            And::class.java, Or::class.java -> deserialize(parser, context)
            Not::class.java -> Not(deserialize(parser, context)!!)
            null -> context.readValue(parser, DatumFieldFilter::class.java)
            else -> parser.readValueAs(type)
        }

    }

}

