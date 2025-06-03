package dev.coffee.examapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Rule
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Exam : Screen("exam", "考试", Icons.Rounded.Alarm)
    object WrongQuestion : Screen("wrong_question", "错题", Icons.AutoMirrored.Rounded.Rule)
}