package com.dedo94.microgreensapp.navigation

import kotlinx.serialization.Serializable

@Serializable
object TemplateListRoute

@Serializable
data class TemplateEditRoute(val templateId: Long = 0L)
