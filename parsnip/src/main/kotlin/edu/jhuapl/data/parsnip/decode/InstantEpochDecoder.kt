/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * InstantEpochDecoder.kt
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
package edu.jhuapl.data.parsnip.decode

/**
 * Converts a date/time string to epoch milliseconds, after first converting to an [Instant].
 */
class InstantEpochDecoder(pattern: String = "yyyy-MM-dd'T'HH:mm:ss") : Decoder<Long> {

    private var delegate = InstantDecoder(pattern)
    var pattern: String = pattern
        set(pattern) {
            field = pattern
            delegate.pattern = pattern
        }

    override val javaType = Long::class.java

    override fun decode(input: String) = delegate.decode(input).toEpochMilli()

}
