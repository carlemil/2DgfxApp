package com.twodgfxapp.ui.menu

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.twodgfxapp.R
import com.twodgfxapp.navigation.Screen

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MenuScreen(
    navController: NavController,
    viewModel:     MenuViewModel = viewModel(),
) {
    val effectsByCategory by viewModel.effectsByCategory.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color    = Color.Black,
    ) {
        if (effectsByCategory.isEmpty() || effectsByCategory.values.all { it.isEmpty() }) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    text  = stringResource(R.string.no_effects_available),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            return@Surface
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            effectsByCategory.forEach { (category, effects) ->

                // ── Category header ───────────────────────────────────────────
                stickyHeader(key = category.name) {
                    Surface(color = Color(0xFF1A1A2E)) {
                        Text(
                            text     = category.displayName,
                            color    = Color(0xFF00D4FF),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                }

                // ── Effect rows ───────────────────────────────────────────────
                items(
                    items = effects,
                    key   = { it.id },
                ) { descriptor ->
                    Text(
                        text     = descriptor.name,
                        color    = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate(Screen.Effect.withId(descriptor.id))
                            }
                            .padding(horizontal = 24.dp, vertical = 14.dp),
                    )
                    HorizontalDivider(color = Color(0xFF2A2A2A), thickness = 0.5.dp)
                }
            }
        }
    }
}
