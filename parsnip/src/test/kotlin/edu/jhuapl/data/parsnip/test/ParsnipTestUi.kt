/*-
 * #%L
 * ******************************* UNCLASSIFIED *******************************
 * ParsnipTestUi.kt
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
package edu.jhuapl.data.parsnip.test

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.readValue
import edu.jhuapl.data.parsnip.Etl
import edu.jhuapl.data.parsnip.datum.Datum
import edu.jhuapl.data.parsnip.io.ParsnipMapper
import edu.jhuapl.data.parsnip.io.parsnipModule
import edu.jhuapl.utilkt.core.javaTrim
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.input.TransferMode
import javafx.scene.layout.Priority.ALWAYS
import javafx.util.Duration.seconds
import tornadofx.*
import java.io.File
import java.nio.charset.Charset


fun main(args: Array<String>) {
    launch<ParsnipTestUi>(args)
}

class ParsnipTestUi : App(TestView::class, TestCss::class)

class TestCss : Stylesheet() {
    init {
        label {
            fontSize = 24.px
        }

        textArea {
            fontFamily = "Consolas"
            fontSize = 18.px
        }
    }
}

class TestView : View() {
    private val input = SimpleStringProperty()
    private val etlText = SimpleStringProperty().apply {
        addListener { _, _, _ -> etlTextChanged = true }
    }
    private val output = SimpleStringProperty()
    private val status = SimpleStringProperty()
    private var etlTextChanged = false
    private var etl = Etl()

    //region UI LAYOUT

    override val root = borderpane {
        prefWidth = 900.0
        prefHeight = 500.0

        top = toolbar {
            button("Compute output") { action { computeOutput() } }
        }

        center = hbox {
            padding = Insets(5.0, 5.0, 5.0, 5.0)
            spacing = 5.0
            vbox {
                hgrow = ALWAYS
                label("Input JSON Data")
                textarea(input) { vgrow = ALWAYS; isWrapText = true }
            }
            vbox {
                hgrow = ALWAYS
                label("ETL Description")
                textarea(etlText) { vgrow = ALWAYS; isWrapText = true }
            }
            vbox {
                hgrow = ALWAYS
                label("Output JSON Data")
                textarea(output) { vgrow = ALWAYS; isWrapText = true; isEditable = false }
            }
        }

        onDragOver = EventHandler {
            if (it.dragboard.hasFiles())
                it.acceptTransferModes(TransferMode.COPY);
            else
                it.consume();
        }
        onDragDropped = EventHandler {
            it.dragboard.files.forEach {
                handleFileInput(it)
            }
            it.consume()
        }

        bottom = label(status)
    }

    //endregion

    fun handleFileInput(f: File) {
        val content = f.readText(Charset.defaultCharset())
        val list = content.splitToSequence("---").filter { !it.isBlank() }
                .map { it.javaTrim() }.toList()
        input.value = list[0]
        etlText.value = list[1]
    }

    fun computeOutput() {
        try {
            val inputMap = ParsnipMapper.readValue(input.value) as Datum
            if (etlTextChanged) {
                etl = YAMLMapper().registerModule(parsnipModule())
                        .readValue(etlText.value) as Etl
            }
            output.value = ParsnipMapper.writerWithDefaultPrettyPrinter().writeValueAsString(etl(inputMap))
            status.value = "Updated output"
            etlTextChanged = false
            afterTwoSeconds { status.value = "" }
        } catch (x: Exception) {
            status.value = x.toString()
        }
    }

    private fun afterTwoSeconds(f: () -> Unit) {
        timeline {
            keyframe(seconds(2.0)) {
                onFinished = EventHandler { f() }
            }
        }
    }
}
