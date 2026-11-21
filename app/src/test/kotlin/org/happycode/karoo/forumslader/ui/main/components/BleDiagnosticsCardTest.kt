package org.happycode.karoo.forumslader.ui.main.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.happycode.karoo.forumslader.domain.diagnostics.BleConnectionDiagnostics
import org.happycode.karoo.forumslader.theme.AppTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BleDiagnosticsCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `should display not connected state before first connection`() {
        composeTestRule.setContent {
            AppTheme {
                BleDiagnosticsCard(
                    diagnostics = BleConnectionDiagnostics(),
                    onReset = {},
                    nowMs = 10_000L,
                )
            }
        }

        composeTestRule.onNodeWithText("BLE Stability").assertIsDisplayed()
        composeTestRule.onNodeWithText("Not connected yet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reset Diagnostics").assertIsDisplayed()
    }

    @Test
    fun `should display live diagnostics metrics and invoke reset callback`() {
        var resetClicked = false
        val diagnostics = BleConnectionDiagnostics(
            unexpectedDisconnectCount = 1,
            connectTimestampMs = 0L,
            checksumPassCount = 198,
            checksumFailCount = 2,
        )

        composeTestRule.setContent {
            AppTheme {
                BleDiagnosticsCard(
                    diagnostics = diagnostics,
                    onReset = { resetClicked = true },
                    nowMs = 4 * 3_600_000L,
                )
            }
        }

        composeTestRule.onNodeWithText("75%").assertIsDisplayed()
        composeTestRule.onNodeWithText("99%").assertIsDisplayed()
        composeTestRule.onNodeWithText("0.3/h").assertIsDisplayed()
        composeTestRule.onNodeWithText("1").assertIsDisplayed()
        composeTestRule.onNodeWithText("4h 0m").assertIsDisplayed()

        composeTestRule.onNodeWithText("Reset Diagnostics").performClick()
        assertTrue(resetClicked)
    }
}
