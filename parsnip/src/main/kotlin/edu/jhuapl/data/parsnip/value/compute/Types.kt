package edu.jhuapl.data.parsnip.value.compute

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import edu.jhuapl.data.parsnip.decode.Decoder
import edu.jhuapl.data.parsnip.decode.DecoderException
import edu.jhuapl.data.parsnip.decode.InstantDecoder
import edu.jhuapl.data.parsnip.decode.StandardDecoders
import edu.jhuapl.data.parsnip.value.ValueCompute
import edu.jhuapl.util.types.SimpleValue
import edu.jhuapl.util.types.convertTo
import edu.jhuapl.util.types.fromShortName
import edu.jhuapl.util.types.toShortName
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * Types.kt
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

/**
 * Converts value to one of given type, if possible. Uses [convertTo] to attempt conversion from any source type to the
 * target type, returning null if unable to convert. This method may be very slow for converting date/time strings to
 * date/time objects.
 */
data class As @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(@JsonIgnore var type: Class<*>) : ValueCompute<Any>, SimpleValue {
    @JsonCreator
    constructor(type: String) : this(fromShortName(type))

    override val simpleValue: String
        get() = type.toShortName()

    val typeName: String
        get() = type.simpleName

    override fun invoke(input: Any?): Any? = input?.convertTo(type)
}

/**
 * Converts value using a given decoder. Assumes the input is a string. Otherwise uses its string representation.
 */
data class Decode @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(@JsonIgnore var decoder: Decoder<*>) : ValueCompute<Any>, SimpleValue {
    @JsonCreator
    constructor(type: String) : this(StandardDecoders.valueOf(type))

    override val simpleValue: Any
        get() = with (decoder) { if (this is StandardDecoders) name else this }

    override fun invoke(input: Any?): Any? = decoder.decode(input.toString())
}

/**
 * Converts value to [Instant] using given format string.
 */
class DecodeInstant @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(_format: String) : ValueCompute<Instant>, SimpleValue {

    var format = ""
        set(value) {
            decoder = InstantDecoder(value)
            field = value
        }
    var decoder: InstantDecoder? = null
    init {
        format = _format
    }

    override val simpleValue: String
        get() = format

    override fun invoke(p1: Any?): Instant? = try {
        decoder?.decode(p1.toString())
    } catch (x: DecoderException) {
        null
    }

}
