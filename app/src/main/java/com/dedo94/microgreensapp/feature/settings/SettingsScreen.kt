package com.dedo94.microgreensapp.feature.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dedo94.microgreensapp.ui.CompactHeader
import com.dedo94.microgreensapp.ui.theme.Spacing

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
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    var showLocationEditor by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        CompactHeader("Opzioni")
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.md),
        ) {
            Text("Varietà", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(Spacing.xs))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onManageVarieties)
                    .padding(vertical = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.Eco, contentDescription = null)
                Spacer(Modifier.width(Spacing.md))
                Column(Modifier.weight(1f)) {
                    Text("Gestisci varietà")
                    Text(
                        text = "Crea e modifica le varietà e i loro piani di coltivazione",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.width(Spacing.sm))
                Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.lg))

            Text("Notifiche", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(Spacing.sm))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Promemoria attivi")
                    Text(
                        text = "Ricevi una notifica per le azioni pianificate (es. \"oggi sciacqua i semi\").",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = viewModel::onNotificationsEnabledChange,
                )
            }
            if (!hasNotificationPermission) {
                Spacer(Modifier.height(Spacing.sm))
                Text(
                    text = "Il sistema richiede anche il permesso di notifica per mostrarle davvero.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
                Spacer(Modifier.height(Spacing.sm))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }) {
                    Text("Concedi permesso")
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.lg))

            Text("Meteo", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(Spacing.xs))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showLocationEditor = !showLocationEditor }
                    .padding(vertical = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.Public, contentDescription = null)
                Spacer(Modifier.width(Spacing.md))
                Column(Modifier.weight(1f)) {
                    Text(location?.name ?: "Nessuna posizione impostata")
                    Text(
                        text = if (location != null) "Tocca per cambiare" else "Tocca per impostare la posizione",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.width(Spacing.sm))
                Icon(
                    imageVector = if (showLocationEditor) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                    contentDescription = null,
                )
            }
            if (showLocationEditor) {
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
                    Spacer(Modifier.height(Spacing.sm))
                    Text("Ricerca in corso…", style = MaterialTheme.typography.bodySmall)
                }
                viewModel.results.forEach { result ->
                    val subtitle = listOfNotNull(result.admin1, result.country).joinToString(", ")
                    ListItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.selectLocation(result)
                                showLocationEditor = false
                            },
                        headlineContent = { Text(result.name) },
                        supportingContent = { Text(subtitle) },
                    )
                }
            }
            if (location != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            enabled = !viewModel.isBackfillingWeather,
                            onClick = viewModel::backfillMissingWeather,
                        )
                        .padding(vertical = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.Refresh, contentDescription = null)
                    Spacer(Modifier.width(Spacing.md))
                    Column(Modifier.weight(1f)) {
                        Text("Recupera meteo mancante")
                        Text(
                            text = when {
                                viewModel.isBackfillingWeather -> "Recupero in corso…"
                                viewModel.weatherBackfillResult != null -> {
                                    val updated = viewModel.weatherBackfillResult!!
                                    if (updated > 0) {
                                        "Aggiornat${if (updated == 1) "o" else "i"} $updated event${if (updated == 1) "o" else "i"}"
                                    } else {
                                        "Nessun evento da aggiornare"
                                    }
                                }
                                else -> "Per gli eventi passati senza temperatura/umidità"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (viewModel.isBackfillingWeather) {
                        Spacer(Modifier.width(Spacing.sm))
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    }
                }
            }
        }
    }
}
