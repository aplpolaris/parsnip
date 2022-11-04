package edu.jhuapl.data.parsnip.datum.filter

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * DatumFieldFilterTest.kt
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

import edu.jhuapl.data.parsnip.datum.DatumFilter
import edu.jhuapl.data.parsnip.io.ParsnipMapper
import edu.jhuapl.data.parsnip.value.filter.*
import edu.jhuapl.testkt.shouldBe
import edu.jhuapl.util.internal.printJsonTest
import edu.jhuapl.util.internal.recycleJsonTest
import junit.framework.TestCase
import java.io.IOException

class DatumFieldFilterTest : TestCase() {

    //region TESTING PROPER BEHAVIOR

    fun setupLogicalParams(): Pair<DatumFieldFilter, DatumFieldFilter> {
        // Params used for And, Or, Not tests
        val param1 = DatumFieldFilter().put("x", Gt(10))
        param1.put("y", Contains("Test"))                     // x > 10 && y contains Test
        val param2 = DatumFieldFilter().put("x", Gt(15))
        param2.put("y", StartsWith("Mock"))                   // x > 15 && y starts with Mock

        return Pair(param1, param2)
    }

    fun testNullCheck() {
        val mf = DatumFieldFilter().put("a", Equal(null))
        mf(mapOf("a" to 1)) shouldBe false
        mf(mapOf("a" to null)) shouldBe true
        mf(mapOf()) shouldBe true
    }

    fun testBasicFunctionality() {
        val mf = DatumFieldFilter().put("a", Range(1, 3))
        assertEquals(true, mf(mapOf("a" to 1, "b" to "two")))

        mf.put("b", Contains("t"))
        assertEquals(true, mf(mapOf("a" to 1, "b" to "two")))

        mf.put("c", Gt("four"))
        assertEquals(false, mf(mapOf("a" to 1, "b" to "two")))
        assertEquals(true, mf(mapOf("a" to 1, "b" to "two", "c" to "seven")))

    }

    fun testLogicalAnd() {
        val paramPair = setupLogicalParams()
        // Logical AND Test
        val andConditional = And(paramPair.first, paramPair.second)
        // Result Conditional: (x > 10 && y contains Test) AND (x > 15 && y starts with Mock)
        // False Cases
        assertEquals(false, andConditional.invoke(mapOf("x" to 20)))                        // y not given
        assertEquals(false, andConditional.invoke(mapOf("y" to "Mock_Test")))               // x not given
        assertEquals(false, andConditional.invoke(mapOf("x" to 12, "y" to "Mock_Test")))    // x invalid
        assertEquals(false, andConditional.invoke(mapOf("x" to 5, "y" to "Mock_Test")))     // x invalid
        assertEquals(false, andConditional.invoke(mapOf("x" to 20, "y" to "Mock_Te5t")))    // y invalid
        assertEquals(false, andConditional.invoke(mapOf("x" to 20, "y" to "M0ck_Test")))    // y invalid
        assertEquals(false, andConditional.invoke(mapOf("x" to 5, "y" to "M0ck_Test")))     // x,y invalid
        // True Case
        assertEquals(true, andConditional.invoke(mapOf("x" to 20, "y" to "Mock_Test")))
        // infix works
        assertEquals(true, (paramPair.first and paramPair.second).invoke(mapOf("x" to 20, "y" to "Mock_Test")))
    }

    fun testLogicalOr() {
        val paramPair = setupLogicalParams()
        // Logical OR Test
        val orConditional = Or(paramPair.first, paramPair.second)
        // Result Conditional: (x > 10 && y contains Test) OR (x > 15 && y starts with Mock)
        // False Cases
        assertEquals(false, orConditional.invoke(mapOf("y" to "Mock")))                     // y not given
        assertEquals(false, orConditional.invoke(mapOf("x" to 20)))                         // x not given
        assertEquals(false, orConditional.invoke(mapOf("x" to 6, "y" to "Mock_Test")))      // x invalid
        assertEquals(false, orConditional.invoke(mapOf("x" to 20, "y" to "Incorrect")))     // y invalid
        assertEquals(false, orConditional.invoke(mapOf("x" to 6, "y" to "Incorrect")))      // x,y invalid
        // True Cases
        assertEquals(true, orConditional.invoke(mapOf("x" to 12, "y" to "Ex_Test")))        // first cond satisfied
        assertEquals(true, orConditional.invoke(mapOf("x" to 20, "y" to "Mock")))           // second cond satisfied
        assertEquals(true, orConditional.invoke(mapOf("x" to 20, "y" to "Mock_Test")))      // Both cond satisfied
        // infix works
        assertEquals(true, (paramPair.first or paramPair.second).invoke(mapOf("x" to 20, "y" to "Mock_Test")))
    }

