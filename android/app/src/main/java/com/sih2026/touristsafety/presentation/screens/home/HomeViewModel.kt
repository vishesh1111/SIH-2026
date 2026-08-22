package com.sih2026.touristsafety.presentation.screens.home

import android.view.WindowManager
import androidx.lifecycle.ViewModel
import com.sih2026.touristsafety.services.PhysicalSignalService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val physicalSignalService: PhysicalSignalService
) : ViewModel() {

    val isHazardActive: StateFlow<Boolean> = physicalSignalService.isActive

    fun toggleHazard(windowParams: WindowManager.LayoutParams? = null) {
        if (isHazardActive.value) {
            physicalSignalService.deactivate(windowParams)
        } else {
            physicalSignalService.activate(windowParams)
        }
    }
}
