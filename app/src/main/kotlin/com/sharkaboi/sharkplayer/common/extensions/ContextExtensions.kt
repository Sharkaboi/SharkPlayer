package com.sharkaboi.sharkplayer.common.extensions

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import android.view.LayoutInflater
import androidx.annotation.StringRes
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sharkaboi.sharkplayer.R
import com.sharkaboi.sharkplayer.common.models.SharkPlayerFile
import com.sharkaboi.sharkplayer.common.models.toSharkPlayerFile
import com.sharkaboi.sharkplayer.databinding.CustomTrackIndexDialogBinding
import java.io.File

internal fun Context.getDefaultDirectories(): List<SharkPlayerFile.Directory> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val volumes: List<StorageVolume> =
            this.getSystemService(StorageManager::class.java).storageVolumes
        buildList {
            volumes.forEach {
                val file = it.directory?.toSharkPlayerFile()
                if (file is SharkPlayerFile.Directory) {
                    add(file)
                }
            }
        }
    } else {
        val rootDirectoryFile = Environment.getExternalStorageDirectory()
        val movieDirectoryFile =
            File(rootDirectoryFile.absolutePath + File.separator + Environment.DIRECTORY_MOVIES)
        val musicDirectoryFile =
            File(rootDirectoryFile.absolutePath + File.separator + Environment.DIRECTORY_MUSIC)
        val downloadDirectoryFile =
            File(rootDirectoryFile.absolutePath + File.separator + Environment.DIRECTORY_DOWNLOADS)
        listOf(
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

@SuppressLint("InflateParams")
internal fun Context.showIntegerValuePromptDialog(
    title: String,
    @StringRes buttonHintId: Int? = null,
    defaultValue: Int? = null,
    onEnter: (Int) -> Unit = {}
) {
    val view = LayoutInflater
        .from(this)
        .inflate(R.layout.custom_track_index_dialog, null)
    val binding: CustomTrackIndexDialogBinding =
        CustomTrackIndexDialogBinding.bind(view)
    defaultValue?.let { binding.etValue.setText(defaultValue.toString()) }
    MaterialAlertDialogBuilder(this)
        .setTitle(title)
        .setView(binding.root)
        .setPositiveButton(buttonHintId ?: android.R.string.ok) { dialog, _ ->
            val number = binding.etValue.text?.toString()?.toIntOrNull()
            if (number == null || number < 0) {
                binding.etValue.error = getString(R.string.invalid_track_index)
            } else {
                onEnter(number)
                dialog.dismiss()
            }
        }.show()
}

internal fun Context.showIntegerValuePromptDialog(
    @StringRes titleId: Int,
    @StringRes buttonHintId: Int? = null,
    defaultValue: Int? = null,
    onEnter: (Int) -> Unit = {}
) = showIntegerValuePromptDialog(getString(titleId), buttonHintId, defaultValue, onEnter)