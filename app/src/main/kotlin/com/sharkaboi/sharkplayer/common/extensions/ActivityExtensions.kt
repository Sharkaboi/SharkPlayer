package com.sharkaboi.sharkplayer.common.extensions

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData

internal fun Activity.showToast(message: String, length: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, message, length).show()

internal inline fun <reified T : Activity> Activity.launch(block: Intent.() -> Unit = {}) {
    val intent = Intent(this, T::class.java)
    intent.apply(block)
    startActivity(intent)
}

internal inline fun <reified T : Activity> Activity.launchAndFinish(block: Intent.() -> Unit = {}) {
    val intent = Intent(this, T::class.java)
    intent.apply(block)
    startActivity(intent)
    finish()
}

internal inline fun <reified T : Activity> Activity.launchAndFinishAffinity(block: Intent.() -> Unit = {}) {
    val intent = Intent(this, T::class.java)
    intent.apply(block)
    startActivity(intent)
    finishAffinity()
}

fun <T> AppCompatActivity.observe(liveData: LiveData<T>, action: (t: T) -> Unit) {
    liveData.observe(this) { t ->
        action(t)
    }
}