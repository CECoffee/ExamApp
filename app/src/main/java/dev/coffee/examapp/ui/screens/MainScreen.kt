package dev.coffee.examapp.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.coffee.examapp.ui.components.BottomNavigationBar
import dev.coffee.examapp.ui.navigation.NavigationHost
import dev.coffee.examapp.ui.navigation.Screen

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomBarItems = listOf(Screen.Practice, Screen.ExamList, Screen.WrongQuestion)
    val hideBottomBarItems = listOf(Screen.Exam.route + "/", Screen.ServerConfig.route)
    val shouldShowBottomBar = hideBottomBarItems.none { baseRoute ->
        currentDestination?.route?.startsWith(baseRoute) == true
    }

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                BottomNavigationBar(
                    navController = navController,
                    currentDestination = currentDestination,
                    items = bottomBarItems
                )
            }
        }
    ) { innerPadding ->
        NavigationHost(
            navController = navController,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}