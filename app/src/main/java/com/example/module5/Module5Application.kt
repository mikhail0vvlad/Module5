package com.example.module5

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.module5.todo.data.local.TodoDatabase
import com.example.module5.todo.data.preferences.TodoPreferences
import com.example.module5.todo.data.repository.TodoRepositoryImpl
import com.example.module5.todo.domain.repository.TodoRepository

val android.content.Context.dataStore: DataStore<Preferences>
    by preferencesDataStore(name = "module5_settings")

class Module5Application : Application() {

    val database: TodoDatabase by lazy { TodoDatabase.getInstance(this) }

    val todoPreferences: TodoPreferences by lazy { TodoPreferences(dataStore) }

    val todoRepository: TodoRepository by lazy {
        TodoRepositoryImpl(database.todoDao())
    }
}
