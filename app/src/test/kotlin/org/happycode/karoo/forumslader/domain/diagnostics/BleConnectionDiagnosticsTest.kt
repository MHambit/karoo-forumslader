package org.happycode.karoo.forumslader.domain.diagnostics

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe

class BleConnectionDiagnosticsTest : FunSpec({

    test("should report idle placeholder and 100% data quality before first connection") {
        // given
        val diagnostics = BleConnectionDiagnostics()

        // when / then
        diagnostics.hasConnected shouldBe false
        diagnostics.uptimeMs(nowMs = 10_000L) shouldBe 0L
        diagnostics.disconnectsPerHour(nowMs = 10_000L) shouldBe 0f
        diagnostics.stabilityPct(nowMs = 10_000L) shouldBe 100f
        diagnostics.dataQualityPct shouldBe 100f
        diagnostics.formatCompact(nowMs = 10_000L) shouldBe "--"
    }

    test("should apply 1-hour floor to disconnect rate during short sessions") {
        // given: connected 2 minutes ago with 1 unexpected disconnect
        val connectMs = 1_000_000L
        val nowMs = connectMs + 2 * 60_000L
        val diagnostics = BleConnectionDiagnostics(
            unexpectedDisconnectCount = 1,
            connectTimestampMs = connectMs,
            checksumPassCount = 99,
            checksumFailCount = 1,
        )

        // when / then
        diagnostics.disconnectsPerHour(nowMs) shouldBe (1.0f plusOrMinus 0.001f)
        diagnostics.stabilityPct(nowMs) shouldBe (0.0f plusOrMinus 0.001f)
        diagnostics.dataQualityPct shouldBe (99.0f plusOrMinus 0.001f)
        diagnostics.formatCompact(nowMs) shouldBe "S:0% Q:99% D:1.0/h"
    }

    test("should calculate stability index and disconnect rate accurately over multi-hour session") {
        // given: 4 hours uptime, 1 unexpected disconnect => 0.25/h => 75% stability
        val connectMs = 0L
        val nowMs = 4 * 3_600_000L
        val diagnostics = BleConnectionDiagnostics(
            unexpectedDisconnectCount = 1,
            connectTimestampMs = connectMs,
            checksumPassCount = 198,
            checksumFailCount = 2,
        )

        // when / then
        diagnostics.uptimeHours(nowMs) shouldBe (4.0f plusOrMinus 0.001f)
        diagnostics.disconnectsPerHour(nowMs) shouldBe (0.25f plusOrMinus 0.001f)
        diagnostics.stabilityPct(nowMs) shouldBe (75.0f plusOrMinus 0.001f)
        diagnostics.dataQualityPct shouldBe (99.0f plusOrMinus 0.001f)
        diagnostics.formatCompact(nowMs) shouldBe "S:75% Q:99% D:0.3/h"
    }

    test("should clamp stability index to 0% when disconnect rate exceeds 1 per hour") {
        // given: 2 hours uptime, 5 unexpected disconnects => 2.5/h => clamped to 0%
        val diagnostics = BleConnectionDiagnostics(
            unexpectedDisconnectCount = 5,
            connectTimestampMs = 0L,
        )

        // when / then
        diagnostics.disconnectsPerHour(nowMs = 2 * 3_600_000L) shouldBe (2.5f plusOrMinus 0.001f)
        diagnostics.stabilityPct(nowMs = 2 * 3_600_000L) shouldBe 0f
    }

    test("should clamp negative elapsed time to zero") {
        // given
        val diagnostics = BleConnectionDiagnostics(connectTimestampMs = 5_000L)

        // when / then
        diagnostics.uptimeMs(nowMs = 4_000L) shouldBe 0L
    }
})
