package com.sharkaboi.sharkplayer.modules.workers.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import com.sharkaboi.sharkplayer.SharkPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WorkersViewModel
@Inject constructor(
    app: Application
) : AndroidViewModel(app) {
    val workers: LiveData<List<WorkInfo>> =
        WorkManager.getInstance(getApplication<SharkPlayer>().applicationContext)
            .getWorkInfosLiveData(
                WorkQuery.Builder.fromStates(
                    listOf(
                        WorkInfo.State.RUNNING,
                        WorkInfo.State.ENQUEUED,
                        WorkInfo.State.SUCCEEDED
                    )
                ).build()
            ).map { it.toList() }
}