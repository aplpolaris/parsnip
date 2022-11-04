/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * MultiDatumTransformSerializer.kt
 * edu.jhuapl.data:parsnip
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
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import edu.jhuapl.data.parsnip.datum.DatumTransform
import edu.jhuapl.data.parsnip.datum.MultiDatumTransform
import edu.jhuapl.data.parsnip.datum.MultiDatumTransformWrapper
import edu.jhuapl.util.types.tryServiceFromShortName
import java.io.IOException

/** Serializer that allows saving content that looks like either a [DatumTransform] or a [MultiDatumTransform]. */
object MultiDatumTransformSerializer : JsonSerializer<MultiDatumTransform>() {

    @Throws(IOException::class)
    override fun serialize(mt: MultiDatumTransform?, gen: JsonGenerator, serializerProvider: SerializerProvider) {
        when (mt) {
            is MultiDatumTransformWrapper -> SimpleValueSerializer.serialize(mt.base, gen, serializerProvider)
            else -> SimpleValueSerializer.serialize(mt, gen, serializerProvider)
        }
    }

}

/** Flexible deserialization as either [DatumTransform] or [MultiDatumTransform]. */
class MultiDatumTransformDeserializer(private val loader: ClassLoader) : JsonDeserializer<MultiDatumTransform>() {
    override fun deserialize(parser: JsonParser, context: DeserializationContext): MultiDatumTransform? {
        var token = parser.currentToken
        return when (token) {
            JsonToken.VALUE_NULL -> null
            JsonToken.START_OBJECT -> {
                val fieldName = parser.nextFieldName()
                val type: Class<*>? = classFrom(fieldName, context)
                parser.nextValue()
                val res = type?.let { parser.readValueAs(it) }
                while (token != JsonToken.END_OBJECT) {
                    token = parser.nextToken()
                }
                when (res) {
                    is DatumTransform -> MultiDatumTransformWrapper(res)
                    is MultiDatumTransform -> res
                    else -> throw ClassNotFoundException(fieldName)
                }
            }
            else -> throw context.instantiationException(this::class.java, "Expected an object but was $token")
        }
    }

    private fun classFrom(name: String, context: DeserializationContext): Class<*>? {
        val attempt1 = tryServiceFromShortName(loader, name, MultiDatumTransform::class.java, DATUM_TRANSFORM_DEFAULT_PACKAGE)
        val attempt2 = tryServiceFromShortName(loader, name, DatumTransform::class.java, DATUM_TRANSFORM_DEFAULT_PACKAGE)
        return attempt1 ?: attempt2
    }
}
