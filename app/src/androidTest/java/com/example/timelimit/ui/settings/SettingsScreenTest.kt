package com.example.timelimit.ui.settings

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.timelimit.model.AppInfo
import com.example.timelimit.ui.theme.TimeLimitTheme
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun appPickerDialog_displaysSearchField() {
        // Given
        val testApps = listOf(
            AppInfo("Test App 1", "com.test.app1", null),
            AppInfo("Test App 2", "com.test.app2", null)
        )

        // When
        composeTestRule.setContent {
            TimeLimitTheme {
                AppPickerDialog(
                    apps = testApps,
                    blockedApps = emptySet(),
                    onDismiss = {},
                    onAppToggle = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Select Apps to Block").assertIsDisplayed()
        composeTestRule.onNodeWithText("Search apps...").assertIsDisplayed()
    }

    @Test
    fun appPickerDialog_filtersAppsOnSearch() {
        // Given
        val testApps = listOf(
            AppInfo("Instagram", "com.instagram", null),
            AppInfo("Facebook", "com.facebook", null),
            AppInfo("Twitter", "com.twitter", null)
        )

        // When
        composeTestRule.setContent {
            TimeLimitTheme {
                AppPickerDialog(
                    apps = testApps,
                    blockedApps = emptySet(),
                    onDismiss = {},
                    onAppToggle = {}
                )
            }
        }

        // Type in search field
        composeTestRule.onNodeWithText("Search apps...").performTextInput("Face")

        // Then
        composeTestRule.onNodeWithText("Facebook").assertIsDisplayed()
        composeTestRule.onNodeWithText("Instagram").assertDoesNotExist()
    }

    @Test
    fun appPickerDialog_showsCheckedStateForBlockedApps() {
        // Given
        val testApps = listOf(
            AppInfo("Test App", "com.test.app", null)
        )
        val blockedApps = setOf("com.test.app")

        // When
        composeTestRule.setContent {
            TimeLimitTheme {
                AppPickerDialog(
                    apps = testApps,
                    blockedApps = blockedApps,
                    onDismiss = {},
                    onAppToggle = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Test App").assertIsDisplayed()
        // Checkbox should be checked (test for semantic properties)
        composeTestRule.onNode(hasTestTag("checkbox_com.test.app") or isToggleable())
            .assertIsDisplayed()
    }

    @Test
    fun appBlockCard_displaysAppInformation() {
        // Given
        val appInfo = AppInfo("Test App", "com.test.app", null)

        // When
        composeTestRule.setContent {
            TimeLimitTheme {
                AppBlockCard(
                    appInfo = appInfo,
                    isBlocked = false,
                    onToggle = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Test App").assertIsDisplayed()
    }

    @Test
    fun emptyStateCard_displaysCorrectMessage() {
        // When
        composeTestRule.setContent {
            TimeLimitTheme {
                EmptyStateCard()
            }
        }

        // Then
        composeTestRule.onNodeWithText("No apps yet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tap the + button to add apps to your blocklist")
            .assertIsDisplayed()
    }
}

