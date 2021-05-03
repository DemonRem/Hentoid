package me.devsaki.hentoid.database.domains

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import me.devsaki.hentoid.util.Helper

@Entity
data class DuplicateEntry(
        val referenceId: Long,
        val referenceSize: Long,
        var duplicateId: Long = -1,
        val titleScore: Float = 0f,
        val coverScore: Float = 0f,
        val artistScore: Float = 0f,
        @Id var id: Long = 0) { // ID is mandatory for ObjectBox to work

    @Transient
    private var totalScore = -1f

    @Transient
    var nbDuplicates = 1

    @Transient
    var referenceContent: Content? = null

    @Transient
    var duplicateContent: Content? = null

    @Transient
    var keep: Boolean? = null


    fun calcTotalScore(): Float {
        if (totalScore > -1) return totalScore
        // Calculate
        val operands = ArrayList<android.util.Pair<Float, Float>>()
        if (titleScore > -1) operands.add(android.util.Pair<Float, Float>(titleScore, 1f))
        if (coverScore > -1) operands.add(android.util.Pair<Float, Float>(coverScore, 1f))
        if (artistScore > -1) operands.add(android.util.Pair<Float, Float>(artistScore, 0.5f))
        return Helper.weigthedAverage(operands)
    }

    fun hash64(): Long {
        return Helper.hash64(("$referenceId.$duplicateId").toByteArray())
    }
}