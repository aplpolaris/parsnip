/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * Services.kt
 * edu.jhuapl.util:ekotlin-utils
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
package edu.jhuapl.util.services

import edu.jhuapl.utilkt.core.info
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.util.*

/** Cache containing service loader instances  */
private val SERVICE_LOADER_CACHE = mutableMapOf<Class<*>, ServiceLoader<*>>()

const val CONFIG_DIR = "config/"
const val MODULES_DIR = CONFIG_DIR + "modules/"

/**
 * Return instances of given type using Java's [ServiceLoader]. Looks for registered services in the current class loader,
 * but also in a runtime directory as described in `EConfig`.
 * @param <T> the service type
 * @param cls the service type
 * @return list of service instances
 */
fun <T : Any> services(cls: Class<T>) = serviceLoader(cls).toList()

/**
 * Return instances of given type using Java's [ServiceLoader]. Looks for registered services in the current class loader,
 * but also in a runtime directory as described in `EConfig`.
 * @param <T> the service type
 * @return list of service instances
 */
inline fun <reified T : Any> services() = serviceLoader(T::class.java).toList()

/**
 * Get [ServiceLoader] for given type, and cache the returned value. The next time the method is called with the same argument,
 * the cached value will be returned.
 * @param <T> the service type
 * @param cls the service type
 * @return service loader
 */
@Synchronized
fun <T : Any> serviceLoader(cls: Class<T>): ServiceLoader<T> {
    return SERVICE_LOADER_CACHE.getOrPut(cls) { ServiceLoader.load(cls, RuntimeServiceClassLoader) } as ServiceLoader<T>
}

/** Class loader that references both the runtime config/ directory and the current classloader. */
object RuntimeServiceClassLoader : URLClassLoader(runtimeServiceJarUrls(), ClassLoader.getSystemClassLoader())

private fun runtimeServiceJarUrls(): Array<URL> {
    val jars = File(MODULES_DIR).listFiles { _, name -> name.endsWith(".jar") }
            ?.map { it.toURI().toURL() }?.toTypedArray()
            ?: emptyArray()
    info<RuntimeServiceClassLoader>(jars.joinToString(prefix = "Discovered module jars: \n - ", separator = "\n - "))
    return jars
}
