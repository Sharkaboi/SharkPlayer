package com.sharkaboi.sharkplayer.di

import android.content.Context
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object FFmpegModule {

    @Provides
    @Singleton
    fun provideFFmpeg(@ApplicationContext context: Context): FFmpeg = FFmpeg.getInstance(context)
}