package edu.jhuapl.data.parsnip

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * Etl.kt
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

import com.fasterxml.jackson.annotation.JsonPropertyOrder
import edu.jhuapl.data.parsnip.dataset.DataSet
import edu.jhuapl.data.parsnip.dataset.DataSetTransform
import edu.jhuapl.data.parsnip.datum.Datum
import edu.jhuapl.data.parsnip.datum.transform.Create
import edu.jhuapl.data.parsnip.datum.filter.DatumFieldFilter
import edu.jhuapl.data.parsnip.datum.DatumTransform
import java.util.*

/**
 * Performs a generic extract-transform-load operation on input data, including a single filter operation, a set of transform
 * operations, and a single load operation. Operations are applied one datum at a time.
 *
 * @author Elisha Peterson
 */
@JsonPropertyOrder("extract", "transform", "load")
open class Etl {

    var extract = DatumFieldFilter()
    var transform: MutableList<DatumTransform> = ArrayList()
    var load = Create()

    operator fun invoke(map: Datum): Datum? {
        if (!extract(map)) {
            return null
        }
        var res = map
        transform.forEach { t -> res = t(res) ?: return null }
        return load(res)
    }

    fun asTransform() = object: DatumTransform {
        override fun invoke(p1: Datum) = this@Etl.invoke(p1)
    }

    fun asDataSetTransform() = object: DataSetTransform {
        override fun invoke(p1: DataSet) = p1.mapNotNull { this@Etl.invoke(it) }
    }

}
