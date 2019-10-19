package de.hpled.threadtostl

import javafx.geometry.Point3D
import javafx.scene.Group
import javafx.scene.PerspectiveCamera
import javafx.scene.SceneAntialiasing
import javafx.scene.SubScene
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.DrawMode
import javafx.scene.shape.MeshView
import javafx.scene.transform.Rotate

class PreviewScene : Pane() {

    private val root = Group()
    private val scene = SubScene(root, 100.0, 100.0, true, SceneAntialiasing.BALANCED)
    private val meshRotationZ = Rotate(0.0, Rotate.Z_AXIS)
    private val meshRotationX = Rotate(90.0, Rotate.X_AXIS)
    private val meshView = MeshView().apply {
        drawMode = DrawMode.FILL
        translateX = 0.0
        translateY = 0.0
        transforms.addAll(meshRotationX, meshRotationZ)
    }
    private val camera = PerspectiveCamera(true).apply {
        farClip = 2000.0
        nearClip = 0.1
        translateZ = -60.0
    }
    private val lastPos = doubleArrayOf(0.0, 0.0)

    init {
        scene.isManaged = true
        scene.fill = Color.SILVER
        scene.camera = camera
        scene.widthProperty().bind(widthProperty())
        scene.heightProperty().bind(heightProperty())

        children += scene
        root.children += meshView

        setOnMousePressed {
            lastPos[0] = it.x
            lastPos[1] = it.y
        }
        setOnMouseDragged {
            val movex = it.x - lastPos[0]
            val movey = it.y - lastPos[1]
            lastPos[0] = it.x
            lastPos[1] = it.y
            rotateCamera(movex, movey)
        }
    }

    private fun rotateCamera(x: Double, y: Double) {
        if(x != 0.0) {
            meshRotationZ.angle += x * 0.2
        }
        if(y != 0.0) {
            meshRotationX.angle += y * 0.2
        }
    }

    fun setModel(model: Model) {
        meshView.mesh = model.toMesh()
        meshRotationX.pivotZ = model.job.length * 0.5
    }

    companion object {
        private val yAxis = Point3D(0.0, 1.0, 0.0)
        private val xAxis = Point3D(1.0, 0.0, 0.0)
    }
}