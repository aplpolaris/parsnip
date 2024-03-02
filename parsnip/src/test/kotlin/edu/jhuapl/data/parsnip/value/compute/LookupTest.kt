package edu.jhuapl.data.parsnip.value.compute

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * LookupTest.kt
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

import edu.jhuapl.util.internal.printPlainMapperJsonTest
import junit.framework.TestCase
import org.junit.Test
import java.io.IOException

class LookupTest : TestCase() {

    @Test
    @Throws(IOException::class)
    fun testSerialize() {
        Lookup(mutableMapOf("1" to "one", "2" to "two")).printPlainMapperJsonTest()
    }

    @Test
    fun testApply() {
        val lookup = Lookup()
        assertEquals(null, lookup(0))

        lookup.table = mapOf("0" to "zero", "2" to "two", "null" to "fifteen")

        assertEquals("zero", lookup(0))
        assertEquals("fifteen", lookup(null))

        lookup.table = mapOf("0" to "zero", "2" to "two")
        lookup.ifNull = "fifteen"

        assertEquals("zero", lookup(0))
        assertEquals("fifteen", lookup(null))
    }

}
