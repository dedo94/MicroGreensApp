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
    SubstrateType.SOIL -> "Terriccio"
    SubstrateType.COCO_COIR -> "Fibra di cocco"
    SubstrateType.HEMP_MAT -> "Tappetino di canapa"
    SubstrateType.PAPER_TOWEL -> "Carta assorbente"
    SubstrateType.PERLITE_VERMICULITE -> "Perlite/Vermiculite"
    SubstrateType.HYDROPONIC_MAT -> "Tappetino idroponico"
    SubstrateType.OTHER -> "Altro"
}

fun TrayEntity.displayColor(): Color =
    colorTag?.let(::Color) ?: TrayPalette[(id % TrayPalette.size).toInt()]
