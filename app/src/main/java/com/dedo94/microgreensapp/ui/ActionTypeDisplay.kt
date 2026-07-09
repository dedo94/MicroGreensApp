package com.dedo94.microgreensapp.ui

import com.dedo94.microgreensapp.core.database.entity.ActionType

fun ActionType.displayLabel(): String = when (this) {
    ActionType.SOAKING -> "Ammollo"
    ActionType.RINSING -> "Sciacquo"
    ActionType.MOLD_PREVENTION -> "Prevenzione muffa"
    ActionType.TRAY_TRANSFER -> "Trasferimento vassoio"
    ActionType.LIGHT_GROWTH -> "Crescita / Luce"
    ActionType.HARVEST -> "Raccolta"
    ActionType.STORAGE -> "Conservazione"
    ActionType.WATERING -> "Irrigazione"
    ActionType.NOTE -> "Nota"
    ActionType.PHOTO_ONLY -> "Solo foto"
    ActionType.CUSTOM -> "Personalizzato"
}
