package com.sharkaboi.sharkplayer.di

import com.sharkaboi.sharkplayer.data.sharedpref.SharedPrefRepository
import com.sharkaboi.sharkplayer.exoplayer.video.repo.VideoPlayerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object VideoPlayerModule {

    @Provides
    fun provideVideoPlayerRepository(sharedPrefRepository: SharedPrefRepository) =
        VideoPlayerRepository(sharedPrefRepository)
}