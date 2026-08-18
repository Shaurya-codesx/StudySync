package com.example.studysyncandroid.ui.auth

import com.example.studysyncandroid.data.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: AuthViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mock()
        viewModel = AuthViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial states are Idle`() {
        assertTrue(viewModel.loginState.value is AuthUiState.Idle)
        assertTrue(viewModel.signupState.value is AuthUiState.Idle)
        assertTrue(viewModel.verifyEmailState.value is AuthUiState.Idle)
    }

    @Test
    fun `login success sets Success state`() = runBlocking {
        whenever(authRepository.login("test@test.com", "password")).thenReturn(Result.success(Unit))

        viewModel.login("test@test.com", "password")

        val state = viewModel.loginState.value
        assertTrue("Expected Success, got $state", state is AuthUiState.Success)
    }

    @Test
    fun `login failure sets Error state`() = runBlocking {
        whenever(authRepository.login("test@test.com", "wrong_pass")).thenReturn(Result.failure(Exception("Invalid credentials")))

        viewModel.login("test@test.com", "wrong_pass")

        val state = viewModel.loginState.value
        assertTrue("Expected Error, got $state", state is AuthUiState.Error)
        assertEquals("An unexpected error occurred. Please try again.", (state as AuthUiState.Error).message)
    }

    @Test
    fun `signup success sets RequiresVerification state`() = runBlocking {
        whenever(authRepository.signup("test@test.com", "password", "Test User")).thenReturn(Result.success(Unit))

        viewModel.signup("test@test.com", "password", "Test User")

        val state = viewModel.signupState.value
        assertTrue("Expected RequiresVerification, got $state", state is AuthUiState.RequiresVerification)
    }

    @Test
    fun `signup failure sets Error state`() = runBlocking {
        whenever(authRepository.signup("bad_email", "password", "Test User")).thenReturn(Result.failure(Exception("Bad email format")))

        viewModel.signup("bad_email", "password", "Test User")

        val state = viewModel.signupState.value
        assertTrue("Expected Error, got $state", state is AuthUiState.Error)
    }

    @Test
    fun `verifyEmail success sets Success state`() = runBlocking {
        whenever(authRepository.verifyEmail("test@test.com", "123456")).thenReturn(Result.success(Unit))

        viewModel.verifyEmail("test@test.com", "123456")

        val state = viewModel.verifyEmailState.value
        assertTrue("Expected Success, got $state", state is AuthUiState.Success)
    }

    @Test
    fun `logout calls repository and triggers callback`() = runBlocking {
        var callbackTriggered = false
        whenever(authRepository.logout()).thenReturn(Unit)

        viewModel.logout {
            callbackTriggered = true
        }

        verify(authRepository).logout()
        assertTrue(callbackTriggered)
    }
}
