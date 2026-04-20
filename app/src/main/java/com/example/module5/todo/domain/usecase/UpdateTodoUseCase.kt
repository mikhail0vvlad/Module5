package com.example.module5.todo.domain.usecase

import com.example.module5.todo.domain.model.Todo
import com.example.module5.todo.domain.repository.TodoRepository

class UpdateTodoUseCase(private val repository: TodoRepository) {
    suspend operator fun invoke(todo: Todo) = repository.updateTodo(todo)
}
