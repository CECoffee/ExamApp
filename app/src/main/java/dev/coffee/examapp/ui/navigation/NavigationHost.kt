package dev.coffee.examapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import dev.coffee.examapp.ui.screens.ServerConfigScreen
import dev.coffee.examapp.ui.screens.exam.*
import dev.coffee.examapp.ui.screens.practice.*
import dev.coffee.examapp.ui.screens.wrongQuestion.*
import dev.coffee.examapp.ui.screens.statistic.*
import java.net.URLDecoder

@Composable
fun NavigationHost(navController: NavHostController,
                   modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.ServerConfig.route,
        modifier = modifier
    ) {
        composable(Screen.ServerConfig.route) {
            ServerConfigScreen(navController)
        }
        composable(Screen.Practice.route) {
            PracticeListScreen(navController)
        }
        composable(Screen.ExamList.route) {
            ExamListScreen(navController)
        }
        composable(Screen.WrongQuestion.route) {
            WrongQuestionScreen(navController)
        }
        composable(Screen.Statistic.route) {
            StatisticScreen(navController)
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

        composable("practice/{chapterId}/{chapterName}") { backStackEntry ->
            val chapterId = URLDecoder.decode(
                backStackEntry.arguments?.getString("chapterId") ?: "",
                "utf-8"
            )
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

