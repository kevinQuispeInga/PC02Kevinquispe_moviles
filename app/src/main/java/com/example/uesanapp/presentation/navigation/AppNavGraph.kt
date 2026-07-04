package com.example.uesanapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.uesanapp.presentation.auth.LoginScreen
import com.example.uesanapp.presentation.auth.RegisterScreen
import com.example.uesanapp.presentation.conversion.ConversionScreen
import com.example.uesanapp.presentation.history.HistoryScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavGraph(){
    val navController = rememberNavController()
    val startDestination = "login"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("register") { RegisterScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("home") {
            DrawerScaffold(navController) {
                ConversionScreen()
            }
        }
        composable("history") {
            DrawerScaffold(navController) {
                HistoryScreen()
            }
        }
    }
}