package com.sharkaboi.sharkplayer.di

import com.masterwok.opensubtitlesandroid.services.OpenSubtitlesService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@InstallIn(ActivityRetainedComponent::class)
@Module
object ExoPlayerModule {

//    @Provides
//    @ActivityRetainedScoped
//    fun provideExoPlayer(@ApplicationContext context: Context) =
//        SimpleExoPlayer.Builder(context).build()

    @Provides
    @ActivityRetainedScoped
    fun provideOpenSubsService(): OpenSubtitlesService = OpenSubtitlesService()
}