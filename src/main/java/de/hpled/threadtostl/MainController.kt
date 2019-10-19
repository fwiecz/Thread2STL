package de.hpled.threadtostl

import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import java.net.URL
import java.util.*

class MainController : Initializable {

    @FXML
    private lateinit var preferences: Preferences
    @FXML
    private lateinit var previewScene: PreviewScene
    @FXML
    private lateinit var exportButton: Button

    private val stlService = ModellingService()

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        exportButton.setOnAction { rebuildModel() }
        rebuildModel()
    }

    private fun rebuildModel() {
        val model = stlService.create( preferences.getJobPreview() )
        previewScene.setModel(model)
    }
}