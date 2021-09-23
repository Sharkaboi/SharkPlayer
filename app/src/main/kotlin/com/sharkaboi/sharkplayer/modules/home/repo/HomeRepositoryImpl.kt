package com.sharkaboi.sharkplayer.modules.home.repo

import com.sharkaboi.sharkplayer.common.extensions.tryCatching
import com.sharkaboi.sharkplayer.common.models.SharkPlayerFile
import com.sharkaboi.sharkplayer.common.util.TaskState
import com.sharkaboi.sharkplayer.data.datastore.DataStoreRepository
import kotlinx.coroutines.flow.Flow
import java.io.File

class HomeRepositoryImpl(
    private val dataStoreRepository: DataStoreRepository
) : HomeRepository {
    override val favorites: Flow<List<SharkPlayerFile.Directory>> =
        dataStoreRepository.favouritesDirsFlow

    override suspend fun removeFavorite(favorite: SharkPlayerFile.Directory): TaskState<Unit> =
        tryCatching {
            TaskState.Success(dataStoreRepository.removeFavorite(favorite))
        }
}