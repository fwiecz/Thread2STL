package de.hpled.threadtostl

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.control.ComboBox
import javafx.scene.control.Spinner
import javafx.scene.control.TextFormatter
import javafx.scene.control.TitledPane
import javafx.scene.layout.VBox
import javafx.util.StringConverter
import java.lang.Exception
import java.net.URL
import java.util.*
import java.util.function.UnaryOperator
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

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
    @FXML
    private lateinit var wallThicknessSpinner: Spinner<Double>
    @FXML
    private lateinit var wallPrefs: TitledPane

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
        wallThinkness = wallThicknessSpinner.value
    }

    fun getJobExport() = getJobPreview().apply {
        resolution = resolutionExport.value
    }

    private fun <T> addSpinnerListener(spinner: Spinner<T>) {
        spinner.focusedProperty().addListener { ob, ol, nw -> spinner.increment(0) }
        spinner.editor.textFormatter = TextFormatter<String>(object : UnaryOperator<TextFormatter.Change> {
            override fun apply(t: TextFormatter.Change): TextFormatter.Change {
                return t.apply {
                    if(t.controlNewText.isEmpty()) {
                        t.text = "0"
                    }
                    if(!t.controlNewText.matches(Regex("[0-9,.]*"))) {
                        t.text = ""
                    }
                }
            }
        })
    }

    fun loadPreset(job: Job?) {
        if(job != null) {
            typeComboBox.selectionModel.select( types.get(job.type) )
            threadDiaSpinner.valueFactory.value = job.outerDiameter
            threadLengthSpinner.valueFactory.value = job.length
            threadAngleSpinner.valueFactory.value = job.angle
            threadStepSpinner.valueFactory.value = job.step
            resolutionExport.valueFactory.value = job.resolution
            wallThicknessSpinner.valueFactory.value = job.wallThinkness
        }
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        typeComboBox.items.setAll(types.values)
        typeComboBox.selectionModel.selectedIndexProperty().addListener { observable, oldValue, newValue ->
            wallPrefs.isDisable = Type.values().get(newValue as Int) != Type.NUT
        }
        typeComboBox.selectionModel.select(0)

        addSpinnerListener(threadDiaSpinner)
        addSpinnerListener(threadLengthSpinner)
        addSpinnerListener(threadAngleSpinner)
        addSpinnerListener(threadStepSpinner)
        addSpinnerListener(resolutionPreview)
        addSpinnerListener(resolutionExport)
        addSpinnerListener(wallThicknessSpinner)
    }
}