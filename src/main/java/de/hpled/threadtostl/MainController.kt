package de.hpled.threadtostl

import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.ProgressBar
import javafx.scene.layout.VBox
import javafx.scene.shape.DrawMode
import javafx.stage.FileChooser
import java.io.BufferedWriter
import java.io.FileWriter
import java.lang.Exception
import java.net.URL
import java.util.*
import java.util.concurrent.ScheduledThreadPoolExecutor
import kotlin.math.max
import kotlin.math.min

class MainController : Initializable {
    @FXML
    private lateinit var mRoot: VBox
    @FXML
    private lateinit var preferences: Preferences
    @FXML
    private lateinit var previewScene: PreviewScene
    @FXML
    private lateinit var updatePreviewButton: Button
    @FXML
    private lateinit var progressBar: ProgressBar
    @FXML
    private lateinit var exportButton: Button
    @FXML
    private lateinit var previewMode: ComboBox<String>

    private val stlService = ModellingService()
    private val executor = ScheduledThreadPoolExecutor(0)
    private val resources = ResourceBundle.getBundle("strings")!!
    private val isRendering = SimpleBooleanProperty(false)
    private val drawModeMap = mapOf (
            DrawMode.FILL to resources.getString("label.drawmode.fill"),
            DrawMode.LINE to resources.getString("label.drawmode.wire")
    )

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        updatePreviewButton.setOnAction { rebuildModel() }
        exportButton.setOnAction { exportToSTL() }
        isRendering.addListener { observable, oldValue, newValue ->
            exportButton.isDisable = newValue
            updatePreviewButton.isDisable = newValue
        }
        previewMode.items.setAll(drawModeMap.values)
        previewMode.selectionModel.select(0)
        previewMode.selectionModel.selectedIndexProperty().addListener {observable, oldValue, newValue ->
            previewScene.setDrawMode( drawModeMap.keys.toList().get(newValue as Int) )
        }
        rebuildModel()
    }

    private fun exportToSTL() {
        val chooser = FileChooser().apply {
            title = resources.getString("label.exportFileTitle")
            extensionFilters.setAll(FileChooser.ExtensionFilter("STL Format", listOf("*.stl")))
        }
        val file = chooser.showSaveDialog(mRoot.scene.window)
        if(file != null) {
            if(!file.exists()) {
                file.createNewFile()
            }
            isRendering.value = true
            progressBar.isVisible = true
            progressBar.progress = 0.0
            executor.execute {
                try {
                    val model = stlService.create(preferences.getJobExport()) {
                        Platform.runLater { progressBar.progress = min(1.0, max(0.0, it)) }
                    }
                    val writer = FileWriter(file)
                    writer.write(model.toSTL {
                        Platform.runLater { progressBar.progress = min(1.0, max(0.0, it)) }
                    })
                    writer.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    Platform.runLater {
                        progressBar.isVisible = false
                        isRendering.value = false
                    }
                }
            }
        }
    }

    private fun rebuildModel() {
        isRendering.value = true
        progressBar.isVisible = true
        progressBar.progress = 0.0
        executor.execute {
            try {
                val model = stlService.create(preferences.getJobPreview()) {
                    Platform.runLater { progressBar.progress = min(1.0, max(0.0, it)) }
                }
                previewScene.setModel(model)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                Platform.runLater {
                    progressBar.isVisible = false
                    isRendering.value = false
                }
            }
        }
    }
}