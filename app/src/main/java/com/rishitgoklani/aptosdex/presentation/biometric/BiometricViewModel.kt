package com.rishitgoklani.aptosdex.presentation.biometric

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
class BiometricViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(BiometricUiState())
    val uiState: StateFlow<BiometricUiState> = _uiState.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<NavigationEvent>()
    val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents.asSharedFlow()

    sealed class NavigationEvent {
        data object ShowBiometricPrompt : NavigationEvent()
        data object NavigateToHome : NavigationEvent()
    }

    fun onEnableClick() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAuthenticating = true)
            _navigationEvents.emit(NavigationEvent.ShowBiometricPrompt)
        }
    }

    fun onSkipClick() {
        viewModelScope.launch {
            _navigationEvents.emit(NavigationEvent.NavigateToHome)
        }
    }

    fun onBiometricSuccess() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isBiometricEnabled = true,
                isAuthenticating = false
            )
            _navigationEvents.emit(NavigationEvent.NavigateToHome)
        }
    }

    fun onBiometricError() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAuthenticating = false)
            // Could show error message here
        }
    }

    fun onBiometricFailed() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAuthenticating = false)
            // Authentication failed but user can retry
        }
    }
}
