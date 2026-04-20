package com.example.module5.todo.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.module5.todo.data.preferences.TodoPreferences
import com.example.module5.todo.domain.model.Todo
import com.example.module5.todo.domain.repository.TodoRepository
import com.example.module5.todo.domain.usecase.AddTodoUseCase
import com.example.module5.todo.domain.usecase.DeleteTodoUseCase
import com.example.module5.todo.domain.usecase.GetTodosUseCase
import com.example.module5.todo.domain.usecase.ImportTodosFromJsonUseCase
import com.example.module5.todo.domain.usecase.UpdateTodoUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TodoViewModel(
    repository: TodoRepository,
    private val preferences: TodoPreferences,
    private val context: Context
) : ViewModel() {

    // Use cases
    private val getTodos = GetTodosUseCase(repository)
    private val addTodo = AddTodoUseCase(repository)
    private val updateTodo = UpdateTodoUseCase(repository)
    private val deleteTodo = DeleteTodoUseCase(repository)
    private val importFromJson = ImportTodosFromJsonUseCase(context, repository, preferences)

    /** Список задач из Room — реактивный Flow → StateFlow */
    val todos: StateFlow<List<Todo>> = getTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * Задание 4: настройка цвета выполненных задач из DataStore.
     * Конвертируем Flow в StateFlow для использования в Compose.
     */
    val useCompletedColor: StateFlow<Boolean> = preferences.useCompletedColor
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    private val _events = MutableSharedFlow<TodoEvent>()
    val events: SharedFlow<TodoEvent> = _events.asSharedFlow()

    init {
        // Задание 4: одноразовый импорт JSON при первом запуске
        viewModelScope.launch {
            importFromJson()
        }
    }

    fun toggleCompleted(todo: Todo) {
        viewModelScope.launch {
            updateTodo(todo.copy(isCompleted = !todo.isCompleted))
        }
    }

    fun delete(todo: Todo) {
        viewModelScope.launch {
            deleteTodo(todo)
        }
    }

    fun save(id: Int, title: String, description: String, isCompleted: Boolean) {
        if (title.isBlank()) return
        viewModelScope.launch {
            if (id == -1) {
                addTodo(
                    Todo(
                        title = title.trim(),
                        description = description.trim(),
                        isCompleted = isCompleted
                    )
                )
            } else {
                // Сохраняем оригинальную дату создания, чтобы не менять порядок сортировки
                val originalCreatedAt = todos.value.find { it.id == id }?.createdAt
                    ?: System.currentTimeMillis()
                updateTodo(
                    Todo(
                        id = id,
                        title = title.trim(),
                        description = description.trim(),
                        isCompleted = isCompleted,
                        createdAt = originalCreatedAt
                    )
                )
            }
            _events.emit(TodoEvent.SavedSuccessfully)
        }
    }

    /** Задание 4: переключение настройки через DataStore */
    fun toggleCompletedColor() {
        viewModelScope.launch {
            preferences.setUseCompletedColor(!useCompletedColor.value)
        }
    }

    suspend fun getTodoById(id: Int): Todo? {
        return todos.value.find { it.id == id }
    }
}

sealed class TodoEvent {
    object SavedSuccessfully : TodoEvent()
}

class TodoViewModelFactory(
    private val repository: TodoRepository,
    private val preferences: TodoPreferences,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return TodoViewModel(repository, preferences, context.applicationContext) as T
    }
}
