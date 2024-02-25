package edu.jhuapl.data.parsnip.set

import edu.jhuapl.data.parsnip.set.compute.IntStats
import edu.jhuapl.data.parsnip.set.compute.Stats

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * ValueSetFunctions.kt
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


/** A collection of value objects, which has size and can be iterated over. */
typealias ValueSet = Collection<Any?>
/** A sequence of value objects, for use when size is not known or unbounded. */
typealias ValueSequence = Sequence<Any?>

/** Computes a scalar output value for an ordered set of inputs. */
interface ValueSetCompute<out Y>: (ValueSet) -> Y?
/** Computes a boolean value for an ordered set of inputs. */
interface ValueSetFilter: (ValueSequence) -> Boolean
/** Transforms one set of values to another. */
interface ValueSetTransform : ValueSetCompute<ValueSequence>

//region FACTORY METHODS

/** Computes stats for given field. */
fun ValueSet.stats() = Stats.invoke(this)
/** Computes stats for given field. */
fun ValueSet.intStats() = IntStats.invoke(this)

//endregion
