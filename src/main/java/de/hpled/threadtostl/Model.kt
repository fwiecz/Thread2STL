package de.hpled.threadtostl

import com.sun.javafx.geom.Vec3f
import javafx.scene.shape.TriangleMesh
import javafx.scene.shape.VertexFormat
import java.util.*
import kotlin.collections.ArrayList

class Model(val job: Job){
    val vertices = ArrayList<Vec3f>(1000)
    val faces = ArrayList<IntArray>(1000)

    fun toMesh(): TriangleMesh {
        val m = TriangleMesh(VertexFormat.POINT_TEXCOORD)
        val points = vertices.fold(LinkedList<Float>(), { acc, vec3f ->
            acc.apply {
                add(vec3f.x)
                add(vec3f.y)
                add(vec3f.z)
            }
        }).toFloatArray()
        m.points.setAll(points, 0, points.size)

        m.texCoords.setAll(*coords)

        val fs = faces.fold(LinkedList<Int>(), { acc, ints ->
            acc.apply {
                add(ints[0]); add(0)
                add(ints[1]); add(1)
                add(ints[2]); add(2)
            }
        }).toIntArray()
        m.faces.setAll(fs, 0, fs.size)
        return m
    }

    companion object {
        private val coords = floatArrayOf(0f, 0f, 0f, 1f, 1f, 0f, 1f, 1f)
    }
}