package com.sharkaboi.sharkplayer.modules.home.repo

import com.sharkaboi.sharkplayer.common.models.SharkPlayerFile
import com.sharkaboi.sharkplayer.common.util.TaskState
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
    val favorites: Flow<List<SharkPlayerFile.Directory>>
    suspend fun removeFavorite(favorite: SharkPlayerFile.Directory): TaskState<Unit>
}
