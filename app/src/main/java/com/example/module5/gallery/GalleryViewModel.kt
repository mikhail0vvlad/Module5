package com.example.module5.gallery

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GalleryViewModel(private val context: Context) : ViewModel() {

    private val _photos = MutableStateFlow<List<File>>(emptyList())
    val photos: StateFlow<List<File>> = _photos.asStateFlow()

    private val _events = MutableSharedFlow<GalleryEvent>()
    val events: SharedFlow<GalleryEvent> = _events.asSharedFlow()

    init {
        scanPhotos()
    }

    fun getPicturesDir(): File =
        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: context.filesDir

    fun generatePhotoFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return File(getPicturesDir(), "IMG_$timestamp.jpg")
    }

    fun scanPhotos() {
        viewModelScope.launch(Dispatchers.IO) {
            val files = getPicturesDir()
                .listFiles { f -> f.extension.lowercase() in listOf("jpg", "jpeg", "png") }
                ?.sortedByDescending { it.lastModified() }
                ?: emptyList()
            _photos.value = files
        }
    }

    fun addPhoto(file: File) {
        _photos.value = listOf(file) + _photos.value
    }

    fun deletePhoto(file: File) {
        viewModelScope.launch(Dispatchers.IO) {
            file.delete()
            withContext(Dispatchers.Main) {
                _photos.value = _photos.value.filter { it.absolutePath != file.absolutePath }
            }
        }
    }

    // Задание 3: экспорт в системную галерею через MediaStore
    fun exportToGallery(file: File) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + "/Module5"
                    )
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    ?: error("MediaStore insert returned null")

                resolver.openOutputStream(uri)?.use { out ->
                    file.inputStream().use { it.copyTo(out) }
                }

                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)

                _events.emit(GalleryEvent.ExportSuccess)
            }.onFailure {
                _events.emit(GalleryEvent.ExportError(it.message ?: "Ошибка экспорта"))
            }
        }
    }
}

sealed class GalleryEvent {
    object ExportSuccess : GalleryEvent()
    data class ExportError(val message: String) : GalleryEvent()
}

class GalleryViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return GalleryViewModel(context.applicationContext) as T
    }
}
