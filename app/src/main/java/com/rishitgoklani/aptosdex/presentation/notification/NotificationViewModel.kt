package com.rishitgoklani.aptosdex.presentation.notification

import android.Manifest
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<NavigationEvent>()
    val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents.asSharedFlow()

    sealed class NavigationEvent {
        data object RequestPermission : NavigationEvent()
        data object NavigateToHome : NavigationEvent()
    }

    fun onEnableClick() {
        viewModelScope.launch {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // For Android 13+, request POST_NOTIFICATIONS permission
                _navigationEvents.emit(NavigationEvent.RequestPermission)
            } else {
                // For older versions, notifications are enabled by default
                onPermissionGranted()
            }
        }
    }

    fun onSkipClick() {
        viewModelScope.launch {
            _navigationEvents.emit(NavigationEvent.NavigateToHome)
        }
    }

    fun onPermissionGranted() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPermissionGranted = true)
            _navigationEvents.emit(NavigationEvent.NavigateToHome)
        }
    }

    fun onPermissionDenied() {
        viewModelScope.launch {
            // User denied permission, still navigate to home
            _navigationEvents.emit(NavigationEvent.NavigateToHome)
        }
    }

    companion object {
        const val NOTIFICATION_PERMISSION = Manifest.permission.POST_NOTIFICATIONS
    }
}
