/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * ParsnipMapperTest.kt
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
package edu.jhuapl.data.parsnip.io

import edu.jhuapl.data.parsnip.datum.transform.Augment
import edu.jhuapl.testkt.shouldBe
import org.junit.Test
import java.util.stream.Stream

class ParsnipMapperTest {

    @Test
    fun testPackageName(){
        packageName(Int::class.java) shouldBe "java.lang"
        packageName(Augment::class.java) shouldBe "edu.jhuapl.data.parsnip.datum.transform"
    }
}
