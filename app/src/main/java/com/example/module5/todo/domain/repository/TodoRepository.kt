package com.example.module5.todo.domain.repository

import com.example.module5.todo.domain.model.Todo
import kotlinx.coroutines.flow.Flow

interface TodoRepository {
    fun getAllTodos(): Flow<List<Todo>>
    suspend fun getTodoById(id: Int): Todo?
    suspend fun addTodo(todo: Todo): Long
    suspend fun updateTodo(todo: Todo)
    suspend fun deleteTodo(todo: Todo)
    suspend fun insertAll(todos: List<Todo>)
    suspend fun count(): Int
}
