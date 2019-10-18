package de.hpled.threadtostl

import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.layout.VBox
import java.net.URL
import java.util.*

class Preferences : VBox(), Initializable {

    init {
        val res = ResourceBundle.getBundle("strings")
        FXMLLoader(javaClass.getResource("/prefs.fxml"), res).apply {
            setRoot(this@Preferences)
            setController(this@Preferences)
            load()
        }
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {

    }
}