package dev.coffee.examapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dev.coffee.examapp.ui.screens.exam.ExamScreen
import dev.coffee.examapp.ui.screens.wrong.WrongQuestionScreen

@Composable
fun NavigationHost(navController: NavHostController,
                   modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.Exam.route,
        modifier = modifier
    ) {
        composable(Screen.Exam.route) {
            ExamScreen()
        }
        composable(Screen.WrongQuestion.route) {
            WrongQuestionScreen()
        }
    }
}