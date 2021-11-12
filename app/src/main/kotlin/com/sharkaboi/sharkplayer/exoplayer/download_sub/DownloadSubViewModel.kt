package com.sharkaboi.sharkplayer.exoplayer.download_sub

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.masterwok.opensubtitlesandroid.models.OpenSubtitleItem
import com.sharkaboi.sharkplayer.SharkPlayer
import com.sharkaboi.sharkplayer.common.extensions.showToast
import com.sharkaboi.sharkplayer.common.util.TaskState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadSubViewModel
@Inject constructor(
    app: Application,
    private val downloadSubRepository: DownloadSubRepository
) : AndroidViewModel(app) {

    private val _subs = MutableLiveData<List<OpenSubtitleItem>>(emptyList())
    val subs: LiveData<List<OpenSubtitleItem>> = _subs

    fun searchSubs(text: CharSequence?) {
        if (text.isNullOrBlank()) {
            return
        }

        viewModelScope.launch {
            when (val result = downloadSubRepository.searchSubs(text.toString())) {
                is TaskState.Failure -> showToast(result.error.message)
                is TaskState.Success -> _subs.value = result.data
            }
        }
    }

    private fun showToast(message: String?) {
        getApplication<SharkPlayer>().applicationContext.showToast(message)
    }

    fun downloadSub(openSubtitleItem: OpenSubtitleItem) {
        viewModelScope.launch {
            when (val result = downloadSubRepository.downloadSub(openSubtitleItem)) {
                is TaskState.Failure -> showToast(result.error.message)
                else -> Unit
            }
        }
    }
}