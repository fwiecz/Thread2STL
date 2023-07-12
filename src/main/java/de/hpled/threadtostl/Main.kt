package de.hpled.threadtostl

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import java.util.*

class Main : Application() {

    @Throws(Exception::class)
    override fun start(primaryStage: Stage) {
        val res = ResourceBundle.getBundle("strings")
        val root = FXMLLoader.load<Parent>(javaClass.getResource("/main.fxml"), res)
        primaryStage.apply {
            title = "Thread2STL"
            scene = Scene(root, 800.0, 600.0)
            minWidth = 400.0
            minHeight = 300.0
            show()
        }
    }
}

fun main() {
    Application.launch(Main::class.java)
}
