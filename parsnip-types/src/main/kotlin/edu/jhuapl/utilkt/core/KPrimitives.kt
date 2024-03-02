/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * KPrimitives.kt
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

/** Apply standard Java trim to string. */
fun String.javaTrim() = this.trim { it <= ' ' }

/** Infix string for providing alternative to blank string. */
infix fun String?.orIfBlank(x: String) = if (isNullOrBlank()) x else this

/** Infix string for providing alternative to empty string. */
fun String?.orIfBlank(op: () -> String) = if (isNullOrBlank()) op() else this

/** Infix string for providing alternative to empty string. */
infix fun String?.orIfEmpty(x: String) = if (isNullOrEmpty()) x else this

/** Infix string for providing alternative to empty string. */
fun String?.orIfEmpty(op: () -> String) = if (isNullOrEmpty()) op() else this

/** Utility function to optionally execute a statement depending on the value of a Boolean. "Null" is the same as false. */
inline fun Boolean?.ifTrue(f: () -> Unit): Boolean {
    if (this != null && this) {
        f()
    }
    return this ?: false
}
