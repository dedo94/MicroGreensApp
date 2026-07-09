package com.dedo94.microgreensapp.navigation

import kotlinx.serialization.Serializable

@Serializable
object CalendarRoute

@Serializable
object TraysListRoute

@Serializable
object StatsRoute

@Serializable
object SettingsRoute

@Serializable
object TemplateListRoute

@Serializable
data class TemplateEditRoute(val templateId: Long = 0L)

@Serializable
object TrayCreateRoute

@Serializable
data class TrayDetailRoute(val trayId: Long)

@Serializable
data class EventEditRoute(val trayId: Long, val eventId: Long = 0L)
