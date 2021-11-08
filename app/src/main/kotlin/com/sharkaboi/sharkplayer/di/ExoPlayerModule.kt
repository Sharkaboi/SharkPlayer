package com.sharkaboi.sharkplayer.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@InstallIn(ActivityRetainedComponent::class)
@Module
object ExoPlayerModule {

//    @Provides
//    @ActivityRetainedScoped
//    fun provideExoPlayer(@ApplicationContext context: Context) =
//        SimpleExoPlayer.Builder(context).build()
}