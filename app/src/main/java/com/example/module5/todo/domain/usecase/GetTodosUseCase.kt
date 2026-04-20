package com.example.module5.todo.domain.usecase

import com.example.module5.todo.domain.model.Todo
import com.example.module5.todo.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow

class GetTodosUseCase(private val repository: TodoRepository) {
    operator fun invoke(): Flow<List<Todo>> = repository.getAllTodos()
}
