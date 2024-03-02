/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * ObjectPointers.kt
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
import edu.jhuapl.utilkt.core.javaTrim

/**
 * Get value at expected location in message body. Expects it to be of the
 * provided type. If not found, or found but the type is wrong, returns a null value.
 * @param jsonPointer location (may omit the starting / if desired)
 * @return value, or missing if it's not there or there's an error
 */
fun Any.atPointer(jsonPointer: String?): Any? = atPointer(jsonPointer, null)

/**
 * Get value at expected location in message body. Expects it to be of the
 * provided type. If not found, or found but the type is wrong, returns a null value.
 * @param <C> expected type
 * @param jsonPointer location (may omit the starting / if desired)
 * @param type expected type... if null will not convert value
 * @return value, or missing if it's not there or there's an error
 */
fun Any.atPointer(jsonPointer: String?, type: Class<*>?): Any? {
    val targetType = type ?: Object::class.java
    val validPointer = when {
        jsonPointer.isNullOrEmpty() -> "/"
        jsonPointer.startsWith("/") -> jsonPointer.javaTrim()
        else -> "/${jsonPointer.javaTrim()}"
    }

    return when {
        validPointer == "/" -> convertTo(targetType)
        this is Map<*, *> -> mapAtPointer(validPointer)?.convertTo(targetType)
        else -> mapperConvertTo(JsonNode::class.java)?.at(validPointer)?.convertTo(targetType)
    }
}

/** Puts content in a map, where the result may be multiply nested if any keys in the provided datum are JSON pointers. */
fun MutableMap<String, Any?>.nestedPutAll(from: Map<String, Any?>) = from.forEach { (t, u) -> nestedPut(t, u) }

/** Puts content in a map, where the result may be nested if the parameter [jsonPointer] is a JSON pointer. */
fun MutableMap<String, Any?>.nestedPut(jsonPointer: String, u: Any?) {
    if (jsonPointer.startsWith("/")) {
        when (val i = jsonPointer.indexOf("/", 1)) {
            -1 -> put(jsonPointer.substring(1), u)
            else -> {
                val nestedPathPointer = jsonPointer.substring(1 until i)
                putIntermediate(nestedPathPointer, get(nestedPathPointer), jsonPointer.substring(i), u)
            }
        }
    } else {
        put(jsonPointer, u)
    }
}

//region MAP OBJECT POINTERS

/** Get nested map content at given pointer. Pointer must start with "/". For use only in this file. */
private fun Map<*, *>.mapAtPointer(jsonPointer: String): Any? {
    require(jsonPointer.startsWith('/'))
    if (jsonPointer in this) {
        return get(jsonPointer)
    } else if (jsonPointer.substring(1) in this) {
        return get(jsonPointer.substring(1))
    }
    return when (val slash2 = jsonPointer.indexOf('/', 1)) {
        -1 -> null
        else -> this.atPointer(jsonPointer.substring(0, slash2))?.atPointer(jsonPointer.substring(slash2))
    }
}

/**
 * Sets an intermediate value within the object tree.
 * @param headPointer pointer to head (current) object
 * @param headObject value at head pointer
 * @param tailPointer pointer relative to the head object
 * @param u final value to add to datum
 */
private fun MutableMap<String, Any?>.putIntermediate(headPointer: String, headObject: Any?, tailPointer: String, u: Any?) {
    when (headObject) {
        is List<*> -> headObject.nestedPut(tailPointer, u)
        is MutableMap<*, *> -> (headObject as MutableMap<String, Any?>).nestedPut(tailPointer, u)
        null -> {
            val newMap = mutableMapOf<String, Any?>()
            newMap.nestedPut(tailPointer, u)
            put(headPointer, newMap)
        }
        else -> throw IllegalArgumentException("Expected a mutable map at $headPointer but was $headObject")
    }
}

//endregion

//region LIST OBJECT POINTERS

/** Puts content in a list, where the result may be nested if the parameter [jsonPointer] is a JSON pointer. */
private fun List<*>.nestedPut(jsonPointer: String, u: Any?) {
    if (jsonPointer.startsWith("/")) {
        when (val i = jsonPointer.indexOf("/", 1)) {
            -1 -> tryPutByListIndex(jsonPointer.substring(1), u)
            else -> {
                val nestedPathPointer = jsonPointer.substring(1 until i)
                putIntermediate(nestedPathPointer, getByListIndex(nestedPathPointer), jsonPointer.substring(i), u)
            }
        }
    } else {
        tryPutByListIndex(jsonPointer, u)
    }
}

/**
 * Sets an intermediate value within the object tree.
 * @param headPointer pointer to head (current) object
 * @param headObject value at head pointer
 * @param tailPointer pointer relative to the head object
 * @param u final value to add to datum
 */
private fun List<*>.putIntermediate(headPointer: String, headObject: Any?, tailPointer: String, u: Any?) {
    when (headObject) {
        is List<*> -> headObject.nestedPut(tailPointer, u)
        is MutableMap<*, *> -> (headObject as MutableMap<String, Any?>).nestedPut(tailPointer, u)
        else -> throw IllegalArgumentException("Expected a mutable list at $headPointer but was $headObject")
    }
}

private fun List<*>.getByListIndex(index: String) = index.toIntOrNull()?.let { get(it) } ?: throw IllegalArgumentException("Invalid index: $index")

private fun List<*>.tryPutByListIndex(index: String, u: Any?) {
    if (this is MutableList<*>) {
        (this as MutableList<Any?>).putByListIndex(index, u)
    } else {
        throw IllegalArgumentException("Invalid index: $index")
    }
}

private fun <E> MutableList<E>.putByListIndex(index: String, u: E) {
    when (index) {
        "-" -> add(u)
        else -> index.toIntOrNull()?.let { set(it, u) } ?: throw IllegalArgumentException("Invalid index: $index")
    }
}

//endregion
