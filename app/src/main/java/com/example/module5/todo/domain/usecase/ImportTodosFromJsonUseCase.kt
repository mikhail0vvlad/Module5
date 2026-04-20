package com.example.module5.todo.domain.usecase

import android.content.Context
import com.example.module5.R
import com.example.module5.todo.data.model.TodoJson
import com.example.module5.todo.data.preferences.TodoPreferences
import com.example.module5.todo.domain.model.Todo
import com.example.module5.todo.domain.repository.TodoRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Задание 4: одноразовый импорт задач из JSON-файла в res/raw в базу данных Room.
 * Повторный запуск пропускается благодаря флагу в DataStore.
 */
class ImportTodosFromJsonUseCase(
    private val context: Context,
    private val repository: TodoRepository,
    private val preferences: TodoPreferences
) {
    suspend operator fun invoke() {
        // Импортируем только если база пустая (первый запуск)
        if (repository.count() > 0) {
            preferences.markJsonImported()
            return
        }

        val json = context.resources
            .openRawResource(R.raw.sample_todos)
            .bufferedReader()
            .use { it.readText() }

        val type = object : TypeToken<List<TodoJson>>() {}.type
        val items: List<TodoJson> = Gson().fromJson(json, type)

        val todos = items.map { item ->
            Todo(
                title = item.title,
                description = item.description,
                isCompleted = item.isCompleted
            )
        }

        repository.insertAll(todos)
        preferences.markJsonImported()
    }
}
