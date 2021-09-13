package com.sharkaboi.sharkplayer.common.extensions

import android.content.Context
import android.os.Environment
import androidx.annotation.StringRes
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sharkaboi.sharkplayer.R
import com.sharkaboi.sharkplayer.common.models.SharkPlayerFile
import java.io.File

internal fun Context.getDefaultDirectories(): List<SharkPlayerFile.Directory> {
    // FIXME: 05-09-2021 Add logic to get all root dirs of phone (emulated, sd cards, otg)
    val rootDirectoryFile = Environment.getExternalStorageDirectory()
    val movieDirectoryFile =
        File(rootDirectoryFile.absolutePath + File.separator + Environment.DIRECTORY_MOVIES)
    val musicDirectoryFile =
        File(rootDirectoryFile.absolutePath + File.separator + Environment.DIRECTORY_MUSIC)
    val downloadDirectoryFile =
        File(rootDirectoryFile.absolutePath + File.separator + Environment.DIRECTORY_DOWNLOADS)
    return listOf(
        SharkPlayerFile.Directory(
            folderName = getString(R.string.internal_storage_hint),
            path = rootDirectoryFile.absolutePath,
            childFileCount = rootDirectoryFile.childCount,
        ),
        SharkPlayerFile.Directory(
            folderName = movieDirectoryFile.name,
            path = movieDirectoryFile.absolutePath,
            childFileCount = movieDirectoryFile.childCount,
        ),
        SharkPlayerFile.Directory(
            folderName = musicDirectoryFile.name,
            path = musicDirectoryFile.absolutePath,
            childFileCount = musicDirectoryFile.childCount,
        ),
        SharkPlayerFile.Directory(
            folderName = downloadDirectoryFile.name,
            path = downloadDirectoryFile.absolutePath,
            childFileCount = downloadDirectoryFile.childCount,
        )
    )
}

internal fun Context.showOneOpDialog(
    @StringRes titleId: Int,
    message: String? = null,
    @StringRes buttonHintId: Int? = null,
    onClick: () -> Unit = {}
) {
    MaterialAlertDialogBuilder(this)
        .setTitle(titleId)
        .setMessage(message)
        .setPositiveButton(buttonHintId ?: android.R.string.ok) { dialog, _ ->
            onClick()
            dialog.dismiss()
        }.show()
}

internal fun Context.showOneOpDialog(
    title: String,
    message: String? = null,
    @StringRes buttonHintId: Int? = null,
    onClick: () -> Unit = {}
) {
    MaterialAlertDialogBuilder(this)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(buttonHintId ?: android.R.string.ok) { dialog, _ ->
            onClick()
            dialog.dismiss()
        }.show()
}