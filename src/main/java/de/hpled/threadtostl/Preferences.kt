package de.hpled.threadtostl

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.control.ComboBox
import javafx.scene.control.Spinner
import javafx.scene.layout.VBox
import java.net.URL
import java.util.*

class Preferences : VBox(), Initializable {
    @FXML
    private lateinit var typeComboBox: ComboBox<String>
    @FXML
    private lateinit var threadDiaSpinner: Spinner<Double>
    @FXML
    private lateinit var threadLengthSpinner: Spinner<Double>
    @FXML
    private lateinit var threadAngleSpinner: Spinner<Double>
    @FXML
    private lateinit var threadStepSpinner: Spinner<Double>
    @FXML
    private lateinit var resolutionPreview: Spinner<Int>
    @FXML
    private lateinit var resolutionExport: Spinner<Int>

    private val resources = ResourceBundle.getBundle("strings")!!
    private val types = mapOf<Type, String>(
            Type.BOLT to resources.getString("label.typeBolt"),
            Type.NUT to resources.getString("label.typeNut")
    )

    init {
        FXMLLoader(javaClass.getResource("/prefs.fxml"), resources).apply {
            setRoot(this@Preferences)
            setController(this@Preferences)
            load()
        }
    }

    fun getJobPreview() = Job().apply {
        type = Type.values().get(typeComboBox.selectionModel.selectedIndex)
        outerDiameter = threadDiaSpinner.value
        length = threadLengthSpinner.value
        angle = threadAngleSpinner.value
        step = threadStepSpinner.value
        resolution = resolutionPreview.value
    }

    fun getJobExport() = getJobPreview().apply {
        resolution = resolutionExport.value
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        typeComboBox.items.setAll(types.values)
        typeComboBox.selectionModel.select(0)
    }
}