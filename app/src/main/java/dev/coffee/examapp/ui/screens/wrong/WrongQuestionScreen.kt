package dev.coffee.examapp.ui.screens.wrong

import dev.coffee.examapp.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.coffee.examapp.ui.components.LoadingIndicator
import dev.coffee.examapp.ui.components.WrongQuestionCard
import dev.coffee.examapp.viewmodel.WrongQuestionViewModel

@Composable
fun WrongQuestionScreen(
    viewModel: WrongQuestionViewModel = viewModel()
) {
    // 从ViewModel获取数据
    val wrongQuestions by viewModel.wrongQuestions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // 初始化加载数据
    LaunchedEffect(Unit) {
        // 在实际应用中，这里应该传递真实的token
        viewModel.refresh("fake_token")
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 顶部标题
        Text(
            text = stringResource(R.string.wrong_questions_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterHorizontally)
        )

        // 错误信息
        errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // 加载状态
        if (isLoading && wrongQuestions.isEmpty()) {
            LoadingIndicator()
        } else if (wrongQuestions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无错题记录",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        } else {
            // 错题列表
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(wrongQuestions) { question ->
                    WrongQuestionCard(
                        question = question,
                        onViewExplanation = { /* 处理查看解析逻辑 */ },
                        onToggleBookmark = { /* 处理收藏逻辑 */ }
                    )
                }

                // 加载更多
                item {
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    } else if (wrongQuestions.isNotEmpty()) {
                        Button(
                            onClick = { viewModel.loadNextPage("fake_token") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text("加载更多")
                        }
                    }
                }
            }
        }
    }
}