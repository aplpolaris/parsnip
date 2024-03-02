/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * Kio.kt
 * edu.jhuapl.util:ekotlin-utils
 * %%
 * Copyright (C) 2024 Johns Hopkins University Applied Physics Laboratory
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

import java.io.File
import java.net.URI
import java.net.URL
import java.nio.file.DirectoryStream
import java.nio.file.Files
import java.nio.file.Path

//region IO UTILS

fun String.fileExtension() = File(this).extension
fun String.withAlternateExtension(ext: String) = "${File(this).nameWithoutExtension}.$ext"

/** Converts [URI] to [Path]. */
fun URI.asPath() = Path.of(this)
/** Loads resources with given extension from file URI. Suppresses any errors. */
fun URI.resourcesInDir(): List<URL> = asPath().stream().asUrls().mapNotNull { it.getOrNull() }

/** Streams directory via glob. */
fun Path.stream(glob: String = "*") = Files.newDirectoryStream(this, glob)
/** Get list of paths in a given directory, with an optional custom glob. */
fun Path.directoryList(glob: String = "*"): List<Path> = stream(glob).toList()
/** Loads resources from path, suppressing any errors for individual files. */
fun Path.resourcesInDir(): List<URL> = stream().asUrls().mapNotNull { it.getOrNull() }

/** Get list of resources in path, as URLs */
fun DirectoryStream<Path>.asUrls(): List<Result<URL>> = use { it.mapCatching { path -> path.toUri().toURL() } }

//endregion
