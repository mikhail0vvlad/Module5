package com.example.module5.todo.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.module5.todo.domain.model.Todo
import com.example.module5.ui.theme.CompletedTaskGreen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TodoListScreen(navController: NavController, viewModel: TodoViewModel) {
    val todos by viewModel.todos.collectAsState()
    val useCompletedColor by viewModel.useCompletedColor.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Список дел") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    // Задание 4: переключатель цвета выполненных задач в TopAppBar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "Цвет",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Switch(
                            checked = useCompletedColor,
                            onCheckedChange = { viewModel.toggleCompletedColor() }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("todo_edit") }) {
                Icon(Icons.Default.Add, contentDescription = "Добавить задачу")
            }
        }
    ) { innerPadding ->
        if (todos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Задач пока нет.\nНажмите + чтобы добавить.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(todos, key = { it.id }) { todo ->
                    TodoItem(
                        todo = todo,
                        useCompletedColor = useCompletedColor,
                        onToggle = { viewModel.toggleCompleted(todo) },
                        onEdit = { navController.navigate("todo_edit?todoId=${todo.id}") },
                        onDelete = { viewModel.delete(todo) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TodoItem(
    todo: Todo,
    useCompletedColor: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    // Задание 4: цвет фона выполненной задачи из DataStore
    val backgroundColor = when {
        todo.isCompleted && useCompletedColor -> CompletedTaskGreen
        todo.isCompleted -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface
    }

    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onEdit,
                    onLongClick = { menuExpanded = true }
                ),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = todo.isCompleted,
                    onCheckedChange = { onToggle() }
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = todo.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else null,
                        color = if (todo.isCompleted)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                    if (todo.description.isNotBlank()) {
                        Text(
                            text = todo.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Удалить") },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                onClick = {
                    menuExpanded = false
                    onDelete()
                }
            )
        }
    }
}
