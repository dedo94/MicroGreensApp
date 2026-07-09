package com.dedo94.microgreensapp.core.database.entity

/**
 * Categoria dell'azione prevista da uno step di template/vassoio o registrata
 * in un evento. Guida sia la UI (icone/etichette) sia la generazione dei
 * promemoria.
 */
enum class ActionType {
    SOAKING,
    RINSING,
    MOLD_PREVENTION,
    TRAY_TRANSFER,
    LIGHT_GROWTH,
    HARVEST,
    STORAGE,
    WATERING,
    NOTE,
    PHOTO_ONLY,
    CUSTOM,
}
