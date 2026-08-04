package com.example.studysyncandroid.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.studysyncandroid.ui.navigation.StudySyncNavGraph
import com.example.studysyncandroid.ui.session.SessionViewModel

@Composable
fun StudySyncApp(
    modifier: Modifier = Modifier,
    sessionViewModel: SessionViewModel = hiltViewModel()
) {
    val startDestination by sessionViewModel.startDestination.collectAsStateWithLifecycle()
    val currentStart = startDestination

    if (currentStart == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        StudySyncNavGraph(
            navController = rememberNavController(),
            startDestination = currentStart,
            modifier = modifier
        )
    }
}