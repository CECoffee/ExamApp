package dev.coffee.examapp.ui.screens.practice

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import dev.coffee.examapp.viewmodel.PracticeViewModel

@Composable
fun PracticeScreen (
    navController: NavController,
    viewModel: PracticeViewModel = viewModel()
){
    val scope = rememberCoroutineScope()

    // TODO
    LaunchedEffect(Unit) { /*...*/ }
    // ...
}