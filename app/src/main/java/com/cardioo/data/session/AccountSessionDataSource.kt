package com.cardioo.data.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AccountSessionDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private val currentAccountKey = longPreferencesKey("current_account_id")

    val currentAccountId: Flow<Long?> = dataStore.data.map { it[currentAccountKey] }

    suspend fun setCurrentAccountId(id: Long?) {
        dataStore.edit { prefs ->
            if (id == null) prefs.remove(currentAccountKey) else prefs[currentAccountKey] = id
        }
    }
}

