/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * ParsnipMapper.kt
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

import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import edu.jhuapl.data.parsnip.dataset.DataSetCompute
import edu.jhuapl.data.parsnip.dataset.DataSetFilter
import edu.jhuapl.data.parsnip.dataset.DataSetTransform
import edu.jhuapl.data.parsnip.datum.DatumCompute
import edu.jhuapl.data.parsnip.datum.DatumTransform
import edu.jhuapl.data.parsnip.datum.MultiDatumTransform
import edu.jhuapl.data.parsnip.datum.filter.And
import edu.jhuapl.data.parsnip.datum.filter.Not
import edu.jhuapl.data.parsnip.datum.filter.Or
import edu.jhuapl.data.parsnip.datum.transform.Augment
import edu.jhuapl.data.parsnip.datum.transform.Create
import edu.jhuapl.data.parsnip.set.ValueSetCompute
import edu.jhuapl.data.parsnip.set.ValueSetFilter
import edu.jhuapl.data.parsnip.set.ValueSetTransform
import edu.jhuapl.data.parsnip.value.ValueCompute
import edu.jhuapl.data.parsnip.value.ValueFilter
import edu.jhuapl.data.parsnip.value.compute.ValueFilterCompute
import edu.jhuapl.util.services.RuntimeServiceClassLoader
import edu.jhuapl.util.types.serviceFromShortName

private const val VALUE_COMPUTE_DEFAULT_PACKAGE = "edu.jhuapl.data.parsnip.value.compute"
private const val VALUE_FILTER_DEFAULT_PACKAGE = "edu.jhuapl.data.parsnip.value.filter"

private const val SET_COMPUTE_DEFAULT_PACKAGE = "edu.jhuapl.data.parsnip.set.compute"
private const val SET_FILTER_DEFAULT_PACKAGE = "edu.jhuapl.data.parsnip.set.filter"
private const val SET_TRANSFORM_DEFAULT_PACKAGE = "edu.jhuapl.data.parsnip.set.transform"

private const val DATUM_COMPUTE_DEFAULT_PACKAGE = "edu.jhuapl.data.parsnip.datum.compute"
private const val DATUM_FILTER_DEFAULT_PACKAGE = "edu.jhuapl.data.parsnip.datum.filter"
internal const val DATUM_TRANSFORM_DEFAULT_PACKAGE = "edu.jhuapl.data.parsnip.datum.transform"

private const val DATASET_COMPUTE_DEFAULT_PACKAGE = "edu.jhuapl.data.parsnip.dataset.compute"
private const val DATASET_FILTER_DEFAULT_PACKAGE = "edu.jhuapl.data.parsnip.dataset.filter"
private const val DATASET_TRANSFORM_DEFAULT_PACKAGE = "edu.jhuapl.data.parsnip.dataset.transform"

private const val DIMENSION_CONSTRAINT_DEFAULT_PACKAGE = "edu.jhuapl.data.parsnip.gen"

/**
 * Mapper instance with objects required for Parsnip object serialization already registered.
 */
object ParsnipMapper: CustomParsnipMapper(RuntimeServiceClassLoader)

/**
 * Class that allows specification of a custom class loader.
 * @param loader class loader used for looking up classes by id/string
 */
open class CustomParsnipMapper(loader: ClassLoader, mapper: ObjectMapper = ObjectMapper()): ObjectMapper(mapper) {
    init {
        registerModule(KotlinModule.Builder().build())
        registerModule(parsnipModule(loader))
        registerModule(commonTypeModule())
    }
}

/**
 * Module for a few basic types, including [Color] and [Class].
 * @return module
 */
fun commonTypeModule() = SimpleModule().apply {
    serialize(ColorSerializer)
    deserialize(ColorDeserializer)
    serialize(ClassSerializer)
    deserialize(ClassDeserializer)
}

/**
 * Get module for serializing/deserializing Parsnip data transformation objects.
 * @param loader class loader used for looking up classes by id/string
 * @return module
 */
