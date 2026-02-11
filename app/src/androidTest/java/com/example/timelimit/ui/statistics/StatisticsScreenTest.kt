package com.example.timelimit.ui.statistics

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.timelimit.ui.theme.TimeLimitTheme
import org.junit.Rule
import org.junit.Test

class StatisticsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun emptyStateView_displaysWhenNoData() {
        // When
        composeTestRule.setContent {
            TimeLimitTheme {
                EmptyStateView()
            }
        }

        // Then
        composeTestRule.onNodeWithText("No usage data yet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Start using apps to see statistics").assertIsDisplayed()
    }

    @Test
    fun usageStatCard_displaysAppInformation() {
        // Given
        val stat = UsageStatItem(
            appName = "Test App",
            packageName = "com.test.app",
            usageTimeMillis = 300000L, // 5 minutes
            icon = null
        )

        // When
        composeTestRule.setContent {
            TimeLimitTheme {
                UsageStatCard(stat)
            }
        }

        // Then
        composeTestRule.onNodeWithText("Test App").assertIsDisplayed()
        composeTestRule.onNodeWithText("com.test.app").assertIsDisplayed()
        composeTestRule.onNodeWithText("5m").assertIsDisplayed()
    }

    @Test
    fun totalTimeCard_displaysFormattedTime() {
        // When
        composeTestRule.setContent {
            TimeLimitTheme {
                TotalTimeCard(totalTime = 7260000L, period = "Today") // 2h 1m
            }
        }

        // Then
        composeTestRule.onNodeWithText("Total Screen Time").assertIsDisplayed()
        composeTestRule.onNodeWithText("2h 1m").assertIsDisplayed()
        composeTestRule.onNodeWithText("Today").assertIsDisplayed()
    }

    @Test
    fun totalTimeCard_handlesMinutesOnly() {
        // When
        composeTestRule.setContent {
            TimeLimitTheme {
                TotalTimeCard(totalTime = 300000L, period = "Today") // 5 minutes
            }
        }

        // Then
        composeTestRule.onNodeWithText("5m").assertIsDisplayed()
    }

    @Test
    fun totalTimeCard_handlesLessThanOneMinute() {
        // When
        composeTestRule.setContent {
            TimeLimitTheme {
                TotalTimeCard(totalTime = 30000L, period = "Today") // 30 seconds
            }
        }

        // Then
        composeTestRule.onNodeWithText("< 1m").assertIsDisplayed()
    }
}

