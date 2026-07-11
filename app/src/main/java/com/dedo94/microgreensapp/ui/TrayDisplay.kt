package com.dedo94.microgreensapp.ui

import androidx.compose.ui.graphics.Color
import com.dedo94.microgreensapp.core.database.entity.SubstrateType
import com.dedo94.microgreensapp.core.database.entity.TrayEntity
import com.dedo94.microgreensapp.core.database.entity.TrayStatus
import com.dedo94.microgreensapp.core.database.entity.TrayStepStatus
import com.dedo94.microgreensapp.ui.theme.TrayPalette

fun TrayStatus.displayLabel(): String = when (this) {
    TrayStatus.IN_PROGRESS -> "In corso"
    TrayStatus.HARVESTED -> "Raccolto"
}

fun TrayStepStatus.displayLabel(): String = when (this) {
    TrayStepStatus.PENDING -> "Da fare"
    TrayStepStatus.DONE -> "Fatto"
    TrayStepStatus.SKIPPED -> "Saltato"
}

fun SubstrateType.displayLabel(): String = when (this) {
    SubstrateType.HYDROPONIC_MAT -> "Idroponica"
    SubstrateType.SOIL -> "Terriccio"
    SubstrateType.OTHER -> "Altro"
}

fun TrayEntity.displayColor(): Color =
    colorTag?.let(::Color) ?: TrayPalette[(id % TrayPalette.size).toInt()]