    fun testNestedLogicalComb() {
        val param1 = DatumFieldFilter().put("x", Gt(10))
        val param2 = DatumFieldFilter().put("x", Lt(15))
        val param3 = DatumFieldFilter().put("x", Gt(12))
        val param4 = DatumFieldFilter().put("x", Lt(14))
        // Logical AND Test
        val andConditionalParam1 = And(param1,param2)
        // Result Conditional: (x > 10 && x < 15)
        val andConditionalParam2 = And(param3,param4)
        // Result Conditional: (x > 12 && x < 14)
        val finalOr = Or(andConditionalParam1, andConditionalParam2)
        // Final Conditional: (x > 10 && x < 15) || (x > 12 && x < 14)

        assertEquals(true, finalOr.invoke(mapOf("x" to 11)))
        assertEquals(false, finalOr.invoke(mapOf("x" to 0)))
    }

    fun testLogicalNotSimple() {
        // Logical Not Test Simple Case
        val notParam1 = DatumFieldFilter().put("x", Gt(10))                          // x > 10
        val notConditionalSimple = Not(notParam1)
        // Result Conditional: !(x > 10) which is same as (x <= 10)
        // False Cases
        assertEquals(false, notConditionalSimple.invoke(mapOf("x" to 20)))
        // True Cases
        assertEquals(true, notConditionalSimple.invoke(mapOf("x" to 1)))
        assertEquals(true, notConditionalSimple.invoke(mapOf("x" to 10)))
        // infix works
        assertEquals(true, ( notParam1.not() ).invoke(mapOf("x" to 1)))
        assertEquals(true, ( !notParam1 ).invoke(mapOf("x" to 1)))
    }

    fun testLogicalNotComplex() {
        val paramPair = setupLogicalParams()
        val andConditional = And(paramPair.first, paramPair.second)
        // Logical Not Test Complex Case
        // Another Conditional, Let's do a logical not on the first And Conditional we did at the top
        val notConditionalComplex = Not(andConditional)
        // Result Conditional: !(x > 10 && y contains Test AND x > 15 && y starts with Mock)
        // Now it's flipped
        // True Case of original And conditional is now False Case
        assertEquals(false, notConditionalComplex.invoke(mapOf("x" to 20, "y" to "Mock_Test")))
        // False Case of original And conditional is now True Case
        assertEquals(true, notConditionalComplex.invoke(mapOf("x" to 20)))                      // y not given
        assertEquals(true, notConditionalComplex.invoke(mapOf("y" to "Mock_Test")))             // x not given
        assertEquals(true, notConditionalComplex.invoke(mapOf("x" to 12, "y" to "Mock_Test")))  // x invalid
        assertEquals(true, notConditionalComplex.invoke(mapOf("x" to 20, "y" to "Mock_Te5t")))  // y invalid
        assertEquals(true, notConditionalComplex.invoke(mapOf("x" to 5, "y" to "M0ck_Test")))   // x,y invalid
    }

    fun testLogicalNotNested() {
        val paramPair = setupLogicalParams()
        val andConditional = And(paramPair.first, paramPair.second)
        // Logical Not Test Complex Case
        // Another Conditional, Let's do a logical not on the first And Conditional we did at the top
        val notConditionalNested = Not(Not(andConditional))
        // Result Conditional: !(!(x > 10 && y contains Test AND x > 15 && y starts with Mock))
        // Now it's the same as the And Conditional:
        // Equivalent Conditional: (x > 10 && y contains Test AND x > 15 && y starts with Mock)
        assertEquals(false, notConditionalNested.invoke(mapOf("x" to 20)))                        // y not given
        assertEquals(false, notConditionalNested.invoke(mapOf("y" to "Mock_Test")))               // x not given
        assertEquals(false, notConditionalNested.invoke(mapOf("x" to 12, "y" to "Mock_Test")))    // x invalid
        assertEquals(false, notConditionalNested.invoke(mapOf("x" to 5, "y" to "Mock_Test")))     // x invalid
        assertEquals(false, notConditionalNested.invoke(mapOf("x" to 20, "y" to "Mock_Te5t")))    // y invalid
        assertEquals(false, notConditionalNested.invoke(mapOf("x" to 20, "y" to "M0ck_Test")))    // y invalid
        assertEquals(false, notConditionalNested.invoke(mapOf("x" to 5, "y" to "M0ck_Test")))     // x,y invalid
        // True Case
        assertEquals(true, notConditionalNested.invoke(mapOf("x" to 20, "y" to "Mock_Test")))
        // infix works
        assertEquals(true, (paramPair.first and paramPair.second).invoke(mapOf("x" to 20, "y" to "Mock_Test")))
    }

