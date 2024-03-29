package com.sharkaboi.sharkplayer.exoplayer.download_sub

import android.content.Context
import android.net.Uri
import android.os.Environment
import com.masterwok.opensubtitlesandroid.OpenSubtitlesUrlBuilder
import com.masterwok.opensubtitlesandroid.models.OpenSubtitleItem
import com.masterwok.opensubtitlesandroid.services.OpenSubtitlesService
import com.sharkaboi.sharkplayer.common.extensions.tryCatching
import com.sharkaboi.sharkplayer.common.util.TaskState
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class DownloadSubRepository
@Inject constructor(
    private val openSubtitlesService: OpenSubtitlesService,
    @ApplicationContext private val context: Context
) {
    suspend fun searchSubs(text: String) = tryCatching {
        val url = OpenSubtitlesUrlBuilder()
            .query(text)
            .build()
        val subs =
            openSubtitlesService.search(OpenSubtitlesService.TemporaryUserAgent, url).filter {
                it.SubFormat.equals("srt", ignoreCase = true)
            }.toList()
        TaskState.Success(subs)
    }

    suspend fun downloadSub(openSubtitleItem: OpenSubtitleItem) = tryCatching {
        val context = context.applicationContext
        val targetUri = getSubtitleSaveUri(context, openSubtitleItem)
        openSubtitlesService.downloadSubtitle(
            context,
            openSubtitleItem,
            targetUri
        )
        TaskState.Success(targetUri)
    }

    private fun getSubtitleSaveUri(context: Context?, openSubtitleItem: OpenSubtitleItem): Uri {
        return context?.let {
            val fileName = openSubtitleItem.SubFileName

            val subsFolderPath = Environment.getExternalStorageDirectory().absolutePath +
                    File.separator +
                    Environment.DIRECTORY_DOWNLOADS +
                    File.separator +
                    "Subs"
            val subsFolder = File(subsFolderPath)
            if (!subsFolder.exists()) {
                subsFolder.mkdir()
            }

            val targetUri = subsFolderPath + File.separator + fileName
            Uri.fromFile(File(targetUri))
        } ?: Uri.EMPTY
    }
}