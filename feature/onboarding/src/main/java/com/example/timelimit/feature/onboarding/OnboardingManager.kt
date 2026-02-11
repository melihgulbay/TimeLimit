package com.example.timelimit.feature.onboarding

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.onboardingDataStore: DataStore<Preferences> by preferencesDataStore(name = "onboarding_preferences")

class OnboardingManager(private val context: Context) {
    private val IS_ONBOARDING_COMPLETED = booleanPreferencesKey("is_onboarding_completed")

    val isOnboardingCompleted: Flow<Boolean> = context.onboardingDataStore.data
        .map { preferences ->
            preferences[IS_ONBOARDING_COMPLETED] ?: false
        }

    suspend fun setOnboardingCompleted() {
        context.onboardingDataStore.edit { preferences ->
            preferences[IS_ONBOARDING_COMPLETED] = true
        }
    }

    suspend fun resetOnboarding() {
        context.onboardingDataStore.edit { preferences ->
            preferences[IS_ONBOARDING_COMPLETED] = false
        }
    }
}

