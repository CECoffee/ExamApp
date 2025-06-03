package dev.coffee.examapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Rule
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Exam : Screen("exam", "考试", Icons.Outlined.Newspaper)
    object WrongQuestion : Screen("wrong_question", "错题", Icons.AutoMirrored.Outlined.Rule)
}