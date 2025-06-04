package dev.coffee.examapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import dev.coffee.examapp.ui.screens.exam.ExamListScreen
import dev.coffee.examapp.ui.screens.exam.ExamResultScreen
import dev.coffee.examapp.ui.screens.exam.ExamScreen
import dev.coffee.examapp.ui.screens.practice.PracticeListScreen
import dev.coffee.examapp.ui.screens.practice.PracticeScreen
import dev.coffee.examapp.ui.screens.wrongQuestion.WrongQuestionScreen
import java.net.URLDecoder

@Composable
fun NavigationHost(navController: NavHostController,
                   modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.ExamList.route,
        modifier = modifier
    ) {
        composable(Screen.Practice.route) {
            PracticeListScreen(navController)
        }
        composable(Screen.ExamList.route) {
            ExamListScreen(navController)
        }
        composable(Screen.WrongQuestion.route) {
            WrongQuestionScreen(navController)
        }
        composable(Screen.Exam.route + "/{examId}/{duration}/{questionIds}",
            arguments = listOf(
                navArgument("examId") { type = NavType.IntType },
                navArgument("duration") { type = NavType.IntType },
                navArgument("questionIds") { type = NavType.StringType }
            )
        ) {
            backStackEntry ->
            val examId = backStackEntry.arguments?.getInt("examId") ?: 0
            val duration = backStackEntry.arguments?.getInt("duration") ?: 0
            val questionIds = backStackEntry.arguments?.getString("questionIds") ?: ""

            ExamScreen(
                examId = examId,
                duration = duration,
                questionIdStrings = questionIds,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.ExamResult.route + "/{score}",
            arguments = listOf( navArgument("score") { type = NavType.StringType } )
        ) {
            backStackEntry ->
            val score = backStackEntry.arguments?.getString("score") ?: ""

            ExamResultScreen(
                scoreString = score,
                onBack = { navController.popBackStack() }
            )
        }

        // In your navigation graph:
        composable("practice/{chapterId}/{chapterName}") { backStackEntry ->
            val chapterId = backStackEntry.arguments?.getString("chapterId")?.toIntOrNull() ?: 0
            val chapterName = URLDecoder.decode(
                backStackEntry.arguments?.getString("chapterName") ?: "",
                "utf-8"
            )
            PracticeScreen(
                chapterId = chapterId,
                chapterName = chapterName,
                onBack = { navController.popBackStack() }
            )
        }



    }
}

