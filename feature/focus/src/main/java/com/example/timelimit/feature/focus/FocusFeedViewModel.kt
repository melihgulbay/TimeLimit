package com.example.timelimit.feature.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timelimit.core.model.Quote
import com.example.timelimit.core.data.repository.FocusQuoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface FocusFeedUiState {
    data object Loading : FocusFeedUiState
    data class Success(val quotes: List<Quote>) : FocusFeedUiState
    data class Error(val message: String) : FocusFeedUiState
}

@HiltViewModel
class FocusFeedViewModel @Inject constructor(
    private val repository: FocusQuoteRepository
) : ViewModel() {

    val uiState: StateFlow<FocusFeedUiState> = repository.observeQuotes()
        .map { quotes ->
            if (quotes.isEmpty()) FocusFeedUiState.Loading
            else FocusFeedUiState.Success(quotes)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FocusFeedUiState.Loading
        )

    init {
        refreshQuotes()
    }

    fun refreshQuotes() {
        viewModelScope.launch {
            repository.refreshQuotes()
        }
    }
}
