package com.sharkaboi.sharkplayer.common.extensions

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import com.sharkaboi.sharkplayer.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal fun AppCompatActivity.showToast(message: String?, length: Int = Toast.LENGTH_SHORT) =
    lifecycleScope.launch(Dispatchers.Main) {
        Toast.makeText(this@showToast, message, length).show()
    }

internal fun AppCompatActivity.showToast(@StringRes id: Int, length: Int = Toast.LENGTH_SHORT) =
    lifecycleScope.launch(Dispatchers.Main) {
        Toast.makeText(this@showToast, id, length).show()
    }

internal inline fun <reified T : Activity> Activity.launch(block: Intent.() -> Unit = {}) {
    val intent = Intent(this, T::class.java)
    intent.apply(block)
    startActivity(intent)
    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
}

internal inline fun <reified T : Activity> Activity.launchAndFinish(block: Intent.() -> Unit = {}) {
    val intent = Intent(this, T::class.java)
    intent.apply(block)
    startActivity(intent)
    finish()
    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
}

internal inline fun <reified T : Activity> Activity.launchAndFinishAffinity(block: Intent.() -> Unit = {}) {
    val intent = Intent(this, T::class.java)
    intent.apply(block)
    startActivity(intent)
    finishAffinity()
    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
}

fun <T> AppCompatActivity.observe(liveData: LiveData<T>, action: (t: T) -> Unit) {
    liveData.observe(this) { t ->
        action(t)
    }
}

fun Activity.openUrl(url: String) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    } catch (e: ActivityNotFoundException) {
        showToast(getString(R.string.no_browser_found_hint))
    } catch (e: Exception) {
        showToast(e.message)
    }
}