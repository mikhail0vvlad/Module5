package com.example.module5.todo.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Задание 4: DataStore-обёртка для хранения настроек списка дел.
 * Заменяет устаревший SharedPreferences.
 */
class TodoPreferences(private val dataStore: DataStore<Preferences>) {

    companion object {
        private val KEY_JSON_IMPORTED = booleanPreferencesKey("json_imported")
        private val KEY_USE_COMPLETED_COLOR = booleanPreferencesKey("use_completed_color")
    }

    /** Был ли уже выполнен одноразовый импорт из JSON */
    val isJsonImported: Flow<Boolean> =
        dataStore.data.map { it[KEY_JSON_IMPORTED] ?: false }

    /** Нужно ли подсвечивать выполненные задачи цветом */
    val useCompletedColor: Flow<Boolean> =
        dataStore.data.map { it[KEY_USE_COMPLETED_COLOR] ?: true }

    suspend fun markJsonImported() {
        dataStore.edit { it[KEY_JSON_IMPORTED] = true }
    }

    suspend fun setUseCompletedColor(value: Boolean) {
        dataStore.edit { it[KEY_USE_COMPLETED_COLOR] = value }
    }
}
