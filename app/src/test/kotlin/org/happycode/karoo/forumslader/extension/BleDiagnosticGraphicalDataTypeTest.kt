package org.happycode.karoo.forumslader.extension

import android.content.Context
import android.widget.RemoteViews
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import io.hammerhead.karooext.internal.ViewEmitter
import io.hammerhead.karooext.models.ViewConfig
import kotlinx.coroutines.flow.MutableStateFlow
import org.happycode.karoo.forumslader.R
import org.happycode.karoo.forumslader.domain.diagnostics.BleConnectionDiagnostics
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BleDiagnosticGraphicalDataTypeTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `should render preview text when preview mode is enabled`() {
        // given
        val dataType = BleDiagnosticGraphicalDataType("karoo-forumslader")
        val emitter = mock<ViewEmitter>()
        val config = ViewConfig(
            alignment = ViewConfig.Alignment.CENTER,
            textSize = 32,
            gridSize = Pair(60, 15),
            viewSize = Pair(480, 120),
            preview = true,
        )

        // when
        dataType.startView(context, config, emitter)

        // then
        val captor = argumentCaptor<RemoteViews>()
        verify(emitter).updateView(captor.capture())
        val inflated = captor.firstValue.apply(context, null)
        val textView = inflated.findViewById<TextView>(R.id.ble_diagnostic_text)
        assertEquals(BleConnectionDiagnostics.PREVIEW_TEXT, textView.text.toString())
    }

    @Test
    fun `should render compact diagnostics string for live state`() {
        // given
        val flow = MutableStateFlow(
            BleConnectionDiagnostics(
                unexpectedDisconnectCount = 1,
                connectTimestampMs = 0L,
                checksumPassCount = 99,
                checksumFailCount = 1,
            )
        )
        val dataType = BleDiagnosticGraphicalDataType(
            extension = "karoo-forumslader",
            diagnosticsFlowProvider = { flow },
            timeProvider = { 4 * 3_600_000L },
        )
        val config = ViewConfig(
            alignment = ViewConfig.Alignment.CENTER,
            textSize = 32,
            gridSize = Pair(60, 15),
            viewSize = Pair(480, 120),
            preview = false,
        )

        // when
        val views = dataType.createRemoteViews(
            context,
            config,
            flow.value.formatCompact(4 * 3_600_000L)
        )

        // then
        val inflated = views.apply(context, null)
        val textView = inflated.findViewById<TextView>(R.id.ble_diagnostic_text)
        assertEquals("S:75% Q:99% D:0.3/h", textView.text.toString())
    }

    @Test
    fun `should emit updates from flow in live startView and cancel cleanly`() {
        // given
        val flow = MutableStateFlow(BleConnectionDiagnostics())
        val dataType = BleDiagnosticGraphicalDataType(
            extension = "karoo-forumslader",
            diagnosticsFlowProvider = { flow },
            timeProvider = { 4 * 3_600_000L },
        )
        val emitter = mock<ViewEmitter>()
        val cancellableCaptor = argumentCaptor<() -> Unit>()
        val config = ViewConfig(
            alignment = ViewConfig.Alignment.CENTER,
            textSize = 32,
            gridSize = Pair(60, 15),
            viewSize = Pair(480, 120),
            preview = false,
        )

        // when
        dataType.startView(context, config, emitter)

        // then
        verify(emitter).setCancellable(cancellableCaptor.capture())
        cancellableCaptor.firstValue.invoke()
    }
}
