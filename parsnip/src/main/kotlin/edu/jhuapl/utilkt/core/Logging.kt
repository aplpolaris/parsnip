/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * Logging.kt
 * edu.jhuapl.util:ekotlin-utils
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
package edu.jhuapl.utilkt.core

import java.util.logging.Level
import java.util.logging.Logger

//region LOGGER SHORTCUTS

inline fun <reified T : Any> loggerFor(): Logger = Logger.getLogger(T::class.java.name)

fun Logger.severe(msg: String, x: Throwable? = null) = log(Level.SEVERE, msg, x)
fun Logger.warning(msg: String, x: Throwable? = null) = log(Level.WARNING, msg, x)
fun Logger.info(msg: String, x: Throwable? = null) = log(Level.INFO, msg, x)
fun Logger.fine(msg: String, x: Throwable? = null) = log(Level.FINE, msg, x)

//endregion

//region CLASS LOGGERS

inline fun <reified T : Any> log(level: Level, msg: String, x: Exception? = null) = loggerFor<T>().log(level, msg, x)

inline fun <reified T : Any> severe(msg: String, x: Throwable? = null) = log<T>(Level.SEVERE, msg, x)
inline fun <reified T : Any> warning(msg: String, x: Throwable? = null) = log<T>(Level.WARNING, msg, x)
inline fun <reified T : Any> info(msg: String, x: Throwable? = null) = log<T>(Level.INFO, msg, x)
inline fun <reified T : Any> fine(msg: String, x: Throwable? = null) = log<T>(Level.FINE, msg, x)

inline fun <reified T : Any> log(level: Level, template: String, vararg args: Any?) = loggerFor<T>().log(level, template, args)
inline fun <reified T : Any> severe(template: String, vararg args: Any?) = log<T>(Level.SEVERE, template, args)
inline fun <reified T : Any> warning(template: String, vararg args: Any?) = log<T>(Level.WARNING, template, args)
inline fun <reified T : Any> info(template: String, vararg args: Any?) = log<T>(Level.INFO, template, args)
inline fun <reified T : Any> fine(template: String, vararg args: Any?) = log<T>(Level.FINE, template, args)

//endregion

//region CONDITIONAL LOGGING

fun <T> Iterable<T>.logWhen(condition: (T) -> Boolean, block: (T) -> Unit) = onEach {
    when {
        condition(it) -> block(it)
    }
}

fun <T> Iterable<T>.logWhenNot(condition: (T) -> Boolean, block: (T) -> Unit) = logWhen({ !condition(it) }, block)

//endregion
