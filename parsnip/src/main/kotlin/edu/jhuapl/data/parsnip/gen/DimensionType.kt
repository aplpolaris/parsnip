/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * DimensionType.kt
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
package edu.jhuapl.data.parsnip.gen

/** Object type for the dimension. */
sealed class DimensionType<X>(internal val name: String) {
    override fun toString() = name
}

/** Get dimension from string. */
fun dimensionType(type: String) = when(type.toLowerCase()) {
    "BOOLEAN" -> BooleanDimensionType
    "STRING" -> StringDimensionType
    "INTEGER" -> IntegerDimensionType
    "FLOAT" -> FloatDimensionType
    "SET" -> SetDimensionType
    else -> throw IllegalArgumentException("$type is not a DimensionType")
}

object BooleanDimensionType : DimensionType<Boolean>("BOOLEAN")
object StringDimensionType : DimensionType<String>("STRING")
object IntegerDimensionType : DimensionType<Int>("INTEGER")
object FloatDimensionType : DimensionType<Double>("FLOAT")
object SetDimensionType : DimensionType<Set<Any>>("SET")
