package org.happycode.karoo.forumslader.extension

import android.content.Context
import android.util.TypedValue
import android.widget.RemoteViews
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.internal.ViewEmitter
import io.hammerhead.karooext.models.ViewConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.happycode.karoo.forumslader.R
import org.happycode.karoo.forumslader.adapters.ForumsladerDataFieldsAdapter.DataFieldId
import org.happycode.karoo.forumslader.application.diagnostics.BleDiagnosticsStore
import org.happycode.karoo.forumslader.domain.diagnostics.BleConnectionDiagnostics

class BleDiagnosticGraphicalDataType(
    extension: String,
    typeId: String = DataFieldId.BLE_DIAGNOSTIC,
    private val diagnosticsFlowProvider: () -> StateFlow<BleConnectionDiagnostics> = { BleDiagnosticsStore.diagnosticsFlow },
    private val timeProvider: () -> Long = System::currentTimeMillis,
) : DataTypeImpl(extension, typeId) {

    override fun startView(context: Context, config: ViewConfig, emitter: ViewEmitter) {
        if (config.preview) {
            emitter.updateView(createRemoteViews(context, config, BleConnectionDiagnostics.PREVIEW_TEXT))
            return
        }

        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        val flow = diagnosticsFlowProvider()

        scope.launch {
            flow.collect { diagnostics ->
                val text = diagnostics.formatCompact(timeProvider())
                emitter.updateView(createRemoteViews(context, config, text))
            }
        }

        scope.launch {
            while (isActive) {
                delay(REFRESH_INTERVAL_MS)
                val diagnostics = flow.value
                if (diagnostics.hasConnected) {
                    val text = diagnostics.formatCompact(timeProvider())
                    emitter.updateView(createRemoteViews(context, config, text))
                }
            }
        }

        emitter.setCancellable {
            scope.cancel()
        }
    }

    internal fun createRemoteViews(context: Context, config: ViewConfig, text: String): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.custom_data_field_ble_diagnostic)
        views.setTextViewText(R.id.ble_diagnostic_text, text)
        val textSizeSp = computeTextSizeSp(config)
        views.setTextViewTextSize(R.id.ble_diagnostic_text, TypedValue.COMPLEX_UNIT_SP, textSizeSp)
        return views
    }

    internal fun computeTextSizeSp(config: ViewConfig): Float {
        val baseSize = config.textSize.toFloat().coerceAtLeast(MIN_TEXT_SIZE_SP)
        val scaled = baseSize * COMPACT_TEXT_SCALE
        return scaled.coerceIn(MIN_TEXT_SIZE_SP, MAX_TEXT_SIZE_SP)
    }

    companion object {
        private const val REFRESH_INTERVAL_MS = 10_000L
        private const val COMPACT_TEXT_SCALE = 0.55f
        private const val MIN_TEXT_SIZE_SP = 12f
        private const val MAX_TEXT_SIZE_SP = 24f
    }
}
