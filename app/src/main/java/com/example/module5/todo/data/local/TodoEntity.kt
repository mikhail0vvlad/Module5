package com.example.module5.todo.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.module5.todo.domain.model.Todo

@Entity(tableName = "todos")
data class TodoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain() = Todo(
        id = id,
        title = title,
        description = description,
        isCompleted = isCompleted,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(todo: Todo) = TodoEntity(
            id = todo.id,
            title = todo.title,
            description = todo.description,
            isCompleted = todo.isCompleted,
            createdAt = todo.createdAt
        )
    }
}
