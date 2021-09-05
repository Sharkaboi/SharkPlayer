package com.sharkaboi.sharkplayer.common.extensions

import android.content.Context
import android.os.Environment
import com.sharkaboi.sharkplayer.common.models.SharkPlayerFile

internal fun Context.getDefaultDirectories(): List<SharkPlayerFile.Directory> {
    // FIXME: 05-09-2021 Add logic to get all root dirs of phone (emulated, sd cards, otg)
    val file = Environment.getRootDirectory()
    return listOf(
        SharkPlayerFile.Directory(
            folderName = file.name,
            path = file.absolutePath,
            childFileCount = file.list()?.size ?: 0,
        )
    )
}