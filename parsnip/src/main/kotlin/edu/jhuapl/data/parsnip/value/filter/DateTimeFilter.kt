package edu.jhuapl.data.parsnip.value.filter

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * DateTimeFilter.kt
 * edu.jhuapl.data:parsnip
 * %%
 * Copyright (C) 2019 - 2026 Johns Hopkins University Applied Physics Laboratory
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

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import edu.jhuapl.data.parsnip.value.ValueFilter
import edu.jhuapl.util.types.SimpleValue
import edu.jhuapl.util.types.cachedInstantFrom
import edu.jhuapl.util.types.toInstantOrNull
import edu.jhuapl.util.types.toLocalDateOrNull
import java.time.Instant
import java.time.LocalDate

/** Tests if an input can be interpreted as a local date (uses all supported date and date-time formats). */
object IsDate : ValueFilter {
    override fun invoke(p: Any?) = p != null && when (p) {
        is LocalDate -> true
        else -> p.toString().toLocalDateOrNull() != null
    }
}

/** Tests if an input can be interpreted as an instant (date and time, or a date-only value). */
object IsDateTime : ValueFilter {
    override fun invoke(p: Any?) = cachedInstantFrom(p) != null
}

/** Tests if an input date/time is strictly before the given timestamp. */
class Before @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(var value: String) : ValueFilter, SimpleValue {

    @JsonIgnore
    val instant: Instant = value.toInstantOrNull()
        ?: throw IllegalArgumentException("Invalid timestamp: $value")

    override val simpleValue get() = value

    override fun invoke(p: Any?) = cachedInstantFrom(p)?.isBefore(instant) == true
}

/** Tests if an input date/time is strictly after the given timestamp. */
class After @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(var value: String) : ValueFilter, SimpleValue {

    @JsonIgnore
    val instant: Instant = value.toInstantOrNull()
        ?: throw IllegalArgumentException("Invalid timestamp: $value")

    override val simpleValue get() = value

    override fun invoke(p: Any?) = cachedInstantFrom(p)?.isAfter(instant) == true
}

/** Tests if an input date/time is between the given start and end timestamps (inclusive on both ends). */
class Between @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(vararg range: Any) : ValueFilter, SimpleValue {

    var range = listOf(*range)

    @JsonIgnore
    val start: Instant = range[0].toString().toInstantOrNull()
        ?: throw IllegalArgumentException("Invalid start timestamp: ${range[0]}")

    @JsonIgnore
    val end: Instant = range[1].toString().toInstantOrNull()
        ?: throw IllegalArgumentException("Invalid end timestamp: ${range[1]}")

    override val simpleValue get() = range

    override fun invoke(p: Any?): Boolean {
        val instant = cachedInstantFrom(p) ?: return false
        return !instant.isBefore(start) && !instant.isAfter(end)
    }
}
