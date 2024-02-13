package dev.eren.removebg

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

abstract class BaseViewModel<TViewState, TAction, TViewAction> : ViewModel() {
    abstract val initialState: TViewState

    private val _state: MutableStateFlow<TViewState> by lazy {
        MutableStateFlow(initialState)
    }
    val state: StateFlow<TViewState>
        get() = _state.asStateFlow()

    val currentState: TViewState
        get() = state.value

    protected val _action = MutableSharedFlow<TAction>()
    val action: Flow<TAction>
        get() = _action

    abstract fun handleViewAction(viewAction: TViewAction)

    fun updateState(update: TViewState.() -> TViewState) {
        _state.update { state ->
            update(state)
        }
    }
}