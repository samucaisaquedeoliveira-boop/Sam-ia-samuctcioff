package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PortalUiSmokeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `preview mode renders dashboard immediately and can switch to auth beta`() {
        composeRule.waitForIdle()
        // Preview Mode boots straight into the Home Dashboard
        composeRule.onNodeWithTag("home_tutorials_cta").assertIsDisplayed()
        composeRule.onNodeWithTag("topbar_preview_badge").assertIsDisplayed().performClick()

        // Clicking PREVIEW switches to AuthScreen where Beta & Preview options are visible
        composeRule.waitUntil(timeoutMillis = 5000) {
            runCatching {
                composeRule.onNodeWithTag("auth_beta_enter_button").assertExists()
                true
            }.getOrDefault(false)
        }
        composeRule.onNodeWithTag("auth_beta_enter_button")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 5000) {
            runCatching {
                composeRule.onNodeWithTag("home_tutorials_cta").assertExists()
                true
            }.getOrDefault(false)
        }
        composeRule.onNodeWithTag("home_tutorials_cta").assertIsDisplayed()
    }

    @Test
    fun `preview mode navigates across core modules without crashing`() {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("nav_sam_ia").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("nav_game_studio").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("nav_video_editor").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("topbar_tutorials_btn").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("topbar_menu_btn").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("drawer_dinosaurs").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("topbar_menu_btn").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("drawer_admin_panel").performScrollTo().performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun `updates center opens and sam admin mandatory release blocks app until contingency`() {
        composeRule.waitForIdle()
        // Open normal Updates Center (Menu 2)
        composeRule.onNodeWithTag("topbar_menu_btn").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("drawer_updates").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("updates_center_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("updates_check_now_btn").assertIsDisplayed()

        // Open SAM ADMIN and publish a mandatory 2.0.0 update
        composeRule.onNodeWithTag("topbar_menu_btn").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("drawer_admin_panel").performScrollTo().performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("admin_lazy_list")
            .performScrollToNode(hasTestTag("admin_save_release_btn"))
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("admin_save_release_btn")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()

        // Mandatory Update foreground blocker must automatically appear (Menu 1)
        composeRule.waitUntil(timeoutMillis = 5000) {
            runCatching {
                composeRule.onNodeWithTag("mandatory_update_screen").assertExists()
                true
            }.getOrDefault(false)
        }
        composeRule.onNodeWithTag("mandatory_update_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("mandatory_update_now_btn").performScrollTo().assertIsDisplayed().performClick()

        // Real update channel unavailable in Robolectric -> shows failure & contingency recovery (never fakes 100%)
        composeRule.waitUntil(timeoutMillis = 8000) {
            runCatching {
                composeRule.onNodeWithTag("mandatory_update_contingency_btn").assertExists()
                true
            }.getOrDefault(false)
        }
        composeRule.onNodeWithTag("mandatory_update_contingency_btn").performScrollTo().performClick()
        composeRule.waitUntil(timeoutMillis = 5000) {
            runCatching {
                composeRule.onNodeWithTag("topbar_menu_btn").assertExists()
                true
            }.getOrDefault(false)
        }
    }

    @Test
    fun `publication screen opens and exposes apk share and export controls`() {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("topbar_menu_btn").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("drawer_publication").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("publication_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("publication_share_apk_btn").performScrollTo().assertIsDisplayed()
    }
}
