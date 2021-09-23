package com.sharkaboi.sharkplayer.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.sharkaboi.sharkplayer.common.extensions.childCount
import com.sharkaboi.sharkplayer.common.models.SharkPlayerFile
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

private const val PREFERENCES_NAME = "SharkPlayerDataStore"
internal val Context.dataStore by preferencesDataStore(
    name = PREFERENCES_NAME
)
private val FAVORITES_KEY = stringSetPreferencesKey("favourite_directories")
private val SUBTITLE_TRACK_INDEX = stringPreferencesKey("subtitle_track_index")
private val AUDIO_TRACK_INDEX = stringPreferencesKey("audio_track_index")

class DataStoreRepository(
    private val dataStore: DataStore<Preferences>,
    private val moshi: Moshi
) {
    private val trackIndexType =
        Types.newParameterizedType(Map::class.java, String::class.java, Integer::class.java)
    private val trackIndexAdapter: JsonAdapter<Map<String, Int>> =
        moshi.adapter<Map<String, Int>>(trackIndexType).nonNull()

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
                    childFileCount = file.childCount
                )
            } ?: emptyList()
        }.flowOn(Dispatchers.IO)

    val subtitleTrackIndices: Flow<Map<String, Int>> =
        dataStore.data.catch { exception ->
            Timber.w(exception)
            emit(emptyPreferences())
        }.map { preferences ->
            preferences[SUBTITLE_TRACK_INDEX]?.let { json ->
                trackIndexAdapter.fromJson(json)
            } ?: hashMapOf()
        }.flowOn(Dispatchers.IO)

    val audioTrackIndices: Flow<Map<String, Int>> =
        dataStore.data.catch { exception ->
            Timber.w(exception)
            emit(emptyPreferences())
        }.map { preferences ->
            preferences[AUDIO_TRACK_INDEX]?.let { json ->
                trackIndexAdapter.fromJson(json)
            } ?: hashMapOf()
        }.flowOn(Dispatchers.IO)

    suspend fun removeFavorite(favorite: SharkPlayerFile.Directory): Unit =
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                val oldSet: Set<String>? = preferences[FAVORITES_KEY]
                val newSet: MutableSet<String> = oldSet?.toMutableSet() ?: mutableSetOf()
                newSet.remove(favorite.path)
                preferences[FAVORITES_KEY] = newSet
            }
        }

    suspend fun addFavorite(favorite: SharkPlayerFile.Directory): Unit =
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                val oldSet: Set<String>? = preferences[FAVORITES_KEY]
                val newSet: MutableSet<String> = oldSet?.toMutableSet() ?: mutableSetOf()
                newSet.add(favorite.path)
                preferences[FAVORITES_KEY] = newSet
            }
        }

    suspend fun setAudioTrackIndexOfDir(trackId: Int, directory: SharkPlayerFile.Directory) =
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                val prefs = preferences[AUDIO_TRACK_INDEX]
                val newMap = hashMapOf<String, Int>()
                prefs?.let {
                    val map = trackIndexAdapter.fromJson(it)
                    if (map != null) {
                        newMap.putAll(map)
                    }
                }
                newMap[directory.path] = trackId
                preferences[AUDIO_TRACK_INDEX] = trackIndexAdapter.toJson(newMap)
            }
        }

    suspend fun setSubTrackIndexOfDir(trackId: Int, directory: SharkPlayerFile.Directory) =
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                val prefs = preferences[SUBTITLE_TRACK_INDEX]
                val newMap = hashMapOf<String, Int>()
                prefs?.let {
                    val map = trackIndexAdapter.fromJson(it)
                    if (map != null) {
                        newMap.putAll(map)
                    }
                }
                newMap[directory.path] = trackId
                preferences[SUBTITLE_TRACK_INDEX] = trackIndexAdapter.toJson(newMap)
            }
        }
}