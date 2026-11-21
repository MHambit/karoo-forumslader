package org.happycode.karoo.forumslader.ui.main.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.happycode.karoo.forumslader.domain.ChargeState
import org.happycode.karoo.forumslader.domain.estimation.BatteryEstimate
import org.happycode.karoo.forumslader.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BatteryEstimateCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `should display measured pack capacity and cycle count when both are available`() {
        // given
        val estimate = BatteryEstimate(
            remainingCapacityPct = 80,
            avgDischargeRatePctPerKm = 1.2f,
            estimatedRangeKm = 65f,
            routeRemainingKm = 50f,
            isSufficientForRoute = true,
            chargeState = ChargeState.DISCHARGING,
            batteryCapacityWh = 33.3f,
            fullChargeCapacityMah = 3000,
            cycleCount = 42
        )

        // when
        composeTestRule.setContent {
            AppTheme {
                BatteryEstimateCard(estimate = estimate)
            }
        }

        // then
        composeTestRule.onNodeWithText("3000 mAh (33.3 Wh) · 42 cycles").assertIsDisplayed()
    }

    @Test
    fun `should display measured pack capacity without cycles when cycle count is unavailable`() {
        // given
        val estimate = BatteryEstimate(
            remainingCapacityPct = 75,
            avgDischargeRatePctPerKm = 1.0f,
            estimatedRangeKm = 75f,
            routeRemainingKm = null,
            isSufficientForRoute = null,
            chargeState = ChargeState.DISCHARGING,
            batteryCapacityWh = 28.86f,
            fullChargeCapacityMah = 2600,
            cycleCount = null
        )

        // when
        composeTestRule.setContent {
            AppTheme {
                BatteryEstimateCard(estimate = estimate)
            }
        }

        // then
        composeTestRule.onNodeWithText("2600 mAh (28.9 Wh)").assertIsDisplayed()
    }

    @Test
    fun `should display default pack capacity footer when BMS capacity is unavailable`() {
        // given
        val estimate = BatteryEstimate(
            remainingCapacityPct = 90,
            avgDischargeRatePctPerKm = 1.0f,
            estimatedRangeKm = 90f,
            routeRemainingKm = null,
            isSufficientForRoute = null,
            chargeState = ChargeState.DISCHARGING
        )

        // when
        composeTestRule.setContent {
            AppTheme {
                BatteryEstimateCard(estimate = estimate)
            }
        }

        // then
        composeTestRule.onNodeWithText("13.3 Wh (default)").assertIsDisplayed()
    }

    @Test
    fun `should display separate labeled charge level and estimated range sections`() {
        // given
        val estimate = BatteryEstimate(
            remainingCapacityPct = 80,
            avgDischargeRatePctPerKm = 1.2f,
            estimatedRangeKm = 65f,
            routeRemainingKm = 50f,
            isSufficientForRoute = true,
            chargeState = ChargeState.DISCHARGING
        )

        // when
        composeTestRule.setContent {
            AppTheme {
                BatteryEstimateCard(estimate = estimate)
            }
        }

        // then
        composeTestRule.onNodeWithText("Battery & Range").assertIsDisplayed()
        composeTestRule.onNodeWithText("Charge Level").assertIsDisplayed()
        composeTestRule.onNodeWithText("80%").assertIsDisplayed()
        composeTestRule.onNodeWithText("Estimated Range").assertIsDisplayed()
        composeTestRule.onNodeWithText("~65 km remaining").assertIsDisplayed()
        composeTestRule.onNodeWithText("1.2%/km").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sufficient for route").assertIsDisplayed()
    }

    @Test
    fun `should prefer live battery level over estimate capacity and show calculating when estimate is null`() {
        // when
        composeTestRule.setContent {
            AppTheme {
                BatteryEstimateCard(estimate = null, batteryLevel = 42.0)
            }
        }

        // then
        composeTestRule.onNodeWithText("Battery & Range").assertIsDisplayed()
        composeTestRule.onNodeWithText("Charge Level").assertIsDisplayed()
        composeTestRule.onNodeWithText("42%").assertIsDisplayed()
        composeTestRule.onNodeWithText("Estimated Range").assertIsDisplayed()
        composeTestRule.onNodeWithText("Calculating…").assertIsDisplayed()
    }

    @Test
    fun `should omit charge level section when neither battery level nor estimate is available`() {
        // when
        composeTestRule.setContent {
            AppTheme {
                BatteryEstimateCard(estimate = null, batteryLevel = null)
            }
        }

        // then
        composeTestRule.onNodeWithText("Charge Level").assertDoesNotExist()
        composeTestRule.onNodeWithText("Estimated Range").assertIsDisplayed()
        composeTestRule.onNodeWithText("Calculating…").assertIsDisplayed()
    }

    @Test
    fun `should display low charge warning and insufficient route banner`() {
        // given
        val estimate = BatteryEstimate(
            remainingCapacityPct = 15,
            avgDischargeRatePctPerKm = 2.0f,
            estimatedRangeKm = 8f,
            routeRemainingKm = 30f,
            isSufficientForRoute = false,
            chargeState = ChargeState.DISCHARGING
        )

        // when
        composeTestRule.setContent {
            AppTheme {
                BatteryEstimateCard(estimate = estimate)
            }
        }

        // then
        composeTestRule.onNodeWithText("15%").assertIsDisplayed()
        composeTestRule.onNodeWithText("~8 km remaining").assertIsDisplayed()
        composeTestRule.onNodeWithText("Battery may run out early").assertIsDisplayed()
    }

    @Test
    fun `should display charging status without discharge rate when charging`() {
        // given
        val estimate = BatteryEstimate(
            remainingCapacityPct = 60,
            avgDischargeRatePctPerKm = 1.5f,
            estimatedRangeKm = null,
            routeRemainingKm = null,
            isSufficientForRoute = null,
            chargeState = ChargeState.CHARGING
        )

        // when
        composeTestRule.setContent {
            AppTheme {
                BatteryEstimateCard(estimate = estimate)
            }
        }

        // then
        composeTestRule.onNodeWithText("60%").assertIsDisplayed()
        composeTestRule.onNodeWithText("Charging — range unlimited").assertIsDisplayed()
        composeTestRule.onNodeWithText("1.5%/km").assertDoesNotExist()
    }
}
