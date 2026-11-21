package org.happycode.karoo.forumslader.ui.main.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.happycode.karoo.forumslader.R
import org.happycode.karoo.forumslader.domain.ChargeState
import org.happycode.karoo.forumslader.domain.estimation.BatteryEstimate
import org.happycode.karoo.forumslader.theme.forumsladerColors
import kotlin.math.roundToInt

private const val GOOD_CHARGE_THRESHOLD_PCT = 50
private const val WARNING_CHARGE_THRESHOLD_PCT = 20

@Composable
fun BatteryEstimateCard(
    estimate: BatteryEstimate?,
    modifier: Modifier = Modifier,
    batteryLevel: Double? = null
) {
    val level = batteryLevel?.toInt() ?: estimate?.remainingCapacityPct

    Card(
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            BatteryCardHeader()

            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            level?.let {
                BatteryChargeSection(
                    levelPct = it,
                    chargeState = estimate?.chargeState
                )
            }

            BatteryRangeSection(estimate = estimate)
        }
    }
}

@Composable
private fun BatteryCardHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.BatteryChargingFull,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(id = R.string.battery_range_estimate_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun BatteryChargeSection(
    levelPct: Int,
    chargeState: ChargeState?
) {
    val indicatorColor = chargeIndicatorColor(levelPct, chargeState)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.battery_charge_level_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$levelPct%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = indicatorColor
            )
        }

        LinearProgressIndicator(
            progress = { (levelPct / 100f).coerceIn(0f, 1f) },
            color = indicatorColor,
            trackColor = MaterialTheme.colorScheme.surface,
            gapSize = 0.dp,
            drawStopIndicator = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        )
    }
}

@Composable
private fun chargeIndicatorColor(levelPct: Int, chargeState: ChargeState?): Color = when {
    chargeState == ChargeState.CHARGING || chargeState == ChargeState.FULL ->
        MaterialTheme.forumsladerColors.statusGood
    levelPct >= GOOD_CHARGE_THRESHOLD_PCT -> MaterialTheme.forumsladerColors.statusGood
    levelPct >= WARNING_CHARGE_THRESHOLD_PCT -> MaterialTheme.forumsladerColors.statusWarning
    else -> MaterialTheme.forumsladerColors.statusDanger
}

@Composable
private fun BatteryRangeSection(estimate: BatteryEstimate?) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(R.string.battery_estimated_range_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        estimate?.let {
            BatteryEstimateContent(it)
        } ?: Text(
            text = stringResource(id = R.string.battery_range_calculating),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BatteryEstimateContent(estimate: BatteryEstimate) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = rangeStatusText(estimate),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f, fill = false)
        )

        if (shouldShowDischargeRate(estimate)) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.battery_range_rate, estimate.avgDischargeRatePctPerKm),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    estimate.isSufficientForRoute?.let { isSufficient ->
        RouteSufficiencyBanner(isSufficient = isSufficient)
    }

    BatteryPackHealthFooter(estimate = estimate)
}

@Composable
private fun rangeStatusText(estimate: BatteryEstimate): String = when (estimate.chargeState) {
    ChargeState.CHARGING,
    ChargeState.FULL -> stringResource(R.string.battery_range_charging)
    ChargeState.STANDBY -> estimate.estimatedRangeKm?.let {
        stringResource(R.string.battery_range_remaining, it.roundToInt())
    } ?: stringResource(R.string.battery_range_standby)
    ChargeState.DISCHARGING -> estimate.estimatedRangeKm?.let {
        stringResource(R.string.battery_range_remaining, it.roundToInt())
    } ?: stringResource(R.string.battery_range_not_enough_data)
}

private fun shouldShowDischargeRate(estimate: BatteryEstimate): Boolean =
    (estimate.chargeState == ChargeState.DISCHARGING || estimate.chargeState == ChargeState.STANDBY) &&
        estimate.avgDischargeRatePctPerKm > 0f

@Composable
private fun RouteSufficiencyBanner(isSufficient: Boolean) {
    val containerColor = if (isSufficient) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
    }
    val contentColor = if (isSufficient) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }
    val icon = if (isSufficient) Icons.Default.CheckCircle else Icons.Default.Warning

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp)
            .background(containerColor, shape = MaterialTheme.shapes.small)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = stringResource(
                if (isSufficient) R.string.battery_range_sufficient
                else R.string.battery_range_insufficient
            ),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = contentColor,
            maxLines = 1
        )
    }
}

@Composable
private fun BatteryPackHealthFooter(estimate: BatteryEstimate) {
    val capacityMah = estimate.fullChargeCapacityMah
    val cycleCount = estimate.cycleCount
    val summaryText = when {
        capacityMah != null && cycleCount != null -> stringResource(
            R.string.battery_pack_capacity_with_cycles,
            capacityMah,
            estimate.batteryCapacityWh,
            cycleCount
        )
        capacityMah != null -> stringResource(
            R.string.battery_pack_capacity_measured,
            capacityMah,
            estimate.batteryCapacityWh
        )
        else -> stringResource(
            R.string.battery_pack_capacity_default,
            estimate.batteryCapacityWh
        )
    }

    Text(
        text = summaryText,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
