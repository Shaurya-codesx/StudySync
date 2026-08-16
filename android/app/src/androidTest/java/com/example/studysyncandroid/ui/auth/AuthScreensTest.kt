package com.example.studysyncandroid.ui.auth

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AuthScreensTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginScreen_initialState_elementsAreVisibleAndButtonDisabled() {
        val mockViewModel = mock<AuthViewModel>()
        whenever(mockViewModel.loginState).thenReturn(MutableStateFlow(AuthUiState.Idle))

        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = {},
                onNavigateToSignup = {},
                onNavigateToForgotPassword = {},
                viewModel = mockViewModel
            )
        }

        // Verify elements exist
        composeTestRule.onNodeWithText("Welcome back.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()

        // Verify button is disabled initially (because fields are empty)
        composeTestRule.onNodeWithText("Log In").assertIsNotEnabled()
    }

    @Test
    fun loginScreen_enteringText_enablesButtonAndTriggersViewModel() {
        val mockViewModel = mock<AuthViewModel>()
        whenever(mockViewModel.loginState).thenReturn(MutableStateFlow(AuthUiState.Idle))

        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = {},
                onNavigateToSignup = {},
                onNavigateToForgotPassword = {},
                viewModel = mockViewModel
            )
        }

        // Enter text
        composeTestRule.onNodeWithText("Email").performTextInput("test@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password123")

        // Button should be enabled now
        composeTestRule.onNodeWithText("Log In").assertIsEnabled()

        // Click the button
        composeTestRule.onNodeWithText("Log In").performClick()

        // Verify viewModel.login was called
        verify(mockViewModel).login("test@example.com", "password123")
    }

    @Test
    fun loginScreen_loadingState_showsCircularProgressIndicator() {
        val mockViewModel = mock<AuthViewModel>()
        whenever(mockViewModel.loginState).thenReturn(MutableStateFlow(AuthUiState.Loading))

        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = {},
                onNavigateToSignup = {},
                onNavigateToForgotPassword = {},
                viewModel = mockViewModel
            )
        }

        // The "Log In" text is hidden during loading, so the CircularProgressIndicator is shown instead.
        composeTestRule.onNodeWithText("Log In").assertDoesNotExist()
    }

    @Test
    fun signupScreen_initialState_elementsAreVisible() {
        val mockViewModel = mock<AuthViewModel>()
        whenever(mockViewModel.signupState).thenReturn(MutableStateFlow(AuthUiState.Idle))

        composeTestRule.setContent {
            SignupScreen(
                onSignupSuccess = {},
                onNavigateToLogin = {},
                viewModel = mockViewModel
            )
        }

        composeTestRule.onNodeWithText("Create an account.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Display Name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign Up").assertIsNotEnabled()
    }
}
