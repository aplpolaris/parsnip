package edu.jhuapl.data.parsnip.io

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * SimpleValueSerializer.kt
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

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.ser.BeanSerializerBuilder
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory
import com.fasterxml.jackson.databind.type.SimpleType
import edu.jhuapl.util.types.SimpleValue

import java.io.IOException

/**
 * Handles serialization of simple value objects. Uses the simple value for serialization if present, otherwise uses
 * the standard class serialization embedded in a single key-value map.
 */
object SimpleValueSerializer : JsonSerializer<Any>() {

    @Throws(IOException::class)
    override fun serialize(mt: Any?, gen: JsonGenerator, serializerProvider: SerializerProvider) {
        when {
            mt == null -> gen.writeNull()
            mt is SimpleValue && mt.simpleValue != mt -> gen.writePairObject(mt::class.java.simpleName to mt.simpleSerializableValue(gen, serializerProvider))
            else -> {
                gen.writeStartObject()
                gen.writeFieldName(mt::class.java.simpleName)
                mt.writeBeanMapObject(gen, serializerProvider)
                gen.writeEndObject()
//                gen.writePairObject(mt::class.java.simpleName to mt.toSerializableMap(gen, serializerProvider))
            }
        }
    }

}

/** Gets the value object that should be used for serializing a [SimpleValue]. */
internal fun SimpleValue.simpleSerializableValue(gen: JsonGenerator, serializerProvider: SerializerProvider): Any = when (val sv = simpleValue) {
    null -> mapOf<String, Any?>()
//    this -> toSerializableMap(gen, serializerProvider)
    else -> sv
}

/** This uses a default [ObjectMapper] so that default object serialization is used; otherwise there's an infinite loop. */
internal fun Any.writeBeanMapObject(gen: JsonGenerator, serializerProvider: SerializerProvider) {
    val type = SimpleType.constructUnsafe(this.javaClass)
    val beanInfo = serializerProvider.config.introspect(type)
    val serializer = BeanSerializerFactory.instance.findBeanOrAddOnSerializer(serializerProvider, type, beanInfo, true)
    serializer.serialize(this, gen, serializerProvider)
}


/** This uses a default [ObjectMapper] so that default object serialization is used; otherwise there's an infinite loop. */
internal fun Any.toSerializableMap(gen: JsonGenerator, serializerProvider: SerializerProvider): LinkedHashMap<*, *> {
    return ObjectMapper().convertValue(this, LinkedHashMap::class.java)
}

internal fun JsonGenerator.writePairObject(pair: Pair<Any, Any?>) = writeObject(mapOf(pair))
internal fun JsonGenerator.writePairsObject(vararg pairs: Pair<Any, Any?>) = writeObject(mapOf(*pairs))
