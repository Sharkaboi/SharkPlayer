package com.sharkaboi.sharkplayer.modules.home.vm

import androidx.lifecycle.*
import com.sharkaboi.sharkplayer.common.models.SharkPlayerFile
import com.sharkaboi.sharkplayer.common.util.TaskState
import com.sharkaboi.sharkplayer.modules.directory.vm.setLoading
import com.sharkaboi.sharkplayer.modules.home.repo.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
@Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {
    private val _uiState = MutableLiveData<HomeState>().getDefault()
    val uiState: LiveData<HomeState> = _uiState
    val favorites = homeRepository.favorites.asLiveData()

    fun removeFavorite(favorite: SharkPlayerFile.Directory) {
        _uiState.setLoading()
        viewModelScope.launch {
            when (val result = homeRepository.removeFavorite(favorite)) {
                is TaskState.Failure -> _uiState.setError(result.error)
                is TaskState.Success -> _uiState.setIdle()
            }
        }
    }
}