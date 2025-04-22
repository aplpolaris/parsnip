/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * Calculate.kt
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
package edu.jhuapl.data.parsnip.datum.compute

import com.fasterxml.jackson.annotation.JsonCreator
import com.googlecode.blaisemath.parser.*
import edu.jhuapl.data.parsnip.datum.Datum
import edu.jhuapl.data.parsnip.datum.DatumCompute
import edu.jhuapl.util.types.SimpleValue
import edu.jhuapl.util.types.atPointer
import edu.jhuapl.util.types.toNumberOrNull
import edu.jhuapl.utilkt.core.fine

/**
 * Performs a numeric calculation using inputs from the datum.
 * Requires all content between successive braces { and } to be a JSON pointer to an element in the given object.
 * Supports numeric operations only.
 */
class Calculate @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(_template: String) : DatumCompute<Number>, SimpleValue {

    private var expressionTree: NumericExpressionTree? = null

    var template: String = ""
        set(value) {
            field = value
            expressionTree = try {
                NumericExpressionTree(template)
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

    override fun invoke(map: Map<String, *>): Number? = expressionTree?.invoke(map)
}

/** Numeric calculate tree. */
class NumericExpressionTree(expr: String) : ExpressionTree<Number?>(expr) {
    private val tree: SemanticNode = RealGrammar.getParser().parseTree(simpleExpression)

    override fun invoke(p1: Datum): Number? {
        val simpleVars = variableLookupTable.map { it.key to p1.atPointer(it.value).toNumberOrNull()?.toDouble() }.toMap()
        tree.assignVariables(simpleVars)
        return try {
            tree.value as? Number
        } catch (ex: SemanticTreeEvaluationException) {
            null
        }
    }
}

/** Stores an expression tree for numeric computations. */
abstract class ExpressionTree<V>(templateExpression: String): (Datum) -> V {

    /** Expression using simple variables. */
    protected var simpleExpression: String?

    /** Maps simple expression variables to pointer expression variables. */
    protected var variableLookupTable = mutableMapOf<String, String>()

    init {
        // compile simple expression and table mapping simple variables to json pointer variables
        val res = StringBuilder()
        var i0 = 0
        var varCount = 0
        var i1 = templateExpression.indexOf('{', i0)
        while (i1 != -1) {
            res.append(templateExpression.substring(i0, i1))
            val i2 = templateExpression.indexOf('}', i1)
            if (i2 == -1) {
                fine<Template>("Invalid JSON pointer template: '{' without '}': {0}", templateExpression)
                break
            }
            val pointerField = templateExpression.substring(i1 + 1, i2)
            val simpleField = "a$varCount"
            varCount++
            variableLookupTable[simpleField] = pointerField
            res.append(simpleField)
            i0 = i2 + 1
            i1 = templateExpression.indexOf('{', i0)
        }
        res.append(templateExpression.substring(i0))
        simpleExpression = res.toString()
    }
}
