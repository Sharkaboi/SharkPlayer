package com.sharkaboi.sharkplayer.di

import com.sharkaboi.sharkplayer.data.datastore.DataStoreRepository
import com.sharkaboi.sharkplayer.modules.home.repo.HomeRepository
import com.sharkaboi.sharkplayer.modules.home.repo.HomeRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@InstallIn(ViewModelComponent::class)
@Module
object HomeModule {

    @Provides
    fun provideHomeRepository(dataStoreRepository: DataStoreRepository): HomeRepository =
        HomeRepositoryImpl(dataStoreRepository)
}