package de.hpled.threadtostl

import com.sun.javafx.geom.Vec3f

data class TriPart(
        var top: Vec3f,
        var bottom: Vec3f? = null,
        var tip: Vec3f? = null
) {
    fun createTip(length: Float, orientation: Float, radius: Float) {
        val b = bottom
        if (b != null) {
            val v = Vector2(radius).rotate(orientation)
            tip = Vec3f(v.x, v.y, 0f).apply {
                val targetLength = length() + length
                normalize()
                mul(targetLength)
                z = b.z + (top.z - b.z) * 0.5f
            }
        }
    }

    fun getTipLength(targetAngle: Float): Float {
        val b = bottom
        if (b != null) {
            val alpha = Math.toRadians((180f - 90f - (targetAngle * 0.5)))
            val center = Vec3f(top).apply { sub(b); mul(0.5f) }
            val sideB = center.length()
            return (Math.tan(alpha) * sideB).toFloat()
        }
        return 0f
    }
}

class ModellingService {

    private fun createModelFromTriParts(job: Job, parts: List<TriPart>): Model {
        val model = Model(job)
        // since we are reusing vertices (top & bottom of some TriPoints are the same)
        val points = parts.fold(mutableSetOf<Vec3f>(), { acc, triPart ->
            acc.add(triPart.top)
            triPart.tip?.apply { acc.add(this) }
            triPart.bottom?.apply { acc.add(this) }
            acc
        })
        model.vertices.addAll(points)
        fun io(v: Vec3f): Int {
            return points.indexOf(v)
        }
        model.faces += intArrayOf(io(parts[0].top), io(parts[1].tip!!), io(parts[1].top))
        model.faces += intArrayOf(io(parts[0].top), io(parts[1].bottom!!), io(parts[1].tip!!))
        for (i in 2 until parts.size - 1) {
            model.faces += intArrayOf(io(parts[i].top), io(parts[i - 1].top), io(parts[i].tip!!))
            model.faces += intArrayOf(io(parts[i].tip!!), io(parts[i - 1].top), io(parts[i - 1].tip!!))
            model.faces += intArrayOf(io(parts[i].tip!!), io(parts[i - 1].tip!!), io(parts[i].bottom!!))
            model.faces += intArrayOf(io(parts[i].bottom!!), io(parts[i - 1].tip!!), io(parts[i - 1].bottom!!))
        }
        val s = parts.size - 1
        model.faces += intArrayOf(io(parts[s].top), io(parts[s - 1].top), io(parts[s - 1].tip!!))
        model.faces += intArrayOf(io(parts[s].top), io(parts[s - 1].tip!!), io(parts[s - 1].bottom!!))

        // close top
        val topCenter = Vec3f(0f, 0f, job.length.toFloat())
        model.vertices += topCenter
        val tcIndex = model.vertices.indexOf(topCenter)
        for(i in (parts.size - job.resolution -1) until parts.size-1) {
            val v1 = io(parts[i].top)
            val v2 = io(parts[i+1].top)
            model.faces += intArrayOf(v1, v2, tcIndex)
        }

        // close bottom
        val bottomCenter = Vec3f(0f, 0f, 0f)
        model.vertices += bottomCenter
        val bcIndex = model.vertices.indexOf(bottomCenter)
        for(i in 0 until job.resolution) {
            val v1 = io(parts[i].bottom ?: parts[i].top)
            val v2 = io(parts[i+1].bottom ?: parts[i+1].top)
            model.faces += intArrayOf(bcIndex, v2, v1)
        }

        return model
    }

    fun create(job: Job): Model {
        val pitchPerStep = (job.step / job.resolution).toFloat()
        println("Pitch per step: ${pitchPerStep}mm")
        val pitchTotal = (job.length / pitchPerStep).toInt()
        println("Pitch total: $pitchTotal")
        val angleStep = 360f / job.resolution
        println("Angle per step: $angleStep")

        val tipLength = TriPart(Vec3f(0f, 0f, job.step.toFloat()), Vec3f(0f, 0f, 0f) )
                .getTipLength(job.angle.toFloat())
        val radius = job.getRadius() - tipLength

        // including first vertex
        val a = Vector2(radius)
        val parts = mutableListOf(TriPart(Vec3f(a.x, a.y, 0f)))


        // Bottom, first round
        for (i in 1 until job.resolution) {
            val angle = i * angleStep
            val dir = Vector2(radius).rotate(angle)
            val bottom = Vec3f(dir.x, dir.y, 0f)
            val top = Vec3f(bottom).apply { z = pitchPerStep * i }
            val t = TriPart(top, bottom).apply {
                createTip(getTipLength(job.angle.toFloat()), angle, radius)
            }
            parts += t
        }

        // mid part
        for (i in job.resolution until pitchTotal) {
            val angle = (i * angleStep) % 360
            val bottom = parts[i - job.resolution].top
            val top = Vec3f(bottom).apply { z = pitchPerStep * i }
            val t = TriPart(top, bottom).apply { createTip(tipLength, angle, radius) }
            parts += t
        }

        // top end part
        for (i in pitchTotal until (pitchTotal + job.resolution)) {
            val angle = (i * angleStep) % 360
            val bottom = parts[i - job.resolution].top
            val top = Vec3f(bottom).apply { z = job.length.toFloat() }
            val t = TriPart(top, bottom).apply {
                createTip(getTipLength(job.angle.toFloat()), angle, radius)
            }
            parts += t
        }

        // final vertex
        parts += TriPart(parts[pitchTotal].top)

        return createModelFromTriParts(job, parts)
    }
}