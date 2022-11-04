/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * CalculateBoolean.kt
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
package edu.jhuapl.data.parsnip.datum.compute

import com.fasterxml.jackson.annotation.JsonCreator
import com.googlecode.blaisemath.parser.*
import edu.jhuapl.data.parsnip.datum.Datum
import edu.jhuapl.data.parsnip.datum.DatumCompute
import edu.jhuapl.util.types.SimpleValue
import edu.jhuapl.util.types.atPointer
import edu.jhuapl.util.types.convertTo
import edu.jhuapl.util.types.toNumberOrNull
import edu.jhuapl.utilkt.core.fine

/**
 * Performs a boolean calculation using inputs from the datum.
 * Requires all content between successive braces { and } to be a JSON pointer to an element in the given object.
 * Supports mathematical and boolean operations.
 */
class CalculateBoolean @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(_template: String) : DatumCompute<Boolean>, SimpleValue {

    private var expressionTree: BooleanExpressionTree? = null

    var template: String = ""
        set(value) {
            field = value
            expressionTree = try {
                BooleanExpressionTree(template)
            } catch (ex: ParseException) {
                fine<Calculate>("Invalid expression: $template")
                null
            }
        }


    init {
        // set here so expression tree updates
        template = _template
    }

    var forNull = "null"

    override val simpleValue: Any
        get() = if (forNull == "null") template else this

    override fun invoke(map: Map<String, *>): Boolean? = expressionTree?.invoke(map)
}

/** Boolean calculate tree. */
class BooleanExpressionTree(expr: String) : ExpressionTree<Boolean?>(expr) {
    private val tree: SemanticNode = BooleanGrammar.getParser().parseTree(simpleExpression)

    override fun invoke(p1: Datum): Boolean? {
        val simpleVars = variableLookupTable.map { it.key to p1.atPointer(it.value)?.let { it.convertTo(Boolean::class.java) } }.toMap()
        tree.assignVariables(simpleVars)
        return try {
            tree.value as? Boolean
        } catch (ex: SemanticTreeEvaluationException) {
            null
        }
    }
}