    //endregion

    //region TESTING DESERIALIZATION MAINTAINS PROPER BEHAVIOR

    fun testDeserializationBasic() {
        val basicConditional = DatumFieldFilter().put("a", Range(1, 3))
        basicConditional.put("b", Contains("t"))
        basicConditional.put("c", Gt("four"))
        // Result Conditional: (1 <= a <= 3), (b contains t), (c > 4)

        // String:  {'a': {'Range': [1, 3]}, 'b': {'Contains': 't'}, 'c': {'Gt': 'four'}}
        val basicEtlString = ParsnipMapper.writeValueAsString(basicConditional)

        val basicEtl = ParsnipMapper.readValue(basicEtlString, DatumFieldFilter::class.java)
        assertEquals(true, basicEtl(mapOf("a" to 1, "b" to "two", "c" to "six")))
        assertEquals(false, basicEtl(mapOf("a" to 1, "c" to "six")))
    }

    fun testDeserializationLogicalAnd() {
        val paramPair = setupLogicalParams()
        val andConditional = And(paramPair.first, paramPair.second)
        // Result Conditional: (x > 10 && y contains Test) AND (x > 15 && y starts with Mock)

        // String: {'And': [{'x': {'Gt': 10}, 'y': {'Contains': 'Test'}}, {'x': {'Gt': 15}, 'y': {'StartsWith': 'Mock'}}] }
        val andEtlString = ParsnipMapper.writeValueAsString(andConditional)

        val andEtl = ParsnipMapper.readValue(andEtlString, DatumFilter::class.java)
        assertEquals(true, andEtl.invoke(mapOf("x" to 20, "y" to "Mock_Test")))
        assertEquals(false, andEtl.invoke(mapOf("x" to 20, "y" to "MOCK_INVALID")))
    }

    fun testDeserializationLogicalOr() {
        val paramPair = setupLogicalParams()
        val orConditional = Or(paramPair.first, paramPair.second)
        // Result Conditional: (x > 10 && y contains Test) OR (x > 15 && y starts with Mock)

        // String: {'Or': [{'x': {'Gt': 10}, 'y': {'Contains': 'Test'}}, {'x': {'Gt': 15}, 'y': {'StartsWith': 'Mock'}}] }
        val orEtlString = ParsnipMapper.writeValueAsString(orConditional)

        val orEtl = ParsnipMapper.readValue(orEtlString, DatumFilter::class.java)
        assertEquals(true, orEtl.invoke(mapOf("x" to 12, "y" to "Mock_Test")))
        assertEquals(false, orEtl.invoke(mapOf("x" to 1, "y" to "Mock_Test")))
    }

    fun testDeserializationNestedLogicalComb() {
        val param1 = DatumFieldFilter().put("x", Gt(10))
        val param2 = DatumFieldFilter().put("x", Lt(15))
        val param3 = DatumFieldFilter().put("x", Gt(12))
        val param4 = DatumFieldFilter().put("x", Lt(14))
        // Logical AND Test
        val andConditionalParam1 = And(param1,param2)
        // Result Conditional: (x > 10 && x < 15)
        val andConditionalParam2 = And(param3,param4)
        // Result Conditional: (x > 12 && x < 14)
        val finalOr = Or(andConditionalParam1, andConditionalParam2)
        // Final Conditional: (x > 10 && x < 15) || (x > 12 && x < 14)

        // String:  {'Or': [{'And': [{'x': {'Gt': 10}}, {'x': {'Lt': 15}}]}, {'And': [{'x': {'Gt': 12}}, {'x': {'Lt': 14}}]}]}
        val basicEtlString = ParsnipMapper.writeValueAsString(finalOr)

        val basicEtl = ParsnipMapper.readValue(basicEtlString, DatumFilter::class.java)
        assertEquals(true, basicEtl.invoke(mapOf("x" to 11)))
        assertEquals(false, basicEtl.invoke(mapOf("x" to 1)))
    }

    fun testDeserializationLogicalNotSimpleCase() {
        val notParam1 = DatumFieldFilter().put("x", Gt(10))
        val notConditionalEx1 = Not(notParam1)
        // Result Conditional: Not(x > 10)

        // String: {"Not": {"x": {"Gt": 10}} }
        val notEtlString = ParsnipMapper.writeValueAsString(notConditionalEx1)

        val notEtl = ParsnipMapper.readValue(notEtlString, DatumFilter::class.java)
        assertEquals(true, notEtl.invoke(mapOf("x" to 1)))
        assertEquals(false, notEtl.invoke(mapOf("x" to 13)))
    }

