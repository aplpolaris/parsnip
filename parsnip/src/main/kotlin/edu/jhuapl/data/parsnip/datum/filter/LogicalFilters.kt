/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * LogicalFilters.kt
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
package edu.jhuapl.data.parsnip.datum.filter

import com.fasterxml.jackson.annotation.JsonCreator
import edu.jhuapl.data.parsnip.datum.Datum
import edu.jhuapl.data.parsnip.datum.DatumFilter
import edu.jhuapl.data.parsnip.value.ValueFilter
import edu.jhuapl.util.types.SimpleValue

/** Filter that accepts all values. */
class All : DatumFilter {
    override fun invoke(o: Datum) = true
}

/** Filter that accepts no values. */
class None : DatumFilter {
    override fun invoke(o: Datum) = false
}

/** Logical And DatumFilter.  */
class And @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(vararg datumFilters: DatumFilter) : DatumFilter, SimpleValue {

    /** Base set of DatumFilters, automatically expands any And operators seen */
    var base: MutableList<out DatumFilter> = datumFilters.conciseAndList()

    override val simpleValue
        get() = base

    override fun invoke(o: Datum): Boolean = base.all { it(o) }

    /** Combines and lists by extracting their content (to first order) and including it in the top list. */
    private fun Array<out DatumFilter>.conciseAndList() = flatMap { if (it is And) it.base else listOf(it) }.toMutableList()
}

/** Logical Or DatumFilter.  */
class Or @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(vararg datumFilters: DatumFilter) : DatumFilter, SimpleValue {

    /** Base set of DatumFilters, automatically expands any And operators seen */
    var base: MutableList<out DatumFilter> = datumFilters.conciseAndList()

    override val simpleValue
        get() = base

    // (set of field conditions which is an AND) or (set of.....)
    override fun invoke(o: Datum): Boolean = base.any { it(o) }

    /** Combines and lists by extracting their content (to first order) and including it in the top list. */
    private fun Array<out DatumFilter>.conciseAndList() = flatMap { if (it is Or) it.base else listOf(it) }.toMutableList()
}

/** Logical not filter.  */
class Not @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(var base: DatumFilter) : DatumFilter, SimpleValue {

    override val simpleValue
        get() = base

    override fun invoke(o: Datum): Boolean = !base(o)

}

//region OPERATOR/INFIX DEFINITIONS

/**
 * Operator that can be used to combine two DatumFilter using [And].
 * @param v filter to combine with
 */
infix fun DatumFilter.and(v: DatumFilter) = And(this, v)

/**
 * Operator that can be used to combine two DatumFilter using [Or].
 * @param v filter to combine with
 */
infix fun DatumFilter.or(v: DatumFilter) = Or(this, v)

/** Overload -filter to indicate !filter. Prevents double negatives. */
operator fun DatumFilter.unaryMinus() = not()

/** Negates a filter. Unwraps doubles to prevent double negation. Prevents double negatives. */
operator fun DatumFilter.not() = if (this is Not) base else Not(this)

//endregion
