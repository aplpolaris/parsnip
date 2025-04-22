/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * StandardDecodersTest.kt
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
package edu.jhuapl.data.parsnip.decode

import edu.jhuapl.testkt.shouldBe
import org.junit.Test

class StandardDecodersTest {

    @Test
    fun testOf() {
        of(Boolean::class.java) shouldBe StandardDecoders.BOOLEAN
        of(java.lang.Boolean::class.java) shouldBe StandardDecoders.BOOLEAN
    }
}
