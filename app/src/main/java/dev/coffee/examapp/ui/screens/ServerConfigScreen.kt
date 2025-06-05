package dev.coffee.examapp.ui.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.coffee.examapp.R
import dev.coffee.examapp.network.RetrofitClient.setBaseUrl
import dev.coffee.examapp.ui.navigation.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerConfigScreen(navController: NavController) {
    var serverAddress by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 顶部Logo和标题
        Spacer(modifier = Modifier.weight(0.3f))
        Image(
            painter = painterResource(id = R.mipmap.ic_launcher), // 替换为您的logo资源
            contentDescription = "App Logo",
            modifier = Modifier.size(80.dp)
        )
        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 16.dp)
        )

        Spacer(modifier = Modifier.weight(0.2f))

        // 胶囊形输入框
        TextField(
            value = serverAddress,
            enabled = !isLoading,

            onValueChange = { serverAddress = it },
            placeholder = { Text(text = "服务器地址", color = MaterialTheme.colorScheme.primary.copy(0.5f)) },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50.dp)),
            singleLine = false,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f),
                cursorColor = MaterialTheme.colorScheme.primary.copy(0.8f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(40.dp))

        // 圆形按钮
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(72.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 3.dp
                )
            } else {
                Button(
                    onClick = {
                        if (serverAddress.isNotBlank()) {
                            isLoading = true

                            coroutineScope.launch {
                                val result = setBaseUrl(serverAddress)
                                isLoading = false
                                if (result) {
                                    navController.navigate(Screen.ExamList.route) {
                                        popUpTo(Screen.ServerConfig.route) {
                                            inclusive = true
                                        }
                                        launchSingleTop = true
                                    }
                                } else { showToast(context, "连接失败") }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Link,
                        contentDescription = "连接服务器",
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(0.5f))
    }
}

private fun showToast(context: Context, message: String) {
    android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
}