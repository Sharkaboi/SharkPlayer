package com.sharkaboi.sharkplayer.di

import com.sharkaboi.sharkplayer.data.datastore.DataStoreRepository
import com.sharkaboi.sharkplayer.modules.directory.repo.DirectoryRepository
import com.sharkaboi.sharkplayer.modules.directory.repo.FileDirectoryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@InstallIn(ViewModelComponent::class)
@Module
object DirectoryModule {

    @Provides
    @ViewModelScoped
    fun provideDirectoryRepository(dataStoreRepository: DataStoreRepository): DirectoryRepository =
        FileDirectoryRepository(dataStoreRepository)

//    @Provides
//    @ViewModelScoped
//    fun provideDirectoryRepository(
//        dataStoreRepository: DataStoreRepository,
//        contentResolver: ContentResolver
//    ): DirectoryRepository =
//        MediaStoreDirectoryRepository(dataStoreRepository, contentResolver)
}