fun parsnipModule(loader: ClassLoader = RuntimeServiceClassLoader) = SimpleModule().apply {
    // custom implementations for a few specific types
    serialize<Create>(CreateSerializer)
    serialize<Augment>(CreateSerializer)

    deserialize(CreateDeserializer(loader))
    deserialize(AugmentDeserializer(loader))

    // override default serialization to use simple values if present
    serialize<ValueCompute<*>>(SimpleValueSerializer)
    serialize<ValueFilter>(SimpleValueSerializer)
    serialize<ValueSetCompute<*>>(SimpleValueSerializer)
    serialize<ValueSetFilter>(SimpleValueSerializer)
    serialize<ValueSetTransform>(SimpleValueSerializer)
    serialize<DatumCompute<*>>(SimpleValueSerializer)
//    sm.addSerializer(DatumFilter::class.java, SimpleValueSerializer())
    serialize<DatumTransform>(SimpleValueSerializer)
    serialize<MultiDatumTransform>(MultiDatumTransformSerializer)
    serialize<DataSetCompute<*>>(SimpleValueSerializer)
    serialize<DataSetFilter>(SimpleValueSerializer)
    serialize<DataSetTransform>(SimpleValueSerializer)

    // deserialization must reconstitute packages, so custom per type
    deserialize(ValueComputeDeserializer(loader))
    deserialize(ValueSetComputeDeserializer(loader))
    deserialize(ValueSetTransformDeserializer(loader))
    deserialize(DatumComputeDeserializer(loader))
    deserialize(DatumTransformDeserializer(loader))
    deserialize(MultiDatumTransformDeserializer(loader))
    deserialize(DataSetComputeDeserializer(loader))
    deserialize(DataSetTransformDeserializer(loader))

    // filters have special implementations to make them more concise
    // todo - most of these need to be implemented
    deserialize(ValueFilterDeserializer())
//    sm.addDeserializer(ValueSetFilter::class.java, ValueSetFilterDeserializer())
//    sm.addDeserializer(DatasetFilter::class.java, DatasetFilterDeserializer())

    serialize<And>(SimpleValueSerializer)
    serialize<Or>(SimpleValueSerializer)
    serialize<Not>(SimpleValueSerializer)
    deserialize(DatumFilterDeserializer())
}

//region DESERIALIZER OBJECTS

/** Value filters can be used in place of [ValueCompute]. */
class ValueComputeDeserializer(loader: ClassLoader) : NameObjectDeserializerWithAlternate<ValueCompute<*>, ValueFilter>(
    { serviceFromShortName(loader, it, VALUE_COMPUTE_DEFAULT_PACKAGE) },
    { serviceFromShortName(loader, it, VALUE_FILTER_DEFAULT_PACKAGE) },
    { ValueFilterCompute(it) })

class DatumComputeDeserializer(loader: ClassLoader) : NameObjectDeserializer<DatumCompute<*>>({ serviceFromShortName(loader, it, DATUM_COMPUTE_DEFAULT_PACKAGE) })
class DatumTransformDeserializer(loader: ClassLoader) : NameObjectDeserializer<DatumTransform>({ serviceFromShortName(loader, it, DATUM_TRANSFORM_DEFAULT_PACKAGE) })

class ValueSetComputeDeserializer(loader: ClassLoader) : NameObjectDeserializer<ValueSetCompute<*>>({ serviceFromShortName(loader, it, SET_COMPUTE_DEFAULT_PACKAGE) })
class ValueSetTransformDeserializer(loader: ClassLoader) : NameObjectDeserializer<ValueSetTransform>({ serviceFromShortName(loader, it, SET_TRANSFORM_DEFAULT_PACKAGE) })

class DataSetComputeDeserializer(loader: ClassLoader) : NameObjectDeserializer<DataSetCompute<*>>({ serviceFromShortName(loader, it, DATASET_COMPUTE_DEFAULT_PACKAGE) })
class DataSetTransformDeserializer(loader: ClassLoader) : NameObjectDeserializer<DataSetTransform>({ serviceFromShortName(loader, it, DATASET_TRANSFORM_DEFAULT_PACKAGE) })

//endregion

//region TYPE LOOKUPS

internal inline fun <reified C: Any> serviceFromShortName(classLoader: ClassLoader, s: String, pkg: String) = serviceFromShortName(classLoader, s, C::class.java, pkg)

/** This is a JDK9+ function that we are including here to work with JDK8 builds. */
internal fun packageName(cls: Class<*>): String {
    var typeCls = cls
    while (typeCls.isArray) {
        typeCls = typeCls.componentType
    }
    return if (typeCls.isPrimitive) {
        "java.lang"
    } else {
        typeCls.name.substringBeforeLast('.', "")
    }
}

//endregion

/** Extension function to simplify adding serializers to module. */
private inline fun <reified T> SimpleModule.serialize(serializer: JsonSerializer<in T>) { addSerializer(T::class.java, serializer) }

/** Extension function to simplify adding deserializers to module. */
private inline fun <reified T> SimpleModule.deserialize(deserializer: JsonDeserializer<out T>) { addDeserializer(T::class.java, deserializer) }
