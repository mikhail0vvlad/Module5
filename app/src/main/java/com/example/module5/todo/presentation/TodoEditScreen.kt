package com.example.module5.todo.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoEditScreen(
    todoId: Int,
    navController: NavController,
    viewModel: TodoViewModel
) {
    val isEditing = todoId != -1
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isCompleted by remember { mutableStateOf(false) }

    // Предзаполнение полей при редактировании
    val todos by viewModel.todos.collectAsState()
    LaunchedEffect(todoId, todos) {
        if (isEditing) {
            todos.find { it.id == todoId }?.let { todo ->
                title = todo.title
                description = todo.description
                isCompleted = todo.isCompleted
            }
        }
    }

    // Закрываем экран после успешного сохранения
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (event is TodoEvent.SavedSuccessfully) {
                navController.popBackStack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Редактировать задачу" else "Новая задача") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .imePadding()
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Название задачи") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = title.isBlank()
            )
            if (title.isBlank()) {
                Text(
                    "Название обязательно",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Описание (необязательно)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = isCompleted,
                    onCheckedChange = { isCompleted = it }
                )
                Text(
                    text = "Выполнено",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.save(todoId, title, description, isCompleted)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank()
            ) {
                Text("Сохранить", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
