/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * ComparingValueFilter.kt
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
package edu.jhuapl.data.parsnip.value.filter

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import edu.jhuapl.data.parsnip.value.ValueFilter
import edu.jhuapl.util.types.ObjectOrdering
import edu.jhuapl.util.types.SimpleValue
import edu.jhuapl.utilkt.core.fine

/**
 * A filter implementation based on a single arbitrary value, that tests inputs by comparing to the value. Also provides a
 * default implementation that catches [IllegalArgumentException].
 */
abstract class ComparingValueFilter(var value: Any?, private val tester: (Int) -> Boolean) : ValueFilter, SimpleValue {

    override val simpleValue
        get() = value

    override fun invoke(o: Any?): Boolean {
        return try {
            tester(ObjectOrdering.compare(o, value))
        } catch (x: IllegalArgumentException) {
            fine<ComparingValueFilter>("Objects were not comparable", x)
            false
        }
    }

}

/** Tests for null values. */
object IsNull : ValueFilter {
    override fun invoke(p: Any?) = p == null
}

/** Tests for non-null values. */
object IsNotNull: ValueFilter {
    override fun invoke(p: Any?) = p != null
}

/** Tests for empty values (nulls or empty strings). */
object IsEmpty: ValueFilter {
    override fun invoke(p: Any?) = p == null || p == ""
}

/** Tests for non-empty values (nulls or empty strings). */
object IsNotEmpty: ValueFilter {
    override fun invoke(p: Any?) = p != null && p != ""
}

/** Equal filter. Allows object equals testing as well as string equality testing. Also supports null checking. */
class Equal @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(value: Any?) : ComparingValueFilter(value, { it == 0 }) {
    override fun invoke(o: Any?) = when {
        value == null || o == null -> value == o
        else -> super.invoke(o)
    }
}

/** Not-equal filter. Also supports null checking. */
class NotEqual @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(value: Any?) : ComparingValueFilter(value, { it != 0 }) {
    override fun invoke(o: Any?) = when {
        value == null || o == null -> value != o
        else -> super.invoke(o)
    }
}

/** Greater-than filter. */
class Gt @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(value: Any?) : ComparingValueFilter(value, { it > 0 })

/** Greater-than or equal filter. */
class Gte @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(value: Any?) : ComparingValueFilter(value, { it >= 0 })

/** Less-than filter. */
class Lt @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(value: Any?) : ComparingValueFilter(value, { it < 0 })

/** Less-than or equal filter. */
class Lte @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(value: Any?) : ComparingValueFilter(value, { it <= 0 })

/** In-range filter, for values between a minimum and a maximum (inclusive). */
class Range @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(vararg range: Any) : ValueFilter, SimpleValue {
    var range = listOf(*range)

    @JsonIgnore
    val min = range[0]
    @JsonIgnore
    val max = range[1]

    override val simpleValue
        get() = range

    override fun invoke(o: Any?) = try {
        ObjectOrdering.compare(o, min) >= 0 && ObjectOrdering.compare(o, max) <= 0
    } catch (x: IllegalArgumentException) {
        fine<ComparingValueFilter>("Objects were not comparable", x)
        false
    }
}