    fun testDeserializationLogicalNotComplexCase() {
        val paramPair = setupLogicalParams()
        val andConditional = And(paramPair.first, paramPair.second)
        val notConditionalComplexCase = Not(andConditional)
        // Result Conditional: !(x > 10 && y contains Test AND x > 15 && y starts with Mock)

        // {'Not': {'And': [{'x': {'Gt': 10}, 'y': {'Contains': 'Test'}}, {'x': {'Gt': 15}, 'y': {'StartsWith': 'Mock'}}] } }
        val notEtlString = ParsnipMapper.writeValueAsString(notConditionalComplexCase)

        val notEtl = ParsnipMapper.readValue(notEtlString, DatumFilter::class.java)
        assertEquals(true, notEtl.invoke(mapOf("x" to 12, "y" to "Mock_Test")))  // x invalid
        assertEquals(false, notEtl.invoke(mapOf("x" to 20, "y" to "Mock_Test")))
   }

    fun testDeserializationLogicalNotNestedCase() {
        val paramPair = setupLogicalParams()
        val andConditional = And(paramPair.first, paramPair.second)
        val notNotConditionalComplexCase = Not(Not(andConditional))
        // Result Conditional: !(!(x > 10 && y contains Test AND x > 15 && y starts with Mock))

        // {'Not': {'Not': {'And': [{'x': {'Gt': 10}, 'y': {'Contains': 'Test'}}, {'x': {'Gt': 15}, 'y': {'StartsWith': 'Mock'}}] } } }
        // Equivalent to
        // {'And': [{'x': {'Gt': 10}, 'y': {'Contains': 'Test'}}, {'x': {'Gt': 15}, 'y': {'StartsWith': 'Mock'}}] }
        val notNotEtlString = ParsnipMapper.writeValueAsString(notNotConditionalComplexCase)

        val notNotEtl = ParsnipMapper.readValue(notNotEtlString, DatumFilter::class.java)
        assertEquals(false, notNotEtl.invoke(mapOf("x" to 12, "y" to "Mock_Test")))
        assertEquals(true, notNotEtl.invoke(mapOf("x" to 20, "y" to "Mock_Test")))
    }

    fun testDeserializationNestedComplexLogicalCombWithNot() {
        val param1 = DatumFieldFilter().put("x", Gt(10))
        val param2 = DatumFieldFilter().put("x", Lt(15))
        val param3 = DatumFieldFilter().put("x", Gt(12))
        val param4 = DatumFieldFilter().put("x", Lt(14))
        // Logical AND Test
        val notAndConditionalParam1 = Not(And(param1,param2))
        // Result Conditional: !(x > 10 && x < 15)
        val notAndConditionalParam2 = Not(And(param3,param4))
        // Result Conditional: !(x > 12 && x < 14)
        val orConditional = Or(notAndConditionalParam1, notAndConditionalParam2)
        val finalCond = Not(orConditional)
        // Final Conditional: !(!(x > 10 && x < 15) || !(x > 12 && x < 14))
        // Simplified Conditional: (x > 10 && x < 15) && (x > 12 && x < 14)
        // only value satisfying the above is 13

        // String:  {"Not":{"Or":[{"Not":{"And":[{"x":{"Gt":10}},{"x":{"Lt":15}}]}},{"Not":{"And":[{"x":{"Gt":12}},{"x":{"Lt":14}}]}}]}}
        val basicEtlString = ParsnipMapper.writeValueAsString(finalCond)

        val basicEtl = ParsnipMapper.readValue(basicEtlString, DatumFilter::class.java)
        assertEquals(true, basicEtl.invoke(mapOf("x" to 13)))
        assertEquals(false, basicEtl.invoke(mapOf("x" to 10)))
    }
    //endregion


    @Throws(IOException::class)
    fun testSerialize() {
        DatumFieldFilter().printJsonTest()
        DatumFieldFilter().put("a", Range(1, 3)).printJsonTest()

        DatumFieldFilter().recycleJsonTest<DatumFilter>()
        DatumFieldFilter().put("a", Range(1, 3)).recycleJsonTest<DatumFilter>()

        val param1 = DatumFieldFilter().put("x", Gt(1))
        param1.put("y", Contains("A"))                     // x > 1 && y contains A
        val param2 = DatumFieldFilter().put("x", Gt(5))
        param2.put("y", StartsWith("Z"))                   // x > 5 && y startsWith Z

        val andConditional = And(param1, param2)
        andConditional.recycleJsonTest<DatumFilter>()
        val orConditional = Or(param1, param2)
        orConditional.recycleJsonTest<DatumFilter>()
        val notConditionalEx2 = Not(andConditional)
        notConditionalEx2.recycleJsonTest<DatumFilter>()
    }

}

