package edu.jhuapl.util.classifier

import java.lang.IllegalArgumentException

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * Classifier.kt
 * edu.jhuapl.util:ekotlin-utils
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

/**
 * Returns a set of likelihood scores for each input, drawn from a set of categories.
 * @param <O> type of object being classified
 * @param <T> space of categories for the classification
 */
abstract class Classifier<O, T>(protected val values: Array<T>) : (O) -> Map<T, Float> {

    /**
     * Returns the "score" or probability of an object having specified category.
     * @param value what to score
     * @param category category to score against
     * @return likelihood score
     */
    abstract fun score(value: O, category: T): Float

    /**
     * Returns a map of probabilities of various types.
     * @param x what to score
     * @return probability map
     */
    override fun invoke(x: O): Map<T, Float> = values.associateBy({ it }, { score(x, it) })

}

/**
 * Returns best guess category for the given object.
 * @param <O> object type to be classified
 * @param <T> type of class/category
 * @param obj object to be classified
 * @return best guess
 */
fun <O, T> Classifier<O, T>.bestGuess(obj: O): T = invoke(obj).entries.maxByOrNull { it.value }?.key
        ?: throw IllegalArgumentException("Classifier has no categories or invalid $this")
