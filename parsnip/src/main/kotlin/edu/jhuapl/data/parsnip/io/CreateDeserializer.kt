/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * CreateDeserializer.kt
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

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.module.kotlin.convertValue
import edu.jhuapl.data.parsnip.datum.DatumTransform
import edu.jhuapl.data.parsnip.datum.compute.Constant
import edu.jhuapl.data.parsnip.datum.transform.Augment
import edu.jhuapl.data.parsnip.datum.transform.Create
import edu.jhuapl.data.parsnip.datum.transform.FieldEncode
import edu.jhuapl.data.parsnip.value.ValueCompute
import edu.jhuapl.data.parsnip.value.compute.TargetMultipleFields

/** Deserializer for [Create] objects. */
class CreateDeserializer(loader: ClassLoader) : MapCreateDeserializerSupport<Create>(loader, { Create() } )
/** Deserializer for [Augment] objects. */
class AugmentDeserializer(loader: ClassLoader) : MapCreateDeserializerSupport<Augment>(loader, { Augment() } )

/**
 * Handles serialization of [Create].
 */
open class MapCreateDeserializerSupport<T : Create>(val loader: ClassLoader, val init: () -> T) : JsonDeserializer<T>() {

    override fun deserialize(p: JsonParser, context: DeserializationContext): T? {
        return when (val token = p.currentToken) {
            JsonToken.VALUE_NULL -> init()
            JsonToken.START_OBJECT -> createMapEncode(p.readValueAs(LinkedHashMap::class.java))
            else -> throw context.instantiationException(DatumTransform::class.java, "Expected an object but was $token")!!
        }
    }

    private fun createMapEncode(map: java.util.LinkedHashMap<*, *>): T {
        val res = init()
        map.forEach { (field, obj) ->
            val fe = FieldEncode<Any>(field as String)
            when (obj) {
                is Map<*, *> -> loadComputesFromObject(fe, obj)
                is List<*> -> loadComputesFromList(fe, obj.filterNotNull())
                else -> fe.from = Constant(obj)
            }
            res.fields.add(fe)
        }
        return res
    }

    /** Creates from/process fields from given list */
    private fun loadComputesFromList(fe: FieldEncode<*>, obj: List<Any>) {
        if (obj.isNotEmpty()) {
            loadComputesFromObject(fe, obj[0])
            val processList = obj.subList(1, obj.size).map {
                CustomParsnipMapper(loader).convertValue<ValueCompute<*>>(it)
            }
            fe.process = processList.filter { it !is TargetMultipleFields }.toMutableList()
            val targetList = processList.filterIsInstance<TargetMultipleFields>()
            if (targetList.isNotEmpty()) {
                fe.target = targetList[0].simpleValue
            }
        }
    }

    /** Creates from/process fields from given object. */
    private fun loadComputesFromObject(fe: FieldEncode<*>, obj: Any) {
        if (obj is LinkedHashMap<*, *> && obj.size > 1) {
            val list = obj.map { (k, v) -> mapOf(k to v) }.toList()
            loadComputesFromList(fe, list)
        } else {
            fe.from = CustomParsnipMapper(loader).convertValue(obj)
        }
    }

}
