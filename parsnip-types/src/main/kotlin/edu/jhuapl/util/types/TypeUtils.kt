/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * TypeUtils.kt
 * edu.jhuapl.data:parsnip
 * %%
 * Copyright (C) 2024 - 2025 Johns Hopkins University Applied Physics Laboratory
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

import edu.jhuapl.util.services.services
import edu.jhuapl.utilkt.core.fine
import edu.jhuapl.utilkt.core.severe

/** Utilities for working with Java types. */
object TypeUtils

//region LOOKUPS

private val JAVA_LANG_TYPES = setOf("Long", "Integer", "Short", "Byte", "Float", "Double", "Boolean", "String")
private val JAVA_TIME_TYPES = setOf("Instant", "LocalDate", "LocalTime", "LocalDateTime")
private val JAVA_UTIL_TYPES = setOf("Date")

private val PRIMITIVE_LOOKUP = mapOf(
        "long" to Long::class.javaPrimitiveType,
        "int" to Int::class.javaPrimitiveType,
        "short" to Short::class.javaPrimitiveType,
        "byte" to Byte::class.javaPrimitiveType,
        "float" to Float::class.javaPrimitiveType,
        "double" to Double::class.javaPrimitiveType,
        "boolean" to Boolean::class.javaPrimitiveType,
        "void" to Void.TYPE)

//endregion

/**
 * Tests if this class is a primitive type with the given wrapper type.
 * @param wrapperType possible wrapper type
 * @return true if so
 */
fun Class<*>.isPrimitiveWithWrapperType(wrapperType: Class<*>) = isPrimitive && kotlin.javaObjectType == wrapperType

/**
 * Generate a shorthand name for the given class. Uses only the class name for classes in `java.lang`, `java.util`, and `java.time`,
 * and the full class and path name otherwise.
 * @return the short name
 */
fun Class<*>.toShortName() = when {
    name.startsWith("java.lang.") && JAVA_LANG_TYPES.contains(name.substring(10)) -> name.substring(10)
    name.startsWith("java.util.") && JAVA_UTIL_TYPES.contains(name.substring(10)) -> name.substring(10)
    name.startsWith("java.time.") && JAVA_TIME_TYPES.contains(name.substring(10)) -> name.substring(10)
    else -> name
}!!

/**
 * Returns the class corresponding to the given shorthand shortClassName. Allows omission of `java.lang` and `java.util` from some
 * of the classes in those packages. Otherwise requires the shortClassName to have the full package path.
 * @param shortClassName the class shortClassName
 * @return class
 * @throws ClassNotFoundException if the class can't be found
 */
@Throws(ClassNotFoundException::class)
fun fromShortName(shortClassName: String): Class<*> = when {
    PRIMITIVE_LOOKUP.containsKey(shortClassName) -> PRIMITIVE_LOOKUP[shortClassName] ?: error("Impossible")
    JAVA_LANG_TYPES.contains(shortClassName) -> Class.forName("java.lang.$shortClassName")
    JAVA_UTIL_TYPES.contains(shortClassName) -> Class.forName("java.util.$shortClassName")
    JAVA_TIME_TYPES.contains(shortClassName) -> Class.forName("java.time.$shortClassName")
    else -> Class.forName(shortClassName)
}

/**
 * Returns the class corresponding to the given shorthand name. Allows omission of package names if present in the same
 * package as the given type, or there is a service registered via [java.util.ServiceLoader] with the given short type name.
 * @param <T> generic type
 * @param classLoader the class loader to use to find service from name
 * @param shortClassName short type name
 * @param type return type
 * @param packages additional packages to look for simple types
 * @return instance if found
 * @throws ClassNotFoundException if the class can't be found
 */
fun <T : Any> serviceFromShortName(classLoader: ClassLoader, shortClassName: String, type: Class<T>, vararg packages: String): Class<out T> {
    // iterate through list of classes with package names added
    // ignore if class not found, but log an error if it's the wrong type
    val qualifiedNamesToTry = listOf(shortClassName) + packages.map { "$it.$shortClassName" }
    for (name in qualifiedNamesToTry) {
        try {
            val c = classLoader.loadClass(name)
//            val c = RuntimeServiceClassLoader.loadClass(name)
            if (c != null && type.isAssignableFrom(c)) {
                return c as Class<T>
            } else if (c != null) {
                severe<TypeUtils>("Expected type $type but was $c")
            }
        } catch (x: ClassNotFoundException) {
            fine<TypeUtils>("Expected in most cases")
        }
    }

    // failing the above, look through available services for an instant of this type
    val c = services(type).firstOrNull { it::class.java.simpleName == shortClassName }?.javaClass
    if (c != null && type.isAssignableFrom(c)) {
        return c
    } else if (c != null) {
        severe<TypeUtils>("Expected type $type but was $c")
    }
    throw ClassNotFoundException("Unable to find object $shortClassName of type $type")
}

/**
 * Returns the class corresponding to the given shorthand name. Allows omission of package names if present in the same
 * package as the given type, or there is a service registered via [java.util.ServiceLoader] with the given short type name.
 * @param <T> generic type
 * @param classLoader the class loader to use to find service from name
 * @param shortClassName short type name
 * @param type return type
 * @param packages additional packages to look for simple types
 * @return instance if found, null otherwise
 */
fun <T : Any> tryServiceFromShortName(classLoader: ClassLoader, shortClassName: String, type: Class<T>, vararg packages: String): Class<out T>? = try {
    serviceFromShortName(classLoader, shortClassName, type, *packages)
} catch (x: ClassNotFoundException) {
    null
}
