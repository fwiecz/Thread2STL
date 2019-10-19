package de.hpled.threadtostl

import com.sun.javafx.geom.Vec3f
import javafx.geometry.Point2D

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

    private fun finalizeToBolt(job: Job, model: Model, parts: List<TriPart>, progress: (p: Double) -> Unit): Model {
        var progCount = 0
        val progStep = 0.2 / (job.resolution * 4 + model.vertices.size)

        fun io(v: Vec3f): Int {
            return model.vertices.indexOf(v)
        }

        // close top
        val topCenter = Vec3f(0f, 0f, job.length.toFloat())
        model.vertices += topCenter
        val tcIndex = model.vertices.indexOf(topCenter)
        for (i in (parts.size - job.resolution - 1) until parts.size - 1) {
            val v1 = io(parts[i].top)
            val v2 = io(parts[i + 1].top)
            model.faces += intArrayOf(v1, v2, tcIndex)
            progress(progCount * progStep + 0.8).also { progCount++ }
        }

        // close bottom
        val bottomCenter = Vec3f(0f, 0f, 0f)
        model.vertices += bottomCenter
        val bcIndex = model.vertices.indexOf(bottomCenter)
        for (i in 0 until job.resolution) {
            val v1 = io(parts[i].bottom ?: parts[i].top)
            val v2 = io(parts[i + 1].bottom ?: parts[i + 1].top)
            model.faces += intArrayOf(bcIndex, v2, v1)
            progress(progCount * progStep + 0.8).also { progCount++ }
        }

        // rotate the whole object 180° so it fits into the corresponding nut when both are projected over one other.
        model.vertices.forEach {
            val dir = Vector2().apply { x = it.x; y = it.y }.rotate(180f)
            it.x = dir.x
            it.y = dir.y
            progress(progCount * progStep + 0.8).also { progCount++ }
        }

        return model
    }

    private fun finalizeToNut(job: Job, model: Model, parts: List<TriPart>, progress: (p: Double) -> Unit): Model {
        var progCount = 0
        val progStep = 0.2 / (job.resolution * 5)

        val angleStep = 360f / job.resolution
        val radius = (job.getRadius() + job.wallThinkness).toFloat()

        fun io(v: Vec3f): Int {
            return model.vertices.indexOf(v)
        }

        // top side
        val ringTop = mutableListOf<Vec3f>()
        for (i in 0 until job.resolution) {
            val angle = i * angleStep
            val dir = Vector2(radius).rotate(angle)
            ringTop += Vec3f(dir.x, dir.y, job.length.toFloat())
            progress(progCount * progStep + 0.8).also { progCount++ }
        }
        model.vertices.addAll(ringTop)

        // find nearest vertex to 0°
        val nearestIndex = (parts.size-job.resolution until parts.size).map {
            val t = parts[it].top
            val angle = Point2D(t.x.toDouble(), t.y.toDouble()).angle(1.0, 0.0)
            Pair(angle, it)
        }.sortedBy { it.first }.first().second - (parts.size - job.resolution)

        for (i in 0 until job.resolution) {
            val in1 = parts[(parts.size - job.resolution) + (i + nearestIndex) % job.resolution].top
            val in2 = parts[(parts.size - job.resolution) + (i + nearestIndex + 1) % job.resolution].top
            val out1 = ringTop[i]
            val out2 = ringTop[(i + 1) % job.resolution]
            model.faces += intArrayOf(io(in1), io(out1), io(out2))
            model.faces += intArrayOf(io(in1), io(out2), io(in2))
            progress(progCount * progStep + 0.8).also { progCount++ }
        }

        // bottom side
        val ringBot = mutableListOf<Vec3f>()
        for (i in 0 until job.resolution) {
            val angle = i * angleStep
            val dir = Vector2(radius).rotate(angle)
            ringBot += Vec3f(dir.x, dir.y, 0f)
            progress(progCount * progStep + 0.8).also { progCount++ }
        }
        model.vertices.addAll(ringBot)

        for (i in 0 until job.resolution) {
            val in1 = parts[i].bottom ?: parts[i].top
            val in2 = parts[(i + 1) % job.resolution].bottom ?: parts[(i + 1) % job.resolution].top
            val out1 = ringBot[i]
            val out2 = ringBot[(i + 1) % job.resolution]
            model.faces += intArrayOf(io(in1), io(out1), io(out2)).apply { reverse() }
            model.faces += intArrayOf(io(in1), io(out2), io(in2)).apply { reverse() }
            progress(progCount * progStep + 0.8).also { progCount++ }
        }

        // connect rings
        for (i in 0 until job.resolution) {
            val top1 = ringTop[i]
            val top2 = ringTop[(i + 1) % job.resolution]
            val bot1 = ringBot[i]
            val bot2 = ringBot[(i + 1) % job.resolution]
            model.faces += intArrayOf(io(top1), io(bot2), io(top2))
            model.faces += intArrayOf(io(top1), io(bot1), io(bot2))
            progress(progCount * progStep + 0.8).also { progCount++ }
        }

        return model
    }

    private fun createModelFromTriParts(job: Job, parts: List<TriPart>, progress: (p: Double) -> Unit): Model {
        var progCount = 0
        val progStep = 0.5 / (parts.size)

        val model = Model(job)

        // Reverse vertice enumeration for inverse faces when Type == NUT
        val rev = if (job.type == Type.BOLT) false else true

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

        model.faces += intArrayOf(io(parts[0].top), io(parts[1].tip!!), io(parts[1].top)).apply { if (rev) reverse() }
        model.faces += intArrayOf(io(parts[0].top), io(parts[1].bottom!!), io(parts[1].tip!!)).apply { if (rev) reverse() }
        for (i in 2 until parts.size - 1) {
            model.faces += intArrayOf(io(parts[i].top), io(parts[i - 1].top), io(parts[i].tip!!)).apply { if (rev) reverse() }
            model.faces += intArrayOf(io(parts[i].tip!!), io(parts[i - 1].top), io(parts[i - 1].tip!!)).apply { if (rev) reverse() }
            model.faces += intArrayOf(io(parts[i].tip!!), io(parts[i - 1].tip!!), io(parts[i].bottom!!)).apply { if (rev) reverse() }
            model.faces += intArrayOf(io(parts[i].bottom!!), io(parts[i - 1].tip!!), io(parts[i - 1].bottom!!)).apply { if (rev) reverse() }
            progress(progCount * progStep + 0.3).also { progCount++ }
        }
        val s = parts.size - 1
        model.faces += intArrayOf(io(parts[s].top), io(parts[s - 1].top), io(parts[s - 1].tip!!)).apply { if (rev) reverse() }
        model.faces += intArrayOf(io(parts[s].top), io(parts[s - 1].tip!!), io(parts[s - 1].bottom!!)).apply { if (rev) reverse() }

        return model
    }

    private fun createThread(job: Job, progress: (p: Double) -> Unit): List<TriPart> {
        val pitchPerStep = (job.step / job.resolution).toFloat()
        val pitchTotal = (job.length / pitchPerStep).toInt()
        val angleStep = 360f / job.resolution

        val tipLength = TriPart(Vec3f(0f, 0f, job.step.toFloat()), Vec3f(0f, 0f, 0f))
                .getTipLength(job.angle.toFloat())
        val radius = if (job.type == Type.BOLT) {
            job.getRadius() - tipLength
        } else {
            job.getRadius()
        }
        val tipDir = if (job.type == Type.BOLT) 1 else -1

        // including first vertex
        val a = Vector2(radius)
        val parts = mutableListOf(TriPart(Vec3f(a.x, a.y, 0f)))

        val progStep = 0.3 / (pitchTotal + job.resolution)
        var progCount = 1

        // Bottom, first round
        for (i in 1 until job.resolution) {
            val angle = i * angleStep
            val dir = Vector2(radius).rotate(angle)
            val bottom = Vec3f(dir.x, dir.y, 0f)
            val top = Vec3f(bottom).apply { z = pitchPerStep * i }
            val t = TriPart(top, bottom).apply {
                createTip(getTipLength(job.angle.toFloat()) * tipDir, angle, radius)
            }
            parts += t
            progress(progCount * progStep).also { progCount++ }
        }

        // mid part
        for (i in job.resolution until pitchTotal) {
            val angle = (i * angleStep) % 360
            val bottom = parts[i - job.resolution].top
            val top = Vec3f(bottom).apply { z = pitchPerStep * i }
            val t = TriPart(top, bottom).apply { createTip(tipLength * tipDir, angle, radius) }
            parts += t
            progress(progCount * progStep).also { progCount++ }
        }

        // top end part
        for (i in pitchTotal until (pitchTotal + job.resolution)) {
            val angle = (i * angleStep) % 360
            val bottom = parts[i - job.resolution].top
            val top = Vec3f(bottom).apply { z = job.length.toFloat() }
            val t = TriPart(top, bottom).apply {
                createTip(getTipLength(job.angle.toFloat()) * tipDir, angle, radius)
            }
            parts += t
            progress(progCount * progStep).also { progCount++ }
        }

        // final vertex
        parts += TriPart(parts[pitchTotal].top)
        return parts
    }

    fun create(job: Job, progress: (p: Double) -> Unit): Model {
        val threadParts = createThread(job, progress)
        val model = createModelFromTriParts(job, threadParts, progress)
        return when (job.type) {
            Type.BOLT -> finalizeToBolt(job, model, threadParts, progress)
            Type.NUT -> finalizeToNut(job, model, threadParts, progress)
        }
    }
}