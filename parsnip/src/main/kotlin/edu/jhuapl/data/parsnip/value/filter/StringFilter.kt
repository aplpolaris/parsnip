package edu.jhuapl.data.parsnip.value.filter

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * StringFilter.kt
 * edu.jhuapl.data:parsnip
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

import com.fasterxml.jackson.annotation.JsonCreator
import edu.jhuapl.data.parsnip.value.ValueFilter
import edu.jhuapl.util.types.SimpleValue

/**
 * A filter implementation that tests inputs based on a single arbitrary value.
 */
abstract class StringFilter(var value: Any?) : ValueFilter, SimpleValue {

    override val simpleValue
        get() = value

    protected val valueString
        get() = value.toString()

}

/** Tests if an input contains the value string. */
class Contains @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(value: String) : StringFilter(value) {
    override fun invoke(o: Any?) = o.toString().contains(valueString)
}

/** Tests if an input starts with the value string. */
class StartsWith @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(value: String) : StringFilter(value) {
    override fun invoke(o: Any?) = o.toString().startsWith(valueString)
}

/** Tests if an input ends with the value string. */
class EndsWith @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(value: String) : StringFilter(value) {
    override fun invoke(o: Any?) = o.toString().endsWith(valueString)
}

/** Tests if an input matches a regex. */
class Matches @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(r: String) : StringFilter(r) {
    val regex
        get() = valueString.toRegex()

    override fun invoke(o: Any?) = regex.matches(o.toString())
}

/** Tests if an input contains a match to the given regex. */
class ContainsMatch @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(r: String) : StringFilter(r) {
    val regex
        get() = valueString.toRegex()

    override fun invoke(o: Any?) = regex.containsMatchIn(o.toString())
}
