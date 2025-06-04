package dev.coffee.examapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Rule
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object ExamList : Screen("exam_list", "考试", Icons.Rounded.Alarm)
    object WrongQuestion : Screen("wrong_question", "错题", Icons.AutoMirrored.Rounded.Rule)
    object Practice : Screen("practice", "练习", Icons.Rounded.EditNote)
    object Exam : Screen("exam", "考试", Icons.Rounded.Alarm)
    object ExamResult : Screen("exam_result", "考试结果", Icons.Rounded.Search)
}