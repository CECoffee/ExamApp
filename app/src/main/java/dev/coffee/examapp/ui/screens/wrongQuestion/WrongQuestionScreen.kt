package dev.coffee.examapp.ui.screens.wrongQuestion

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.coffee.examapp.R
import dev.coffee.examapp.ui.components.LoadingIndicator
import dev.coffee.examapp.ui.components.QuestionCard
import dev.coffee.examapp.ui.components.WrongQuestionCard
import dev.coffee.examapp.ui.theme.ErrorColor
import dev.coffee.examapp.viewmodel.WrongQuestionViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

@Composable
fun WrongQuestionScreen(
    viewModel: WrongQuestionViewModel = viewModel()
) {
    val context = LocalContext.current
    val wrongQuestions by viewModel.wrongQuestions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val showDialog by viewModel.showQuestionDialog.collectAsState()
    val hasMore by viewModel.hasMore.collectAsState()
    val questionDetail by viewModel.questionDetail.collectAsState()
    val loadingDetailId by viewModel.loadingDetailId.collectAsState()

    LaunchedEffect(Unit) {
        // TEST
//        val testWrongQuestion = WrongQuestion(
//            questionId = 1,
//            content = "这是测试题目内容",
//            myAnswer = "B",
//            correctAnswer = "A"
//        )
//         viewModel.wrongQuestions.value = listOf(testWrongQuestion)

        viewModel.refresh()
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    if (showDialog && errorMessage == null) {
        Dialog(
            onDismissRequest = { viewModel.closeQuestionDialog() },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            QuestionCard(
                question = questionDetail,
                isLoading = false,
                userAnswer = "",
                onAnswerChanged = {},
                showExplanation = true,
                onSubmit = null
            )
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 顶部标题
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.wrong_questions_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }

        // 错误信息
//        errorMessage?.let { message ->
//            Text(
//                text = message,
//                color = MaterialTheme.colorScheme.error,
//                modifier = Modifier.padding(horizontal = 16.dp)
//            )
//        }

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
                items(
                    items = wrongQuestions,
                    key = { it.questionId }
                ) { question ->
                    SwipeToDeleteContainer(
                        onDelete = { viewModel.deleteWrongQuestion(question.questionId) },
                        deleteEnabled = true
                    ) {
                        WrongQuestionCard(
                            question = question,
                            isLoading = loadingDetailId == question.questionId,
                            onViewExplanation = { viewModel.viewDetail(question.questionId) }
                        )
                    }
                }

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
                    } else if (hasMore) {
                        Button(
                            onClick = { viewModel.loadNextPage() },
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

// 滑动删除容器组件
@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteContainer(
    onDelete: suspend () -> Unit,
    deleteEnabled: Boolean,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val context = LocalContext.current

    // TODO 滑动状态控制
    val swipeState = rememberSwipeableState(initialValue = SwipePosition.Closed)
    val swipeAnchors = remember(density) {
        mapOf(
            0f to SwipePosition.Closed,
            with(density) { -88.dp.toPx() } to SwipePosition.Open
        )
    }

    var deleteState by remember { mutableStateOf<DeleteState>(DeleteState.Idle) }
    val isDeleting = deleteState is DeleteState.Loading
    val coroutineScope = rememberCoroutineScope()

    val deleteCardBgColor = when {
        isDeleting -> ErrorColor.copy(0.3f)
        else -> ErrorColor.copy(0.8f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // 包含删除按钮
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .swipeable(
                    state = swipeState,
                    anchors = swipeAnchors,
                    thresholds = { _, _ -> FractionalThreshold(0.5f) },
                    orientation = Orientation.Horizontal
                )
                .offset { IntOffset(swipeState.offset.value.toInt(), 0) }
        ) {
            content()

            Card(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(80.dp)
                    .align(Alignment.CenterEnd)
                    .padding(vertical = 8.dp)
                    .offset(x = 80.dp) // 初始在屏幕外
                    .clickable(
                        enabled = deleteEnabled && !isDeleting,
                        onClick = {
                            deleteState = DeleteState.Loading
                            coroutineScope.launch {
                                try {
                                    withTimeout(5000) {
                                        onDelete()
                                    }
                                    deleteState = DeleteState.Success
                                } catch (e: Exception) {
                                    deleteState = DeleteState.Error(e.message ?: "删除失败")
                                }
                            }
                        }
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(deleteCardBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    when (deleteState) {
                        is DeleteState.Idle ->
                            Icon(Icons.Filled.RemoveCircle, "删除")

                        is DeleteState.Loading ->
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )

                        is DeleteState.Error ->
                            Icon(Icons.Default.Error, "失败")

                        is DeleteState.Success -> TODO()
                    }
                }
            }
        }

        // 处理删除结果
        LaunchedEffect(deleteState) {
            when (val state = deleteState) {
                is DeleteState.Error -> {
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                    delay(2000)
                    deleteState = DeleteState.Idle
                }
                is DeleteState.Success -> {
                    onDelete()
                    deleteState = DeleteState.Idle
                }
                else -> {}
            }
        }
    }
}

private enum class SwipePosition { Open, Closed }

private sealed class DeleteState {
    object Idle : DeleteState()
    object Loading : DeleteState()
    object Success : DeleteState()
    data class Error(val message: String) : DeleteState()
}