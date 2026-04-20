package com.example.module5.navigation

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.module5.Module5Application
import com.example.module5.diary.DiaryEditScreen
import com.example.module5.diary.DiaryListScreen
import com.example.module5.diary.DiaryViewModel
import com.example.module5.diary.DiaryViewModelFactory
import com.example.module5.gallery.GalleryScreen
import com.example.module5.gallery.GalleryViewModel
import com.example.module5.gallery.GalleryViewModelFactory
import com.example.module5.gallery.PhotoViewScreen
import com.example.module5.home.HomeScreen
import com.example.module5.todo.presentation.TodoEditScreen
import com.example.module5.todo.presentation.TodoListScreen
import com.example.module5.todo.presentation.TodoViewModel
import com.example.module5.todo.presentation.TodoViewModelFactory

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val app = context.applicationContext as Module5Application

    // ── ViewModels привязаны к Activity, а не к отдельным destination ─────
    // Это гарантирует общий стейт между списком и экраном редактирования

    val diaryVm: DiaryViewModel = viewModel(
        viewModelStoreOwner = activity,
        factory = DiaryViewModelFactory(context)
    )

    val galleryVm: GalleryViewModel = viewModel(
        viewModelStoreOwner = activity,
        factory = GalleryViewModelFactory(context)
    )

    val todoVm: TodoViewModel = viewModel(
        viewModelStoreOwner = activity,
        factory = TodoViewModelFactory(
            repository = app.todoRepository,
            preferences = app.todoPreferences,
            context = context
        )
    )

    NavHost(navController = navController, startDestination = "home") {

        composable("home") {
            HomeScreen(navController)
        }

        // ── Задание 1: Дневник ──────────────────────────────────────────────
        composable("diary_list") {
            DiaryListScreen(navController = navController, viewModel = diaryVm)
        }

        composable(
            route = "diary_edit?filename={filename}",
            arguments = listOf(navArgument("filename") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStack ->
            DiaryEditScreen(
                filename = backStack.arguments?.getString("filename"),
                navController = navController,
                viewModel = diaryVm
            )
        }

        // ── Задания 2-3: Фотогалерея ───────────────────────────────────────
        composable("gallery") {
            GalleryScreen(navController = navController, viewModel = galleryVm)
        }

        composable(
            route = "photo_view/{encodedPath}",
            arguments = listOf(navArgument("encodedPath") { type = NavType.StringType })
        ) { backStack ->
            val decodedPath = Uri.decode(backStack.arguments?.getString("encodedPath") ?: "")
            PhotoViewScreen(
                filePath = decodedPath,
                navController = navController,
                viewModel = galleryVm
            )
        }

        // ── Задание 4: Список дел ──────────────────────────────────────────
        composable("todo_list") {
            TodoListScreen(navController = navController, viewModel = todoVm)
        }

        composable(
            route = "todo_edit?todoId={todoId}",
            arguments = listOf(navArgument("todoId") {
                type = NavType.IntType
                defaultValue = -1
            })
        ) { backStack ->
            TodoEditScreen(
                todoId = backStack.arguments?.getInt("todoId") ?: -1,
                navController = navController,
                viewModel = todoVm
            )
        }
    }
}
