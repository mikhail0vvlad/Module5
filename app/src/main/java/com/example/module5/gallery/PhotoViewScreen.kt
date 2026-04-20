package com.example.module5.gallery

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavController
import coil.compose.AsyncImage
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoViewScreen(
    filePath: String,
    navController: NavController,
    viewModel: GalleryViewModel
) {
    val file = remember(filePath) { File(filePath) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Задание 3: реакция на результат экспорта в MediaStore
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is GalleryEvent.ExportSuccess ->
                    snackbarHostState.showSnackbar("Фото добавлено в галерею")
                is GalleryEvent.ExportError ->
                    snackbarHostState.showSnackbar("Ошибка: ${event.message}")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(file.name) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    // Задание 3: кнопка экспорта в системную галерею
                    IconButton(onClick = { viewModel.exportToGallery(file) }) {
                        Icon(Icons.Default.Share, contentDescription = "Экспорт в галерею")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = file,
                contentDescription = "Фото",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
