package edu.jhuapl.data.parsnip.datum.compute

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * MathOp.kt
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

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import edu.jhuapl.data.parsnip.datum.DatumCompute
import edu.jhuapl.util.types.atPointer
import edu.jhuapl.util.types.timeType
import edu.jhuapl.util.types.toCachedDateTime
import edu.jhuapl.util.types.toNumberOrNull

/** Performs a mathematical operation on numeric or date/time input fields, e.g. add/subtract/divide.  */
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
class MathOp : DatumCompute<Any> {
    /** Fields to combine  */
    var fields: List<String>
    /** The operator to apply.  */
    var operator: Operate = Operate.ADD
    /** Result to return if inputs are invalid.  */
    var ifInvalid: Any? = null

    constructor(fields: List<String>): this(Operate.ADD, fields)

    @JsonCreator
    constructor(@JsonProperty("operator") op: Operate = Operate.ADD,
                @JsonProperty("fields") fields: List<String> = emptyList(),
                @JsonProperty("ifInvalid") ifInvalid: Any? = null) {
        this.operator = op
        this.fields = fields
        this.ifInvalid = ifInvalid
    }

    override fun invoke(map: Map<String, *>): Any? {
        val first = map.valueAtFirstPointerIn(fields) ?: return ifInvalid
        val inputs = fields.map { map.atPointer(it) }.toNullableNumberList()
        val res = operator.invoke(inputs, ifInvalid)

        // if the first argument is a date/time type, e.g. Date, and the operator is compatible, return value as that type
        val dateTimeOp = operator.returnsDates && first::class.java.timeType() && first !is Number
        return when {
            dateTimeOp -> ((res as? Number)?.toLong() ?: res)?.toCachedDateTime(first::class.java)
            else -> res
        }
    }
}

//region UTILITY FUNCTIONS

/**
 * Lookup value of the first field within the map, using pointer notation. Returns null if fields is empty or lookup
 * result is null or cannot be found.
 */
private fun Map<String, *>.valueAtFirstPointerIn(fields: List<String>) =
        if (fields.isEmpty()) null else atPointer(fields[0])

/** Converts elements of list to numbers wherever possible, leaving null values if not. */
internal fun <E> List<E>.toNullableNumberList() = map { it?.toNumberOrNull() }

//endregion
