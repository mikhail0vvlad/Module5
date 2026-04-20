package com.example.module5.todo.data.model

import com.google.gson.annotations.SerializedName

/** Модель для разбора JSON-файла при первоначальном импорте (Задание 4) */
data class TodoJson(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String = "",
    @SerializedName("isCompleted") val isCompleted: Boolean = false
)
