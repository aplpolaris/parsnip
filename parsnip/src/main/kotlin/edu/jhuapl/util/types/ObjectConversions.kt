/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * ObjectConversions.kt
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
package edu.jhuapl.util.types

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.MissingNode
import edu.jhuapl.utilkt.core.warning

/** Utilities for converting between common object types. */
private class ObjectConverters

/**
 * Returns value converted to target type, possibly using [ObjectMapper]. Always converts an object. If the provided type
 * is null, returns the original input (except for the case where this is [MissingNode], which always returns null).
 * @param type target type
 * @return converted object, or null if unable to convert
 */
fun <C> Any?.convertTo(type: Class<C>): C? {
    return when {
        type == Unit::class.java -> Unit
        this == null -> null
        this is MissingNode -> null
        this is JsonNode -> mapperConvertTo(type)
        type == String::class.java -> toString()
        type == Any::class.java -> this
        type == Long::class.java -> toNumberOrEpochLong()
        type == java.lang.Long::class.java -> toNumberOrEpochLong()
        type.numberType() -> toNumberOrNull(type as Class<out Number>)
        type.timeType() -> toCachedDateTime(type)
        else -> mapperConvertTo(type)
    } as C?
}

/** Converts value to a long, by either parsing as a number or as a date/time, then converting to a number. */
private fun Any.toNumberOrEpochLong(): Long? = toNumberOrNull(Long::class.java) ?: toEpochMilli()

/** Returns value converted to target type using [ObjectMapper], null if conversion fails */
internal fun <C> Any.mapperConvertTo(targetType: Class<C>): C? {
    return try {
        ObjectMapper().convertValue(this, targetType)
    } catch (x: IllegalArgumentException) {
        warning<ObjectConverters>("Unable to convert $this to $targetType", x)
        null
    } catch (x: ClassCastException) {
        warning<ObjectConverters>("Unable to convert $this to $targetType", x)
        null
    }
}

