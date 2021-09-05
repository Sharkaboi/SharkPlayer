package com.sharkaboi.sharkplayer.common.extensions

import android.content.Context
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

internal fun RecyclerView.initLinearDefaults(context: Context?, hasFixedSize: Boolean = false) {
    setHasFixedSize(hasFixedSize)
    layoutManager = LinearLayoutManager(context)
//    addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
    itemAnimator = DefaultItemAnimator()
}