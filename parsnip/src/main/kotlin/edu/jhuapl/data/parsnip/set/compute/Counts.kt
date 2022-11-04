/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * Counts.kt
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
package edu.jhuapl.data.parsnip.set.compute

import edu.jhuapl.data.parsnip.set.ValueSet
import edu.jhuapl.data.parsnip.set.ValueSetCompute
import edu.jhuapl.util.types.toNumberOrNull

/** Counts number of values. */
object Count : ValueSetCompute<Int> {
    override fun invoke(values: ValueSet) = values.size
}

/** Counts number of valid values: non-null and non-NaN. */
object CountValid : ValueSetCompute<Int> {
    override fun invoke(values: ValueSet) = values.count { it != null && it.isFiniteIfNumber() }
}

/** Counts number of missing (null) values. */
object CountMissing : ValueSetCompute<Int> {
    override fun invoke(values: ValueSet) = values.count { it == null }
}

/** Counts number of non-null values. */
object CountNonNull : ValueSetCompute<Int> {
    override fun invoke(values: ValueSet) = values.count { it != null }
}

/** Counts number of distinct values. */
object CountDistinct : ValueSetCompute<Int> {
    override fun invoke(values: ValueSet) = values.distinct().count()
}

/** Counts number of numeric values. */
object CountNumeric : ValueSetCompute<Int> {
    override fun invoke(values: ValueSet) = values.count { it.toNumberOrNull() != null }
}

/** If value is a number, checks if its finite. Otherwise returns true. */
private fun Any.isFiniteIfNumber(): Boolean = when(this) {
    is Double -> isFinite()
    is Float -> isFinite()
    else -> true
}
