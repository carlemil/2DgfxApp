package com.twodgfxapp.ui.effect

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.twodgfxapp.model.EffectCatalogue

/**
 * Semi-transparent drawer overlay shown during quick-switch (US3).
 * Displays all 29 effects; selecting one calls [onEffectSelected].
 * Tapping outside the list calls [onDismiss].
 */
@Composable
fun QuickSwitchOverlay(
    onEffectSelected: (effectId: String) -> Unit,
    onDismiss:        () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x88000000))   // dimmed scrim
            .clickable(onClick = onDismiss), // tap outside → dismiss
    ) {
        // Drawer panel on the left side
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .width(240.dp)
                .background(Color(0xEE0D0D1A))
                .align(Alignment.CenterStart)
                // Stop taps inside the panel from propagating to the scrim
                .clickable(enabled = false) {},
        ) {
            item {
                Text(
                    text     = "Switch Effect",
                    color    = Color(0xFF00D4FF),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
                HorizontalDivider(color = Color(0xFF333355))
            }

            items(EffectCatalogue.effects, key = { it.id }) { descriptor ->
                Text(
                    text     = descriptor.name,
                    color    = Color.White,
                    fontSize = 15.sp,
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .clickable { onEffectSelected(descriptor.id) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                )
                HorizontalDivider(color = Color(0xFF1A1A2A), thickness = 0.5.dp)
            }
        }
    }
}
