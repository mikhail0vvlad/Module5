package com.example.module5.diary

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DiaryViewModel(private val context: Context) : ViewModel() {

    private val diaryDir: File
        get() = File(context.filesDir, "diary").also { it.mkdirs() }

    private val _entries = MutableStateFlow<List<DiaryEntry>>(emptyList())
    val entries: StateFlow<List<DiaryEntry>> = _entries.asStateFlow()

    init {
        // Полное сканирование папки только при запуске
        loadAllEntries()
    }

    private fun loadAllEntries() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = diaryDir.listFiles()
                ?.filter { it.extension == "txt" }
                ?.mapNotNull { file -> parseFile(file) }
                ?.sortedByDescending { it.timestamp }
                ?: emptyList()
            _entries.value = list
        }
    }

    fun readEntryContent(filename: String): String {
        return File(diaryDir, filename).readText()
    }

    fun saveEntry(filename: String?, title: String, content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val timestamp = System.currentTimeMillis()
            val safeTitle = title.replace(Regex("[^а-яА-Яa-zA-Z0-9_\\- ]"), "").trim()

            val actualFilename = filename ?: run {
                val datePart = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    .format(Date(timestamp))
                if (safeTitle.isNotEmpty()) "${datePart}_${safeTitle}.txt"
                else "${datePart}.txt"
            }

            val file = File(diaryDir, actualFilename)
            file.writeText("$title\n\n$content")

            val newEntry = DiaryEntry(
                filename = actualFilename,
                title = title.ifEmpty { "Без заголовка" },
                preview = content.take(40),
                timestamp = timestamp
            )

            withContext(Dispatchers.Main) {
                if (filename == null) {
                    // Новая запись добавляется в начало без пересканирования
                    _entries.value = listOf(newEntry) + _entries.value
                } else {
                    // Обновляем существующую запись
                    _entries.value = _entries.value.map {
                        if (it.filename == filename) newEntry else it
                    }
                }
            }
        }
    }

    fun deleteEntry(entry: DiaryEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            File(diaryDir, entry.filename).delete()
            withContext(Dispatchers.Main) {
                // Удаление по имени файла без пересканирования
                _entries.value = _entries.value.filter { it.filename != entry.filename }
            }
        }
    }

    private fun parseFile(file: File): DiaryEntry? = runCatching {
        val lines = file.readLines()
        val title = lines.firstOrNull()?.trim() ?: "Без заголовка"
        val content = lines.drop(2).joinToString("\n")
        DiaryEntry(
            filename = file.name,
            title = title.ifEmpty { "Без заголовка" },
            preview = content.take(40),
            timestamp = file.lastModified()
        )
    }.getOrNull()
}

class DiaryViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return DiaryViewModel(context.applicationContext) as T
    }
}
