/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * Decoder.kt
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
 * Provides logic for decoding a type from a string.
 */
interface Decoder<out X> {

    /** The Java type of returned object. */
    val javaType: Class<out X>

    /**
     * Perform decoding.
     * @param input input string
     * @return decoded value
     * @throws DecoderException if there's a decoding error
     */
    @Throws(DecoderException::class)
    fun decode(input: String): X

}
