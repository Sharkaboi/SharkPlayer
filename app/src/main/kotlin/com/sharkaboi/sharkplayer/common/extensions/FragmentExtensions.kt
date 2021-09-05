package com.sharkaboi.sharkplayer.common.extensions

import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData

internal fun Fragment.showToast(message: String, length: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(context, message, length).show()


fun <T> Fragment.observe(liveData: LiveData<T>, action: (t: T) -> Unit) {
    liveData.observe(viewLifecycleOwner) { t ->
        action(t)
    }
}