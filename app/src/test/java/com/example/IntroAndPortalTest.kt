package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.example.data.local.SystemSettingsEntity
import com.example.ui.screens.IntroSplashScreen
import com.example.ui.screens.PublicPortalScreen
import com.example.ui.theme.EFinanceRwandaTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class IntroAndPortalTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `public portal omits How It Works before login`() {
        composeRule.setContent {
            EFinanceRwandaTheme {
                PublicPortalScreen(
                    settings = SystemSettingsEntity(),
                    advertisements = emptyList(),
                    onNavigateLogin = {},
                    onNavigateRegister = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("How It Works").assertDoesNotExist()
        composeRule.onNodeWithTag("public_login_button").assertIsDisplayed()
        composeRule.onNodeWithTag("public_register_button").assertIsDisplayed()
    }

    @Test
    fun `intro splash starts with multi-color logo phase`() {
        composeRule.setContent {
            EFinanceRwandaTheme {
                IntroSplashScreen(onFinished = {})
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("intro_splash_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("intro_logo_phase").assertIsDisplayed()
        assertTrue(true)
    }
}
