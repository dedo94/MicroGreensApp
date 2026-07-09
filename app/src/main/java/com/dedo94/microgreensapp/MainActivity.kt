package com.dedo94.microgreensapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import com.dedo94.microgreensapp.navigation.MicroGreensNavHost
import com.dedo94.microgreensapp.ui.theme.MicroGreensAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val deepLinkTrayId = mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        deepLinkTrayId.value = extractTrayId(intent)
        setContent {
            MicroGreensAppTheme {
                MicroGreensNavHost(deepLinkTrayId = deepLinkTrayId)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        deepLinkTrayId.value = extractTrayId(intent)
    }

    private fun extractTrayId(intent: Intent?): Long? =
        intent?.getLongExtra(EXTRA_TRAY_ID, -1L)?.takeIf { it != -1L }

    companion object {
        const val EXTRA_TRAY_ID = "extra_tray_id"
    }
}
