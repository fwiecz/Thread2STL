package de.hpled.threadtostl

import com.sun.javafx.geom.Vec3f
import javafx.scene.shape.TriangleMesh
import javafx.scene.shape.VertexFormat
import java.lang.StringBuilder
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

    fun toSTL(progress: (p: Double) -> Unit) : String {
        val sb = StringBuilder()
        sb.append("solid ascii\n")
        faces.forEachIndexed { index, ints ->
            val (p1, p2, p3) = Triple(vertices[ints[0]], vertices[ints[1]], vertices[ints[2]])
            val normal = Vec3f(p2).apply { sub(p1); cross(this, Vec3f(p2).apply { sub(p1) }); normalize() }
            sb.append("  facet normal ${normal.x} ${normal.y} ${normal.z}\n")
            sb.append("    outer loop\n")
            sb.append("      vertex ${p1.x} ${p1.y} ${p1.z}\n")
            sb.append("      vertex ${p2.x} ${p2.y} ${p2.z}\n")
            sb.append("      vertex ${p3.x} ${p3.y} ${p3.z}\n")
            sb.append("    endloop\n")
            sb.append("  endfacet\n")
            progress(index.toDouble() / faces.size)
        }
        sb.append("endsolid ascii\n")
        return sb.toString()
    }

    companion object {
        private val coords = floatArrayOf(0f, 0f, 0f, 1f, 1f, 0f, 1f, 1f)
    }
}