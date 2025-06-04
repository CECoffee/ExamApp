package dev.coffee.examapp.ui.screens.exam

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ExamResultScreen(scoreString: String, onBack: () -> Unit) {
    val score = remember(scoreString) { scoreString.toDouble() }
    val message = when {
        score >= 90 -> "优秀！"
        score >= 80 -> "良好！"
        score >= 60 -> "及格！"
        else -> "再接再厉！"
    }

    val color = when {
        score >= 90 -> Color(0xFF4CAF50)
        score >= 80 -> Color(0xFF2196F3)
        score >= 60 -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "考试结束",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "您的最终成绩",
            fontSize = 24.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(180.dp)
                .border(8.dp, color, CircleShape)
        ) {
            Text(
                text = "${score.toInt()}分",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }

        Text(
            text = message,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(vertical = 24.dp)
        )

        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp, vertical = 16.dp)
        ) {
            Text("返回首页", style = TextStyle(fontSize = 18.sp))
        }
    }
}
