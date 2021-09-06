package com.sharkaboi.sharkplayer.di

import com.sharkaboi.sharkplayer.data.datastore.DataStoreRepository
import com.sharkaboi.sharkplayer.modules.directory.repo.DirectoryRepository
import com.sharkaboi.sharkplayer.modules.directory.repo.DirectoryRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@InstallIn(ViewModelComponent::class)
@Module
object DirectoryModule {

    @Provides
    fun provideDirectoryRepository(dataStoreRepository: DataStoreRepository): DirectoryRepository =
        DirectoryRepositoryImpl(dataStoreRepository)
}