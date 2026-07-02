package com.example.plant_care_app.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.plant_care_app.ui.theme.PlantCareAppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginRegisterScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginShowsRequiredFieldsErrorWhenSubmittingEmptyForm() {
        setLoginScreen()

        composeTestRule
            .onNodeWithText("Iniciar Sesión")
            .performScrollTo()
            .performClick()

        composeTestRule
            .onNodeWithText("Por favor, completa todos los campos")
            .assertIsDisplayed()
    }

    @Test
    fun loginNavigatesToRegisterScreenWhenSignUpIsClicked() {
        setAuthNavHost()

        composeTestRule
            .onNodeWithText("Regístrate")
            .performScrollTo()
            .performClick()

        composeTestRule
            .onNodeWithText("Únete a nuestra comunidad plantover")
            .assertIsDisplayed()
    }

    @Test
    fun registerShowsRequiredFieldsErrorWhenSubmittingEmptyForm() {
        setRegisterScreen()

        composeTestRule
            .onNode(hasText("Crear Cuenta") and hasClickAction())
            .performScrollTo()
            .performClick()

        composeTestRule
            .onNodeWithText("Por favor, completa todos los campos")
            .assertIsDisplayed()
    }

    @Test
    fun registerShowsPasswordMismatchErrorWhenPasswordsDoNotMatch() {
        setRegisterScreen()

        composeTestRule.onAllNodes(hasSetTextAction())[0].performTextInput("Mora")
        composeTestRule.onAllNodes(hasSetTextAction())[1].performTextInput("mora@example.com")
        composeTestRule.onAllNodes(hasSetTextAction())[2].performTextInput("password-1")
        composeTestRule.onAllNodes(hasSetTextAction())[3].performTextInput("password-2")

        composeTestRule
            .onNode(hasText("Crear Cuenta") and hasClickAction())
            .performScrollTo()
            .performClick()

        composeTestRule
            .onNodeWithText("Las contraseñas no coinciden")
            .assertIsDisplayed()
    }

    @Test
    fun registerBackToLoginPopsBackToLoginScreen() {
        setAuthNavHost()

        composeTestRule
            .onNodeWithText("Regístrate")
            .performScrollTo()
            .performClick()
        composeTestRule
            .onNodeWithText("Inicia Sesión")
            .performScrollTo()
            .performClick()

        composeTestRule
            .onNodeWithText("Plant Care")
            .assertIsDisplayed()
    }

    private fun setLoginScreen() {
        composeTestRule.setContent {
            val navController = rememberNavController()

            PlantCareAppTheme {
                LoginScreen(navController = navController)
            }
        }
    }

    private fun setRegisterScreen() {
        composeTestRule.setContent {
            val navController = rememberNavController()

            PlantCareAppTheme {
                RegisterScreen(navController = navController)
            }
        }
    }

    private fun setAuthNavHost() {
        composeTestRule.setContent {
            val navController = rememberNavController()

            PlantCareAppTheme {
                NavHost(navController = navController, startDestination = "login") {
                    composable("login") {
                        LoginScreen(navController = navController)
                    }
                    composable("register") {
                        RegisterScreen(navController = navController)
                    }
                }
            }
        }
    }
}
