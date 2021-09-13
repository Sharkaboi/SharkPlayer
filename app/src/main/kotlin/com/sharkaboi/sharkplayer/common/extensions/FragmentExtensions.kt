package com.sharkaboi.sharkplayer.common.extensions

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import com.sharkaboi.sharkplayer.R

internal fun Fragment.showToast(message: String, length: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(context, message, length).show()

internal fun Fragment.showToast(@StringRes id: Int, length: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(context, id, length).show()

fun <T> Fragment.observe(liveData: LiveData<T>, action: (t: T) -> Unit) {
    liveData.observe(viewLifecycleOwner) { t ->
        action(t)
    }
}