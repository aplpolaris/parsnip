/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * JaskonTestUtils.kt
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
package edu.jhuapl.util.internal

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.readValue
import edu.jhuapl.data.parsnip.io.ParsnipMapper
import edu.jhuapl.data.parsnip.io.parsnipModule
import edu.jhuapl.testkt.shouldBe
import kotlin.test.assertNotNull

fun Any.printPlainMapperJsonTest() = println(ObjectMapper().writeValueAsString(this))
fun Any.prettyPrintPlainMapperJsonTest() = println(ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this))
fun Any.recyclePlainMapperJsonTest() = testRecycle(this)

fun Any.printJsonTest() = println(ParsnipMapper.writeValueAsString(this))
fun Any.prettyPrintJsonTest() = println(ParsnipMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this))
fun Any.prettyPrintYamlTest() = println(YAMLMapper().registerModule(parsnipModule()).writerWithDefaultPrettyPrinter().writeValueAsString(this))
inline fun <reified X> X.recycleJsonTest() = testRecycle(this)
inline fun <reified X> X.recycleYamlTest() = testYamlRecycle(this)

inline fun <reified X> testRecycle(value: X): X {
    val firstTime = ParsnipMapper.writeValueAsString(value)
    println(firstTime)
    val this2 = ParsnipMapper.readValue<X>(firstTime)
    val secondTime = ParsnipMapper.writeValueAsString(this2)
    println(secondTime)
    secondTime shouldBe firstTime
    return this2
}

inline fun <reified X> testYamlRecycle(value: X): X {
    val mapper = YAMLMapper().registerModule(parsnipModule())
    val firstTime = mapper.writeValueAsString(value)
    println(firstTime)
    val this2 = mapper.readValue<X>(firstTime)
    val secondTime = mapper.writeValueAsString(this2)
    println(secondTime)
    secondTime shouldBe firstTime
    return this2
}
