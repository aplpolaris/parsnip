/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * TypeUtilsTest.kt
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
package edu.jhuapl.util.types

import edu.jhuapl.testkt.shouldBe
import edu.jhuapl.testkt.shouldThrow
import org.junit.Test
import java.awt.Color
import java.time.Instant
import java.util.*

class TypeUtilsTest {

    @Test
    fun testToShortName() {
        Int::class.java.toShortName() shouldBe "int"
        Long::class.java.toShortName() shouldBe "long"
        Date::class.java.toShortName() shouldBe "Date"
        Instant::class.java.toShortName() shouldBe "Instant"
        Color::class.java.toShortName() shouldBe "java.awt.Color"
    }

    @Test
    fun testFromShortName() {
        fromShortName("int") shouldBe Int::class.java
        { fromShortName("Int") } shouldThrow ClassNotFoundException::class
        fromShortName("Integer") shouldBe Integer::class.java
        fromShortName("long") shouldBe Long::class.java
        fromShortName("Date") shouldBe Date::class.java
        fromShortName("Instant") shouldBe Instant::class.java
        { fromShortName("Color") } shouldThrow ClassNotFoundException::class
        fromShortName("java.awt.Color") shouldBe Color::class.java
    }

    @Test
    fun serviceFromShortName() {

    }

}
