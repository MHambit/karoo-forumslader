package org.happycode.karoo.forumslader.domain.diagnostics

import java.util.Locale
import kotlin.math.roundToInt

data class BleConnectionDiagnostics(
    val unexpectedDisconnectCount: Int = 0,
    val connectTimestampMs: Long? = null,
    val checksumPassCount: Int = 0,
    val checksumFailCount: Int = 0,
) {
    val hasConnected: Boolean
        get() = connectTimestampMs != null

    val dataQualityPct: Float
        get() {
            val total = checksumPassCount + checksumFailCount
            return if (total > 0) checksumPassCount.toFloat() / total * 100f else 100f
        }

    fun uptimeMs(nowMs: Long): Long =
        connectTimestampMs?.let { (nowMs - it).coerceAtLeast(0L) } ?: 0L

    fun uptimeHours(nowMs: Long): Float =
        uptimeMs(nowMs) / MILLIS_PER_HOUR

    fun disconnectsPerHour(nowMs: Long): Float =
        if (connectTimestampMs == null) {
            0f
        } else {
            unexpectedDisconnectCount / maxOf(MIN_UPTIME_HOURS_DENOMINATOR, uptimeHours(nowMs))
        }

    fun stabilityPct(nowMs: Long): Float =
        maxOf(0f, (1f - disconnectsPerHour(nowMs)) * 100f)

    fun formatCompact(nowMs: Long): String {
        if (connectTimestampMs == null) return IDLE_PLACEHOLDER
        val stability = stabilityPct(nowMs).roundToInt()
        val quality = dataQualityPct.roundToInt()
        val rate = "%.1f".format(Locale.US, disconnectsPerHour(nowMs))
        return "S:$stability% Q:$quality% D:$rate/h"
    }

    companion object {
        private const val MILLIS_PER_HOUR = 3_600_000f
        private const val MIN_UPTIME_HOURS_DENOMINATOR = 1f
        const val IDLE_PLACEHOLDER = "--"
        const val PREVIEW_TEXT = "S:98% Q:100% D:0.2/h"
    }
}
