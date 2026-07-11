package com.dedo94.microgreensapp.feature.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dedo94.microgreensapp.ui.CompactHeader

@Composable
fun SettingsScreen(
    onManageVarieties: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    var hasNotificationPermission by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> hasNotificationPermission = granted }

    val location by viewModel.location.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        CompactHeader("Opzioni")
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Text("Varietà", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onManageVarieties),
                leadingContent = { Icon(Icons.Outlined.Eco, contentDescription = null) },
                headlineContent = { Text("Gestisci varietà") },
                supportingContent = { Text("Crea e modifica le varietà e i loro piani di coltivazione") },
                trailingContent = {
                    Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null)
                },
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

            Text("Notifiche", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            if (hasNotificationPermission) {
                Text("I promemoria per le azioni pianificate sono attivi.")
            } else {
                Text("Attiva le notifiche per ricevere i promemoria (es. \"oggi sciacqua i semi\").")
                Spacer(Modifier.height(8.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }) {
                    Text("Attiva notifiche")
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

            Text("Meteo", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                location?.let { "Posizione attuale: ${it.name}" }
                    ?: "Nessuna posizione impostata: temperatura/umidità/alba-tramonto non verranno pre-compilate negli eventi.",
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = viewModel.query,
                onValueChange = viewModel::onQueryChange,
                label = { Text("Cerca una città") },
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = viewModel::search) {
                        Icon(Icons.Outlined.Search, contentDescription = "Cerca")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
            if (viewModel.isSearching) {
                Spacer(Modifier.height(8.dp))
                Text("Ricerca in corso…", style = MaterialTheme.typography.bodySmall)
            }
            viewModel.results.forEach { result ->
                val subtitle = listOfNotNull(result.admin1, result.country).joinToString(", ")
                ListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectLocation(result) },
                    headlineContent = { Text(result.name) },
                    supportingContent = { Text(subtitle) },
                )
            }
        }
    }
}
