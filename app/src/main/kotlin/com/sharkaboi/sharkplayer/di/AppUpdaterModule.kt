package com.sharkaboi.sharkplayer.di

import android.content.Context
import com.sharkaboi.appupdatechecker.AppUpdateChecker
import com.sharkaboi.appupdatechecker.models.AppUpdateCheckerSource
import com.sharkaboi.sharkplayer.common.constants.AppConstants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppUpdaterModule {

    @Provides
    @Singleton
    fun provideAppUpdater(@ApplicationContext context: Context): AppUpdateChecker =
        AppUpdateChecker(
            context,
            AppUpdateCheckerSource.GithubSource(
                ownerUsername = AppConstants.githubUsername,
                repoName = AppConstants.githubRepoName
            )
        )
}