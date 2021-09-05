package com.sharkaboi.sharkplayer.modules.home.repo

import com.sharkaboi.sharkplayer.common.models.SharkPlayerFile
import com.sharkaboi.sharkplayer.common.util.TaskState
import com.sharkaboi.sharkplayer.data.datastore.DataStoreRepository
import kotlinx.coroutines.flow.Flow

class HomeRepositoryImpl(
    private val dataStoreRepository: DataStoreRepository
) : HomeRepository {
    override val favorites: Flow<List<SharkPlayerFile.Directory>> =
        dataStoreRepository.favouritesDirsFlow

    override suspend fun removeFavorite(favorite: SharkPlayerFile.Directory): TaskState<Unit> {
        return try {
            TaskState.Success(dataStoreRepository.removeFavorite(favorite))
        } catch (e: Exception) {
            TaskState.Failure(e)
        }
    }
}