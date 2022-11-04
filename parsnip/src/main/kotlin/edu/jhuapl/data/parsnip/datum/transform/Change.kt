package edu.jhuapl.data.parsnip.datum.transform

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * Change.kt
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
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import edu.jhuapl.data.parsnip.datum.Datum
import edu.jhuapl.data.parsnip.datum.DatumTransform
import edu.jhuapl.data.parsnip.value.filter.Equal
import edu.jhuapl.data.parsnip.value.filter.NotEqual
import edu.jhuapl.util.types.atPointer
import edu.jhuapl.util.types.nestedPutAll
import java.util.*

/**
 * Monitors field value transitions; assumes inputs are ordered. Returns a copy of the original map with optional additional
 * fields added when changes are detected, and a null otherwise. To do this, the class maintains state for each "group by"
 * field to monitor last/current values. When monitoring from changes, a "null" value for "from" or "to" will match all
 * changes to/from the other value, while a "nulL" value for both fields will match all changes.
 */
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
class Change(var groupBy: String?, var monitor: String, vararg whenChange: Transition) : DatumTransform {

    @JsonCreator
    constructor(@JsonProperty("groupBy") groupBy: String?, @JsonProperty("monitor") monitor: String,
                @JsonProperty("whenChange") whenChangeList: List<Transition>? = listOf()) : this(groupBy, monitor, *whenChangeList.toArray())

    var whenChange: List<Transition> = listOf(*whenChange)

    /** Used to monitor any change, when changes are empty */
    private val anyChange = listOf(Transition(null, null, mapOf<String, Any>()))

    /** State object tracking current field value for each group.  */
    private val lastStates: MutableMap<String, Any?> = mutableMapOf()


    override operator fun invoke(map: Datum): Datum? {
        val g = if (groupBy == null) null else map.atPointer(groupBy, Any::class.java)
        val group = Objects.toString(g)
        val lastValue = lastStates[group]
        val value = map.atPointer(monitor, Any::class.java)
        lastStates[group] = value

        val res = map.toMutableMap()
        var triggered = false
        val changeTests = if (whenChange.isEmpty()) anyChange else whenChange
        changeTests.filter { it.matchesChange(lastValue, value) }
                .forEach {
                    res.nestedPutAll(it.put)
                    triggered = true
                }
        return if (triggered) res else null
    }
}

/** Describes a transition from one value to another, along with resulting content to add to map. Null values of from/to will monitor for any differences.  */
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
class Transition @JsonCreator constructor(@JsonProperty("from") var from: Any?,
                                          @JsonProperty("to") var to: Any?,
                                          @JsonProperty("put") var put: Map<String, *>) {

    fun matchesChange(lastValue: Any?, curValue: Any?) = when {
        from == null && to == null -> NotEqual(lastValue).invoke(curValue)
        from == null -> NotEqual(to).invoke(lastValue) && Equal(to).invoke(curValue)
        to == null -> Equal(from).invoke(lastValue) && NotEqual(from).invoke(curValue)
        else -> Equal(from).invoke(lastValue) && Equal(to).invoke(curValue)
    }
}

private fun List<Transition>?.toArray() = when(this) {
    null -> arrayOf()
    else -> toTypedArray()
}
