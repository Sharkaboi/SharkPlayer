package com.sharkaboi.sharkplayer.di

import com.sharkaboi.sharkplayer.data.datastore.DataStoreRepository
import com.sharkaboi.sharkplayer.data.sharedpref.SharedPrefRepository
import com.sharkaboi.sharkplayer.exoplayer.audio.repo.AudioPlayerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object AudioPlayerModule {

    @Provides
    @ViewModelScoped
    fun provideAudioPlayerRepository(
        sharedPrefRepository: SharedPrefRepository,
        dataStoreRepository: DataStoreRepository
    ) = AudioPlayerRepository(sharedPrefRepository, dataStoreRepository)
}