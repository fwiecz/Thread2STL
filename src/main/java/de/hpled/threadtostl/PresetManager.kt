package de.hpled.threadtostl

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import javafx.stage.FileChooser
import javafx.stage.Window
import java.io.FileReader
import java.io.FileWriter
import java.lang.Exception
import java.util.*

class PresetManager {
    private val gson = Gson()
    private val resources = ResourceBundle.getBundle("strings")!!

    fun savePreset(job: Job, window: Window) {
        val chooser = FileChooser().apply {
            title = resources.getString("label.savePreset")
            extensionFilters.setAll(FileChooser.ExtensionFilter("JSON Format", listOf("*.json")))
        }
        val file = chooser.showSaveDialog(window)

        if(file != null) {
            if(!file.exists()) {
                file.createNewFile()
            }
            val writer = FileWriter(file)
            try {
                val json = gson.toJson(job)
                writer.write(json)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                writer.close()
            }
        }
    }

    fun loadPreset(window: Window) : Job? {
        val chooser = FileChooser().apply {
            title = resources.getString("label.openPreset")
            extensionFilters.setAll(FileChooser.ExtensionFilter("JSON Format", listOf("*.json")))
        }
        val file = chooser.showOpenDialog(window)
        if(file != null) {
            try {
                val job = gson.fromJson<Job>(JsonReader(FileReader(file)), Job::class.java) as Job
                return job
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }
}