/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * LogicalFilters.kt
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
import edu.jhuapl.data.parsnip.value.ValueFilter
import edu.jhuapl.util.types.SimpleValue

/** Filter that accepts all values. */
class All : ValueFilter {
    override fun invoke(p1: Any?) = true
}

/** Filter that accepts no values. */
class None : ValueFilter {
    override fun invoke(p1: Any?) = false
}

/** Logical and filter.  */
class And @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(vararg filters: ValueFilter) : ValueFilter, SimpleValue {

    /** Base set of filters, automatically expands any And operators seen */
    var base: MutableList<out ValueFilter> = filters.conciseAndList()

    override val simpleValue
        get() = base

    override fun invoke(o: Any?): Boolean = base.all { it(o) }

    /** Combines and lists by extracting their content (to first order) and including it in the top list. */
    private fun Array<out ValueFilter>.conciseAndList() = flatMap { if (it is And) it.base else listOf(it) }.toMutableList()

}

/** Logical or filter.  */
class Or @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(vararg filters: ValueFilter) : ValueFilter, SimpleValue {

    /** Base set of filters, automatically expands any Or operators seen */
    var base: MutableList<out ValueFilter> = filters.conciseOrList()

    override val simpleValue
        get() = base

    override fun invoke(o: Any?): Boolean = base.any { it(o) }

    /** Combines and lists by extracting their content (to first order) and including it in the top list. */
    private fun Array<out ValueFilter>.conciseOrList() = flatMap { if (it is Or) it.base else listOf(it) }.toMutableList()

}

/** Logical not filter.  */
class Not @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(var base: ValueFilter) : ValueFilter, SimpleValue {

    override val simpleValue
        get() = base

    override fun invoke(o: Any?): Boolean = !base(o)

}

//region OPERATOR/INFIX DEFINITIONS

/**
 * Operator that can be used to combine two filters using [And].
 * @param v filter to combine with
 */
infix fun ValueFilter.and(v: ValueFilter) = And(this, v)

/**
 * Operator that can be used to combine two filters using [Or].
 * @param v filter to combine with
 */
infix fun ValueFilter.or(v: ValueFilter) = Or(this, v)

/** Overload -filter to indicate !filter. Prevents double negatives. */
operator fun ValueFilter.unaryMinus() = not()

/** Negates a filter. Unwraps doubles to prevent double negation. Prevents double negatives. */
operator fun ValueFilter.not() = if (this is Not) base else Not(this)

//endregion
