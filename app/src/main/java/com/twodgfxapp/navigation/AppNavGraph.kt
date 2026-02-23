package com.twodgfxapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.twodgfxapp.ui.effect.EffectScreen
import com.twodgfxapp.ui.menu.MenuScreen

/** All navigation destinations in the app. */
sealed class Screen(val route: String) {
    object Menu : Screen("menu")
    object Effect : Screen("effect/{effectId}") {
        const val ARG_EFFECT_ID = "effectId"
        fun withId(effectId: String) = "effect/$effectId"
    }
}

/**
 * Root NavHost. Starts at [Screen.Menu].
 *
 * Back-stack invariant: at most two entries — [menu] and [effect/{effectId}].
 * Quick-switch replaces the current effect entry in-place using popUpTo(inclusive=true).
 */
@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController    = navController,
        startDestination = Screen.Menu.route,
    ) {
        composable(Screen.Menu.route) {
            MenuScreen(navController = navController)
        }

        composable(
            route     = Screen.Effect.route,
            arguments = listOf(
                navArgument(Screen.Effect.ARG_EFFECT_ID) { type = NavType.StringType }
            ),
        ) {
            EffectScreen(navController = navController)
        }
    }
}
