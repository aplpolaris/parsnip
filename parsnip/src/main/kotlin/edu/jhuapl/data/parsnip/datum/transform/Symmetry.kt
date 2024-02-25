package edu.jhuapl.data.parsnip.datum.transform

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * Symmetry.kt
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

import edu.jhuapl.data.parsnip.datum.Datum
import edu.jhuapl.data.parsnip.datum.DatumTransform
import edu.jhuapl.util.types.SimpleValue

/**
 * A way of transforming a datum between valid representations of the same underlying content. For instance, this might
 * interchange the fields (ip1,host1,port1) and (ip2,host2,port2).
 *
 * The transformation is encoded in a map, e.g. `ip1->ip2, host1->host2, port1->port2`. Both the map and its inverse are
 * applied in the data transformation.
 */
data class Symmetry(var transform: MutableMap<String, String> = mutableMapOf()) : DatumTransform, SimpleValue {

    override val simpleValue: Any?
        get() = transform

    constructor(vararg pairs: Pair<String, String>): this(mutableMapOf(*pairs))
    constructor(k: String, v: String): this(mutableMapOf(k to v))
    constructor(k1: String, v1: String, k2: String, v2: String): this(mutableMapOf(k1 to v1, k2 to v2))
    constructor(k1: String, v1: String, k2: String, v2: String, k3: String, v3: String): this(mutableMapOf(k1 to v1, k2 to v2, k3 to v3))

    fun copy() = Symmetry(transform)

    override fun invoke(t: Datum): Datum {
        val res = t.toMutableMap()
        transform.forEach { (k, v) ->
            res[v] = t[k]
            res[k] = t[v]
        }
        return res
    }

}
