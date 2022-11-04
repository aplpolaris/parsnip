/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * KotlinTestUtils.kt
 * edu.jhuapl.util:ekotlin-test
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
package edu.jhuapl.testkt

import java.util.*
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.asserter

/** Inline text for equality testing */
infix fun Any?.shouldBe(x: Any?) = assertEquals(x, this)

/** Inline text for content testing */
infix fun <T> Array<T>?.contentShouldBe(x: Array<*>?) {
    val check = when(this) {
        null -> x == null
        else -> x != null && contentEquals(x)
    }
    if (!check) {
        asserter.fail("Expected ${Arrays.toString(x)} but was ${Arrays.toString(this)}")
    }
}
/** Inline text for content testing */
infix fun IntArray?.contentShouldBe(x: IntArray?) {
    val check = when(this) {
        null -> x == null
        else -> x != null && contentEquals(x)
    }
    if (!check) {
        asserter.fail("Expected ${Arrays.toString(x)} but was ${Arrays.toString(this)}")
    }
}

/** Inline text for throwable testing */
infix fun <T : Throwable> (() -> Any?).shouldThrow(exceptionClass: KClass<T>) = assertFailsWith(exceptionClass) { this() }
