package com.twodgfxapp.ui.menu

import androidx.lifecycle.ViewModel
import com.twodgfxapp.model.EffectCatalogue
import com.twodgfxapp.model.EffectCategory
import com.twodgfxapp.model.EffectDescriptor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MenuViewModel : ViewModel() {

    private val _effectsByCategory = MutableStateFlow(EffectCatalogue.byCategory())

    /** Effects grouped by category, in catalogue insertion order. */
    val effectsByCategory: StateFlow<Map<EffectCategory, List<EffectDescriptor>>> =
        _effectsByCategory.asStateFlow()
}
