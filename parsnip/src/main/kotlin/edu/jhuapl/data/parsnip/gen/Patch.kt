/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * Patch.kt
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

import edu.jhuapl.data.parsnip.dataset.DataSequence
import edu.jhuapl.data.parsnip.datum.Datum
import edu.jhuapl.data.parsnip.datum.MutableDatum
import edu.jhuapl.util.types.nestedPutAll

/** Apply the given patch to this object. */
fun Datum.patch(patch: Datum): MutableDatum = toMutableMap().also { it.nestedPutAll(patch) }

/** Generate a dataset by applying randomly generated patches to a template datum. */
fun DataSequence.patch(template: Datum): DataSequence = map { template.patch(it) }
