package com.example.shiftcalculator.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.shiftcalculator.model.ShiftType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.Calendar

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object Storage {
    private val START_DATE_KEY = longPreferencesKey("start_date")
    private val SHIFTS_CONFIG_KEY = stringPreferencesKey("shifts_config")
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun saveStartDate(context: Context, date: Long) {
        context.dataStore.edit { preferences ->
            preferences[START_DATE_KEY] = date
        }
    }

    fun getStartDate(context: Context): Flow<Long?> {
        return context.dataStore.data.map { preferences ->
            preferences[START_DATE_KEY]
        }
    }

    suspend fun clearStartDate(context: Context) {
        context.dataStore.edit { preferences ->
            preferences.remove(START_DATE_KEY)
        }
    }

    suspend fun saveShifts(context: Context, shifts: List<ShiftType>) {
        context.dataStore.edit { preferences ->
            val jsonString = json.encodeToString(shifts)
            preferences[SHIFTS_CONFIG_KEY] = jsonString
        }
    }

    fun getShifts(context: Context): Flow<List<ShiftType>> {
        return context.dataStore.data.map { preferences ->
            val jsonString = preferences[SHIFTS_CONFIG_KEY]
            if (jsonString != null) {
                try {
                    json.decodeFromString<List<ShiftType>>(jsonString)
                } catch (e: Exception) {
                    ShiftType.getDefaultShifts()
                }
            } else {
                ShiftType.getDefaultShifts()
            }
        }
    }
}

