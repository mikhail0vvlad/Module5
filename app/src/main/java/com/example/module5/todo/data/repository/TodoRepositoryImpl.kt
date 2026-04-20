package com.example.module5.todo.data.repository

import com.example.module5.todo.data.local.TodoDao
import com.example.module5.todo.data.local.TodoEntity
import com.example.module5.todo.domain.model.Todo
import com.example.module5.todo.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TodoRepositoryImpl(private val dao: TodoDao) : TodoRepository {

    override fun getAllTodos(): Flow<List<Todo>> =
        dao.getAllTodos().map { list -> list.map { it.toDomain() } }

    override suspend fun getTodoById(id: Int): Todo? =
        dao.getTodoById(id)?.toDomain()

    override suspend fun addTodo(todo: Todo): Long =
        dao.insert(TodoEntity.fromDomain(todo))

    override suspend fun updateTodo(todo: Todo) =
        dao.update(TodoEntity.fromDomain(todo))

    override suspend fun deleteTodo(todo: Todo) =
        dao.delete(TodoEntity.fromDomain(todo))

    override suspend fun insertAll(todos: List<Todo>) =
        dao.insertAll(todos.map { TodoEntity.fromDomain(it) })

    override suspend fun count(): Int = dao.count()
}
