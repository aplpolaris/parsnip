/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * CreateSerializer.kt
 * edu.jhuapl.data:parsnip
 * %%
 * Copyright (C) 2019 - 2026 Johns Hopkins University Applied Physics Laboratory
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

import tools.jackson.core.JsonGenerator
import tools.jackson.databind.ValueSerializer
import tools.jackson.databind.SerializationContext
import edu.jhuapl.data.parsnip.datum.compute.Constant
import edu.jhuapl.data.parsnip.datum.transform.Create
import edu.jhuapl.data.parsnip.datum.transform.FieldEncode
import edu.jhuapl.data.parsnip.value.ValueFilter
import edu.jhuapl.data.parsnip.value.compute.TargetMultipleFields
import edu.jhuapl.data.parsnip.value.compute.ValueFilterCompute
import edu.jhuapl.util.types.SimpleValue

/**
 * Handles serialization of [Create].
 */
object CreateSerializer : ValueSerializer<Create>() {
    override fun serialize(value: Create?, gen: JsonGenerator, serializers: SerializationContext) {
        when (value) {
            null -> gen.writeNull()
            else -> gen.writePOJO(delegateMap(value, gen, serializers))
        }
    }

    private fun delegateMap(value: Create, gen: JsonGenerator, serializerProvider: SerializationContext)
            = value.fields.associateBy({ it.targetSingle }, { delegateSerializableObject(it, gen, serializerProvider) })

    /**
     * Encodes transforming content of [FieldEncode], which will be just a simple object for "from" if there are no
     * process steps, or a list where the first value is "from" and remainder are "process" steps otherwise. If the encode
     * targets multiple fields, an additional item is added to the end of the list of content.
     */
    private fun delegateSerializableObject(value: FieldEncode<*>, gen: JsonGenerator, serializerProvider: SerializationContext): Any? = when {
        !value.targetMultipleFields && value.process.isEmpty() && value.from is Constant -> (value.from as Constant).value
        !value.targetMultipleFields && value.process.isEmpty() -> value.from
        value.targetMultipleFields -> listOrMapWithUniqueFields(listOf(value.from) + value.process + TargetMultipleFields(value.target), gen, serializerProvider)
        else -> listOrMapWithUniqueFields(listOf(value.from) + value.process, gen, serializerProvider)
    }

    /**
     * Returns object for serialization in one of two ways: if no object types are repeated in the list, returns a
     * type-to-instance map that allows a more human-readable representation, with the type implicit in the key.
     * Otherwise, when object types are repeated, encodes result as a list, requiring the object type information to be
     * encoded with the list element.
     */
    private fun listOrMapWithUniqueFields(list: List<Any>, gen: JsonGenerator, serializerProvider: SerializationContext): Any {
        val setOfTypes = list.map { it::class.java }.toSet()
        return if (setOfTypes.size == list.size) {
            list.associateBy(
                { if (it is ValueFilterCompute) it.filter::class.java.simpleName else it::class.java.simpleName },
                { simplestValue(it, gen, serializerProvider) } )
        } else {
            list
        }
    }

    /** Get the simple value representation of the object, if possible. */
    private fun simplestValue(it: Any, gen: JsonGenerator, serializerProvider: SerializationContext): Any = when (it) {
        is SimpleValue -> it.simpleSerializableValue(gen, serializerProvider)
        is ValueFilterCompute -> simplestValue(it.filter, gen, serializerProvider)
        else -> it.toSerializableMap(gen, serializerProvider)
    }

}
