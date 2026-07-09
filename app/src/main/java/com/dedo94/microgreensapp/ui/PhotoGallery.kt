package com.dedo94.microgreensapp.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import com.dedo94.microgreensapp.core.database.entity.PhotoEntity
import java.io.File

/**
 * Galleria orizzontale con tessera "aggiungi" (fotocamera o galleria di
 * sistema) più le miniature esistenti; tap su una miniatura apre un
 * visualizzatore a schermo intero con opzione elimina.
 */
@Composable
fun PhotoGallery(
    photos: List<PhotoEntity>,
    fileFor: (PhotoEntity) -> File,
    onCreateCaptureTarget: () -> Pair<File, Uri>,
    onPhotoCaptured: (File) -> Unit,
    onPhotoPicked: (Uri) -> Unit,
    onDeletePhoto: (PhotoEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    var viewerPhoto by remember { mutableStateOf<PhotoEntity?>(null) }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            AddPhotoTile(
                onCreateCaptureTarget = onCreateCaptureTarget,
                onPhotoCaptured = onPhotoCaptured,
                onPhotoPicked = onPhotoPicked,
            )
        }
        items(items = photos, key = { it.id }) { photo ->
            Surface(
                modifier = Modifier
                    .size(88.dp)
                    .clickable { viewerPhoto = photo },
                shape = RoundedCornerShape(8.dp),
            ) {
                AsyncImage(
                    model = fileFor(photo),
                    contentDescription = photo.caption.ifBlank { "Foto" },
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    viewerPhoto?.let { photo ->
        Dialog(onDismissRequest = { viewerPhoto = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    .padding(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    IconButton(onClick = {
                        onDeletePhoto(photo)
                        viewerPhoto = null
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Elimina foto")
                    }
                    IconButton(onClick = { viewerPhoto = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Chiudi")
                    }
                }
                AsyncImage(
                    model = fileFor(photo),
                    contentDescription = photo.caption.ifBlank { "Foto" },
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                )
            }
        }
    }
}

@Composable
private fun AddPhotoTile(
    onCreateCaptureTarget: () -> Pair<File, Uri>,
    onPhotoCaptured: (File) -> Unit,
    onPhotoPicked: (Uri) -> Unit,
) {
    var showChooser by remember { mutableStateOf(false) }
    var pendingCaptureFile by remember { mutableStateOf<File?>(null) }

    val captureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { success ->
        val file = pendingCaptureFile
        pendingCaptureFile = null
        if (success && file != null) {
            onPhotoCaptured(file)
        }
    }
    val pickLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri -> uri?.let(onPhotoPicked) }

    Surface(
        modifier = Modifier
            .size(88.dp)
            .clickable { showChooser = true },
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(Icons.Default.AddAPhoto, contentDescription = "Aggiungi foto")
        }
    }

    if (showChooser) {
        AlertDialog(
            onDismissRequest = { showChooser = false },
            title = { Text("Aggiungi foto") },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showChooser = false
                                val (file, uri) = onCreateCaptureTarget()
                                pendingCaptureFile = file
                                captureLauncher.launch(uri)
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null)
                        Text("  Scatta foto")
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showChooser = false
                                pickLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                                )
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Text("  Scegli dalla galleria")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showChooser = false }) { Text("Annulla") }
            },
        )
    }
}
