package edu.jhuapl.data.parsnip

/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * BatchEtl.kt
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

import com.fasterxml.jackson.annotation.JsonPropertyOrder
import edu.jhuapl.data.parsnip.dataset.DataSet
import edu.jhuapl.data.parsnip.dataset.DataSetTransform
import edu.jhuapl.data.parsnip.datum.Datum
import edu.jhuapl.data.parsnip.datum.transform.Create
import edu.jhuapl.data.parsnip.datum.filter.DatumFieldFilter
import edu.jhuapl.data.parsnip.datum.DatumTransform
import edu.jhuapl.data.parsnip.datum.MultiDatumTransform
import java.util.*

/**
 * Performs a generic extract-transform-load operation on input data, including a single filter operation, a set of transform
 * operations, and a single load operation. Operations are applied in batches, allowing for transformations that change a
 * single datum to multiple datums.
 */
@JsonPropertyOrder("extract", "transform", "load")
class BatchEtl {

    var extract = DatumFieldFilter()
    var transform: MutableList<MultiDatumTransform> = ArrayList()
    var load = Create()

    operator fun invoke(p1: DataSet): DataSet {
        var res = p1.filter { extract(it) }
        transform.forEach { res = res.flatMap(it) }
        return res.map { load(it) }
    }

    fun asTransform() = object: DataSetTransform {
        override fun invoke(p1: DataSet) = this@BatchEtl.invoke(p1)
    }

}
