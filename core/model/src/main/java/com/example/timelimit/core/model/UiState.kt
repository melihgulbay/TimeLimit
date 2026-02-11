package com.example.timelimit.core.model

/**
 * A generic sealed interface to represent the state of a UI component.
 * This pattern is widely used in modern Android development to handle
 * Loading, Success, and Error states in a type-safe way.
 */
sealed interface UiState<out T> {
    /**
     * Represents the state when data is being fetched or processed.
     */
    data object Loading : UiState<Nothing>

    /**
     * Represents the state when data has been successfully fetched or processed.
     * @param data The successfully retrieved data.
     */
    data class Success<T>(val data: T) : UiState<T>

    /**
     * Represents the state when an error occurred during data fetching or processing.
     * @param message A human-readable error message.
     */
    data class Error(val message: String) : UiState<Nothing>
}
