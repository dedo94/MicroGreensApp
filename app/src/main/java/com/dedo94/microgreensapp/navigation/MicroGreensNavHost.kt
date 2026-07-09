package com.dedo94.microgreensapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dedo94.microgreensapp.feature.template.TemplateEditScreen
import com.dedo94.microgreensapp.feature.template.TemplateListScreen

@Composable
fun MicroGreensNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = TemplateListRoute) {
        composable<TemplateListRoute> {
            TemplateListScreen(
                onCreateTemplate = { navController.navigate(TemplateEditRoute(templateId = 0L)) },
                onOpenTemplate = { id -> navController.navigate(TemplateEditRoute(templateId = id)) },
            )
        }
        composable<TemplateEditRoute> {
            TemplateEditScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}
