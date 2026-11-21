package org.happycode.karoo.forumslader.application.diagnostics

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.happycode.karoo.forumslader.domain.diagnostics.BleConnectionDiagnostics

class BleDiagnosticsTracker(
    private val timeProvider: () -> Long = System::currentTimeMillis,
) {
    private val _diagnosticsFlow = MutableStateFlow(BleConnectionDiagnostics())
    val diagnosticsFlow: StateFlow<BleConnectionDiagnostics> = _diagnosticsFlow.asStateFlow()

    val current: BleConnectionDiagnostics
        get() = _diagnosticsFlow.value

    fun onConnected() {
        val nowMs = timeProvider()
        _diagnosticsFlow.update { state ->
            if (state.connectTimestampMs == null) {
                state.copy(connectTimestampMs = nowMs)
            } else {
                state
            }
        }
    }

    fun onUnexpectedDisconnect() {
        _diagnosticsFlow.update { state ->
            state.copy(unexpectedDisconnectCount = state.unexpectedDisconnectCount + 1)
        }
    }

    fun onChecksumPass() {
        _diagnosticsFlow.update { state ->
            state.copy(checksumPassCount = state.checksumPassCount + 1)
        }
    }

    fun onChecksumFail() {
        _diagnosticsFlow.update { state ->
            state.copy(checksumFailCount = state.checksumFailCount + 1)
        }
    }

    fun reset(isConnected: Boolean = false) {
        _diagnosticsFlow.value = BleConnectionDiagnostics(
            connectTimestampMs = if (isConnected) timeProvider() else null
        )
    }
}

object BleDiagnosticsStore {
    private val tracker = BleDiagnosticsTracker()

    val diagnosticsFlow: StateFlow<BleConnectionDiagnostics>
        get() = tracker.diagnosticsFlow

    val current: BleConnectionDiagnostics
        get() = tracker.current

    fun onConnected() = tracker.onConnected()

    fun onUnexpectedDisconnect() = tracker.onUnexpectedDisconnect()

    fun onChecksumPass() = tracker.onChecksumPass()

    fun onChecksumFail() = tracker.onChecksumFail()

    fun reset(isConnected: Boolean = false) = tracker.reset(isConnected)
}
