/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * KResources.kt
 * edu.jhuapl.util:ekotlin-utils
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
package edu.jhuapl.utilkt.core

import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import java.nio.file.FileSystems

/** Get file extension associated with [URL]'s path. */
val URL.extension: String
    get() = path.substringAfterLast('.', "")

/** Alternate loader that allows use of a custom class loader by specifying a type. */
inline fun <reified C : Any, O : Any> loadResourcesByClassLoader(dir: String, ext: String, loader: (URL) -> O?) =
        packageResourcesByClassLoader<C>(dir, ext).mapNotNull {
            try {
                loader(it)
            } catch (ex: IOException) {
                loggerFor<C>().warning("Could not load from $it")
                null
            }
        }

/** Loads resources with given extension from package  */
inline fun <reified C : Any> packageResourcesByClassLoader(pkg: String, ext: String?): List<URL> = try {
    C::class.java.classLoader.getResources(pkg).toList().map { it.toURI() }
            .flatMap {
                when (it.scheme) {
                    "jar" -> it.resourcesInJarPackage<C>(pkg)
                    else -> it.resourcesInDir()
                }
            }.filter { ext == null || it.toString().endsWith(ext) }
} catch (ex: URISyntaxException) {
    loggerFor<C>().fine("URI error", ex)
    emptyList()
} catch (ex: IOException) {
    loggerFor<C>().fine("URI error", ex)
    emptyList()
}

/** Loads resources with given extension from package URI  */
inline fun <reified C : Any> URI.resourcesInJarPackage(pkg: String): List<URL> = try {
    FileSystems.newFileSystem(this, emptyMap<String, Any>()).use { it.getPath(pkg).resourcesInDir() }
} catch (ex: IOException) {
    loggerFor<C>().fine("IO error with $this, $pkg", ex)
    emptyList()
}
