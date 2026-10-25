package com.migraineguard.app.feature.fitness

import kotlinx.serialization.Serializable

/** Display choice only; it does not infer or change the account's body profile. */
enum class AnatomyIllustration(val label: String) { MALE("Male"), FEMALE("Female") }

@Serializable
data class AnatomyPart(val slug: String, val paths: List<String>)

@Serializable
data class AnatomyAsset(val viewBox: List<Float>, val outline: String, val parts: List<AnatomyPart>) {
    fun validate() {
        require(viewBox.size == 4 && viewBox.all { it.isFinite() })
        require(viewBox[2] > 0f && viewBox[3] > 0f)
        require(outline.isNotBlank() && parts.isNotEmpty())
        require(parts.all { it.slug.isNotBlank() && it.paths.isNotEmpty() && it.paths.all(String::isNotBlank) })
    }
}

/** SVG space and Canvas space use the same transform for drawing and hit testing. */
data class AnatomyTransform(val scale: Float, val left: Float, val top: Float, val originX: Float, val originY: Float) {
    fun bodyX(canvasX: Float) = (canvasX - left) / scale + originX
    fun bodyY(canvasY: Float) = (canvasY - top) / scale + originY
    fun canvasX(bodyX: Float) = left + (bodyX - originX) * scale
    fun canvasY(bodyY: Float) = top + (bodyY - originY) * scale

    companion object {
        fun fit(box: List<Float>, width: Float, height: Float): AnatomyTransform {
            require(box.size == 4 && box.all { it.isFinite() } && box[2] > 0 && box[3] > 0)
            require(width.isFinite() && height.isFinite() && width > 0 && height > 0)
            val scale = minOf(width / box[2], height / box[3]) * .97f
            return AnatomyTransform(scale, (width - box[2] * scale) / 2, (height - box[3] * scale) / 2, box[0], box[1])
        }
    }
}

object AnatomyRegionMapping {
    fun muscleFor(slug: String): Muscle? = when (slug) {
        "chest" -> Muscle.CHEST
        "deltoids" -> Muscle.SHOULDERS
        "biceps" -> Muscle.BICEPS
        "triceps" -> Muscle.TRICEPS
        "abs", "lower-back" -> Muscle.CORE
        "obliques" -> Muscle.OBLIQUES
        "trapezius" -> Muscle.UPPER_BACK
        "upper-back" -> Muscle.LATS // The source's broad lateral-back surfaces.
        "gluteal" -> Muscle.GLUTES
        "quadriceps" -> Muscle.QUADS
        "hamstring" -> Muscle.HAMSTRINGS
        "calves" -> Muscle.CALVES
        else -> null // Head, joints, forearms, inner thighs and shins are not mislabeled as catalog targets.
    }
}
