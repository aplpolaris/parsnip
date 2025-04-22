/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * KCollections.kt
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
package edu.jhuapl.utilkt.core

//region ITERABLE UTILS

/** Convert iterable to a map using given assignment function. */
fun <T, K, V> Iterable<T>.toMap(pairs: (T) -> Pair<K, V>): Map<K, V> = map(pairs).toMap()
/** Convert iterable to a map using given assignment function. */
fun <T, K, V> Iterable<T>.toMutableMap(pairs: (T) -> Pair<K, V>): MutableMap<K, V> = map(pairs).toMap().toMutableMap()
/** Transform individual values, wrapping results in [Result] to keep success/failure and exception information. */
fun <T, R> Iterable<T>.mapCatching(transform: (T) -> R): List<Result<R>> = map { t -> runCatching<R> { transform(t) } }

//endregion
