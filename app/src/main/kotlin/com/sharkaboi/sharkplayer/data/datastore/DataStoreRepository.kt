package com.sharkaboi.sharkplayer.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sharkaboi.sharkplayer.common.models.SharkPlayerFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

private const val PREFERENCES_NAME = "SharkPlayerDataStore"
internal val Context.dataStore by preferencesDataStore(
    name = PREFERENCES_NAME
)
private val FAVORITES_KEY = stringSetPreferencesKey("favourite_directories")

class DataStoreRepository(
    private val dataStore: DataStore<Preferences>
) {
    val favouritesDirsFlow: Flow<List<SharkPlayerFile.Directory>> =
        dataStore.data.catch { exception ->
            Timber.w(exception)
            emit(emptyPreferences())
        }.map { preferences ->
            preferences[FAVORITES_KEY]?.map { path ->
                val file = File(path)
                SharkPlayerFile.Directory(
                    folderName = file.nameWithoutExtension,
                    path = path,
                    childFileCount = file.list()?.size ?: 0
                )
            } ?: emptyList()
        }.flowOn(Dispatchers.IO)

    suspend fun removeFavorite(favorite: SharkPlayerFile.Directory): Unit = withContext(Dispatchers.IO) {
        dataStore.edit { preferences ->
            val oldSet: List<SharkPlayerFile.Directory> =
                favouritesDirsFlow.firstOrNull() ?: emptyList()
            val newSet: MutableSet<String> = oldSet.map { it.path }.toMutableSet()
            newSet.remove(favorite.path)
            preferences[FAVORITES_KEY] = newSet
        }
    }

    suspend fun addFavorite(favorite: SharkPlayerFile.Directory): Unit = withContext(Dispatchers.IO) {
        dataStore.edit { preferences ->
            val oldSet: List<SharkPlayerFile.Directory> =
                favouritesDirsFlow.firstOrNull() ?: emptyList()
            val newSet: MutableSet<String> = oldSet.map { it.path }.toMutableSet()
            newSet.add(favorite.path)
            preferences[FAVORITES_KEY] = newSet
        }
    }
}