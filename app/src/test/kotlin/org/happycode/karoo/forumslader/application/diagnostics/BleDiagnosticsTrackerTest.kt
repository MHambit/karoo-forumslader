package org.happycode.karoo.forumslader.application.diagnostics

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.happycode.karoo.forumslader.model.ForumsladerParser
import org.happycode.karoo.forumslader.ui.main.components.formatUptime

class BleDiagnosticsTrackerTest : FunSpec({

    test("should record first connection timestamp once and preserve it across reconnects") {
        // given
        var now = 1_000L
        val tracker = BleDiagnosticsTracker(timeProvider = { now })

        // when
        tracker.onConnected()
        now = 5_000L
        tracker.onUnexpectedDisconnect()
        tracker.onConnected()

        // then
        tracker.current.connectTimestampMs shouldBe 1_000L
        tracker.current.unexpectedDisconnectCount shouldBe 1
    }

    test("should track checksum pass and fail counts and reset cleanly") {
        // given
        val tracker = BleDiagnosticsTracker(timeProvider = { 1_000L })

        // when
        tracker.onConnected()
        tracker.onChecksumPass()
        tracker.onChecksumPass()
        tracker.onChecksumFail()

        // then
        tracker.current.checksumPassCount shouldBe 2
        tracker.current.checksumFailCount shouldBe 1

        // when
        tracker.reset()

        // then
        tracker.current.hasConnected shouldBe false
        tracker.current.unexpectedDisconnectCount shouldBe 0
        tracker.current.checksumPassCount shouldBe 0
        tracker.current.checksumFailCount shouldBe 0
    }

    test("should report checksum pass, mismatch, and malformed hex from ForumsladerParser while ignoring unchecksummed sentences") {
        // given
        val tracker = BleDiagnosticsTracker(timeProvider = { 1_000L })
        val parser = ForumsladerParser { passed ->
            if (passed) tracker.onChecksumPass() else tracker.onChecksumFail()
        }
        val body = "FL5,0x00,0,0,4000,4000,4000,0,0,250,0"
        val checksum = body.fold(0) { acc, c -> acc xor c.code }.toString(16).uppercase().padStart(2, '0')

        // when: valid checksum
        parser.processIncomingBytes($$"$$$body*$$checksum\r\n".toByteArray())
        // when: wrong checksum
        parser.processIncomingBytes($$"$$$body*00\r\n".toByteArray())
        // when: malformed non-hex checksum
        parser.processIncomingBytes($$"$$$body*ZZ\r\n".toByteArray())
        // when: unchecksummed sentence (excluded from denominator)
        parser.processIncomingBytes($$"$$$body\r\n".toByteArray())

        // then
        tracker.current.checksumPassCount shouldBe 1
        tracker.current.checksumFailCount shouldBe 2
    }

    test("should format monitoring uptime in human-readable units") {
        formatUptime(30_000L) shouldBe "< 1m"
        formatUptime(5 * 60_000L) shouldBe "5m"
        formatUptime(135 * 60_000L) shouldBe "2h 15m"
    }

    test("should update and reset singleton BleDiagnosticsStore") {
        // given
        BleDiagnosticsStore.reset()

        // when
        BleDiagnosticsStore.onConnected()
        BleDiagnosticsStore.onUnexpectedDisconnect()
        BleDiagnosticsStore.onChecksumPass()
        BleDiagnosticsStore.onChecksumFail()

        // then
        BleDiagnosticsStore.current.hasConnected shouldBe true
        BleDiagnosticsStore.current.unexpectedDisconnectCount shouldBe 1
        BleDiagnosticsStore.current.checksumPassCount shouldBe 1
        BleDiagnosticsStore.current.checksumFailCount shouldBe 1
        BleDiagnosticsStore.diagnosticsFlow.value shouldBe BleDiagnosticsStore.current

        // cleanup
        BleDiagnosticsStore.reset()
        BleDiagnosticsStore.current.hasConnected shouldBe false
    }
})
