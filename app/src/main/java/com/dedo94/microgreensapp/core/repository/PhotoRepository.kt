package com.dedo94.microgreensapp.core.repository

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.dedo94.microgreensapp.core.database.dao.PhotoDao
import com.dedo94.microgreensapp.core.database.entity.PhotoEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Le foto vivono nello storage privato dell'app (`filesDir/photos`), mai in
 * uno storage condiviso: niente permessi di lettura/scrittura storage da
 * chiedere, e vengono eliminate automaticamente alla disinstallazione.
 */
@Singleton
class PhotoRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val photoDao: PhotoDao,
) {
    private val photosDir: File
        get() = File(context.filesDir, "photos").apply { mkdirs() }

    fun observePhotosForTray(trayId: Long): Flow<List<PhotoEntity>> = photoDao.getForTray(trayId)

    fun observePhotosForEvent(eventId: Long): Flow<List<PhotoEntity>> = photoDao.getForEvent(eventId)

    fun fileFor(photo: PhotoEntity): File = File(photosDir, photo.filePath)

    /** Crea un file vuoto e restituisce l'Uri (via FileProvider) da passare all'intent fotocamera. */
    fun createCaptureTarget(): Pair<File, Uri> {
        val file = File(photosDir, "IMG_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return file to uri
    }

    suspend fun savePhoto(file: File, trayId: Long, eventId: Long?) {
        photoDao.insert(PhotoEntity(trayId = trayId, eventId = eventId, filePath = file.name))
    }

    suspend fun importFromUri(sourceUri: Uri, trayId: Long, eventId: Long?) {
        withContext(Dispatchers.IO) {
            val file = File(photosDir, "IMG_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            } ?: return@withContext
            photoDao.insert(PhotoEntity(trayId = trayId, eventId = eventId, filePath = file.name))
        }
    }

    suspend fun deletePhoto(photo: PhotoEntity) {
        withContext(Dispatchers.IO) { fileFor(photo).delete() }
        photoDao.delete(photo)
    }
}
