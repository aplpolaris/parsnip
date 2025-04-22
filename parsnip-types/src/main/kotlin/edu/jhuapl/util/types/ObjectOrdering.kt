package edu.jhuapl.util.types

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * ObjectOrdering.kt
 * edu.jhuapl.data:parsnip
 * %%
 * Copyright (C) 2024 - 2025 Johns Hopkins University Applied Physics Laboratory
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

import java.util.*
import kotlin.Comparator

/**
 * An ordering that allows comparison of multiple object types. Nulls are allowed but placed last. May throw an
 * [IllegalArgumentException] if values are not comparable.
 */
object ObjectOrdering : Comparator<Any> {

    override fun compare(left: Any?, right: Any?): Int {
        require(left != null && right != null) { "Nulls not allowed" }
        val c1 = left::class.java
        val c2 = right::class.java
        return when {
            left == right -> 0
            c1 == c2 && left is Comparable<*> -> compareSameComparable(left, right)
            left is Number -> compareNumbers(left, right)
            right is Number -> -compareNumbers(right, left)
            c1.timeType() -> compareTimeTypes(left, right)
            c2.timeType() -> -compareTimeTypes(right, left)
            left is Boolean -> compareBooleans(left, right)
            right is Boolean -> -compareBooleans(right, left)
            else -> c1.toString().compareTo(c2.toString())
        }
    }

    /** Compares comparable values of the same type. Expects both inputs to have the same comparable type. */
    private fun <C : Comparable<*>> compareSameComparable(left: C, right: Any): Int = (left as Comparable<C>).compareTo(right as C)

    /** Compare values when left is known to be a number. */
    @Throws(IllegalArgumentException::class)
    private fun compareNumbers(left: Number, right: Any): Int {
        if (right is Number) {
            return left.toDouble().compareTo(right.toDouble())
        }
        val num = Objects.toString(right).toNumber(left::class.java)
        return left.toDouble().compareTo(num.toDouble())
    }

    private fun compareBooleans(left: Boolean, right: Any) = when {
        right is Boolean -> left.compareTo(right)
        "true" == right.toString().toLowerCase() -> left.compareTo(true)
        "false" == right.toString().toLowerCase() -> left.compareTo(false)
        else -> left.toString().compareTo(right.toString())
    }

    private fun compareTimeTypes(left: Any, right: Any): Int {
        val t0 = left.toEpochMilli()!!
        val t1 = right.toEpochMilli()
        require(t1 != null) { "Not a timestamp: $right" }
        return t0.compareTo(t1)
    }
}
