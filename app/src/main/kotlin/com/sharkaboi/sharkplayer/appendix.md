# common\constants\AppConstants.kt  
```kt
package com.sharkaboi.sharkplayer.common.constants

import android.Manifest
import android.os.Build

object AppConstants {
    val requiredPermissions: Array<String> = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
//        Manifest.permission.MEDIA_CONTENT_CONTROL,
//        Manifest.permission.MODIFY_AUDIO_SETTINGS,
        *sdkSpecificPermissions
    )
    private val sdkSpecificPermissions: Array<String>
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                arrayOf(
//                    Manifest.permission.MANAGE_MEDIA,
//                    Manifest.permission.MANAGE_MEDIA,
                    Manifest.permission.MANAGE_EXTERNAL_STORAGE
                )
            } else {
                return emptyArray()
            }
        }
    const val githubUsername = "Sharkaboi"
    const val githubRepoName = "SharkPlayer"
    const val githubLink = "https://github.com/$githubUsername/$githubRepoName"
}
```  
# common\extensions\ActivityExtensions.kt  
```kt
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
```  
# common\extensions\BooleanExtensions.kt  
```kt
package com.sharkaboi.sharkplayer.common.extensions

internal fun Boolean.toInt(): Int {
    return if (this) 1 else 0
}
```  
# common\extensions\ContextExtensions.kt  
```kt
package com.sharkaboi.sharkplayer.common.extensions

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import android.view.LayoutInflater
import android.widget.Toast
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
                showToast(R.string.invalid_track_index)
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

internal fun Context.showToast(message: String?, length: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, message, length).show()

internal fun Context.showToast(@StringRes id: Int, length: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, id, length).show()
```  
# common\extensions\CoroutineExtensions.kt  
```kt
package com.sharkaboi.sharkplayer.common.extensions

import com.sharkaboi.sharkplayer.common.util.TaskState
import kotlinx.coroutines.*
import timber.log.Timber

suspend fun <T : Any> tryCatching(
    block: suspend () -> TaskState<T>
): TaskState<T> {
    return withContext(Dispatchers.IO) {
        try {
            block()
        } catch (e: Exception) {
            Timber.e(e)
            return@withContext TaskState.Failure<T>(e)
        }
    }
}

fun <T> debounce(
    delay: Long = 800L,
    scope: CoroutineScope,
    callback: (T?) -> Unit
): (T?) -> Unit {
    var debounceJob: Job? = null
    return { param: T? ->
        debounceJob?.cancel()
        debounceJob = scope.launch {
            delay(delay)
            callback(param)
        }
    }
}
```  
# common\extensions\CursorExtensions.kt  
```kt
package com.sharkaboi.sharkplayer.common.extensions

import android.database.Cursor
import androidx.core.database.getStringOrNull

fun Cursor.getStringOfColumnName(name: String): String {
    return try {
        this.getStringOrNull(this.getColumnIndexOrThrow(name)) ?: String.emptyString
    } catch (e: Exception) {
        String.emptyString
    }
}

fun Cursor.getLongOfColumnName(name: String): Long {
    return try {
        this.getStringOrNull(this.getColumnIndexOrThrow(name)) ?: "0"
    } catch (e: Exception) {
        "0"
    }.toLong()
}
```  
# common\extensions\DurationExtensions.kt  
```kt
package com.sharkaboi.sharkplayer.common.extensions

import kotlin.time.Duration

fun Duration.getTimeString(): String {
    if (!this.isPositive()) {
        return "0s"
    }
    val hours = this.inWholeHours.toInt()
    val minutes = this.minus(Duration.hours(hours)).inWholeMinutes
    val seconds = this.minus(Duration.minutes(minutes)).inWholeSeconds
    return buildString {
        append(if (hours <= 0) String.emptyString else "${hours}hr")
        append(" ")
        append(if (minutes <= 0) String.emptyString else "${minutes}min")
        append(" ")
        append(if (seconds <= 0 || minutes > 0 || hours > 0) String.emptyString else "${seconds}s")
    }.trim()
}
```  
# common\extensions\ExoPlayerExtensions.kt  
```kt
package com.sharkaboi.sharkplayer.common.extensions

import android.net.Uri
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem

internal fun ExoPlayer.setVideosAsPlayList(mediaItems: List<MediaItem>) {
    clearMediaItems()
    setMediaItems(mediaItems, true)
}

internal fun ExoPlayer.setAudio(audioUri: Uri) {
    setMediaItem(MediaItem.fromUri(audioUri))
}
```  
# common\extensions\FileExtensions.kt  
```kt
package com.sharkaboi.sharkplayer.common.extensions

import android.media.MediaMetadataRetriever
import java.io.File
import kotlin.time.Duration

internal const val SIZE_NOT_A_FILE = -1L

internal val File.childCount: Int
    get() = this.list()?.size ?: 0

internal val File.isVideoFile: Boolean
    get() {
        val supportedFileRegex = "mp4|webm|mkv|flv".toRegex(RegexOption.IGNORE_CASE)
        return this.isFile && this.extension.matches(supportedFileRegex)
    }

internal val File.isAudioFile: Boolean
    get() {
        val supportedFileRegex = "m4a|mp3|ogg|wav|flac|opus".toRegex(RegexOption.IGNORE_CASE)
        return this.isFile && this.extension.matches(supportedFileRegex)
    }

internal val File.fileSize: Long
    get() = when {
        this.isFile -> this.length()
        else -> SIZE_NOT_A_FILE
    }

internal val File.videoOrAudioLength: Duration
    get() = runCatching {
        val metadataRetriever = MediaMetadataRetriever()
        metadataRetriever.setDataSource(absolutePath)
        val lengthInMilliSeconds =
            metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLong() ?: 0L
        metadataRetriever.release()
        return Duration.milliseconds(lengthInMilliSeconds)
    }.getOrElse { Duration.ZERO }

internal val File.videoResolution: Pair<Int, Int>
    get() = runCatching {
        val metadataRetriever = MediaMetadataRetriever()
        metadataRetriever.setDataSource(absolutePath)
        val videoHeight =
            metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                ?.toInt() ?: 0
        val videoWidth =
            metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                ?.toInt() ?: 0
        metadataRetriever.release()
        return Pair(videoHeight, videoWidth)
    }.getOrElse { Pair(0, 0) }

internal val File.audioBitrate: Long
    get() = runCatching {
        val metadataRetriever = MediaMetadataRetriever()
        metadataRetriever.setDataSource(absolutePath)
        val bitsPerSecond =
            metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
                ?.toLong() ?: 0L
        metadataRetriever.release()
        return bitsPerSecond / 1024
    }.getOrElse { 0L }


```  
# common\extensions\FloatExtensions.kt  
```kt
package com.sharkaboi.sharkplayer.common.extensions

import kotlin.math.roundToInt

internal fun Float.nextEvenInt(): Int {
    return this.roundToInt().nextEven()
}
```  
# common\extensions\FragmentExtensions.kt  
```kt
package com.sharkaboi.sharkplayer.common.extensions

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import com.sharkaboi.sharkplayer.R

internal fun Fragment.showToast(message: String?, length: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(context, message, length).show()

internal fun Fragment.showToast(@StringRes id: Int, length: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(context, id, length).show()

fun <T> Fragment.observe(liveData: LiveData<T>, action: (t: T) -> Unit) {
    liveData.observe(viewLifecycleOwner) { t ->
        action(t)
    }
}

fun Fragment.openUrl(url: String) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
        activity?.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    } catch (e: ActivityNotFoundException) {
        showToast(getString(R.string.no_browser_found_hint))
    } catch (e: Exception) {
        showToast(e.message)
    }
}
```  
# common\extensions\ImageViewExtensions.kt  
```kt
package com.sharkaboi.sharkplayer.common.extensions

import android.widget.ImageView
import coil.load
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.sharkaboi.sharkplayer.common.models.SharkPlayerFile

fun ImageView.setThumbnailOf(
    videoFile: SharkPlayerFile.VideoFile,
    block: ImageRequest.Builder.() -> Unit = {}
) {
    this.load(videoFile.getFile()) {
        videoFrameMillis(videoFile.length.inWholeMilliseconds / 2)
        block()
    }
}
```  
# common\extensions\IntExtensions.kt  
```kt
package com.sharkaboi.sharkplayer.common.extensions

internal fun Int.nextEven(): Int {
    return this + (this % 2 != 0).toInt()
}
```  
# common\extensions\LongExtensions.kt  
```kt
package com.sharkaboi.sharkplayer.common.extensions

import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

internal fun Long.getSizeString(): String {
    if (this <= 0) return "0B"

    val units = listOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(this.toDouble()) / log10(1024.0)).toInt()

    val formatter = DecimalFormat("#,##0.#")
    val rawValue = this / 1024.0.pow(digitGroups.toDouble())

    return "${formatter.format(rawValue)} ${units[digitGroups]}"
}
```  
# common\extensions\RecyclerViewExtensions.kt  
```kt
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
```  
# common\extensions\StringExtensions.kt  
```kt
package com.sharkaboi.sharkplayer.common.extensions

internal val String.Companion.emptyString: String
    get() = ""
```  
# common\models\SharkPlayerFile.kt  
```kt
package com.sharkaboi.sharkplayer.common.models

import android.os.Environment
import com.sharkaboi.sharkplayer.common.extensions.*
import java.io.File
import kotlin.time.Duration

sealed class SharkPlayerFile {
    data class Directory(
        val folderName: String,
        val path: String,
        val childFileCount: Int
    ) : SharkPlayerFile()

    data class VideoFile(
        val fileName: String,
        val path: String,
        val length: Duration,
        val size: Long,
        val videoWidth: Int,
        val videoHeight: Int,
    ) : SharkPlayerFile() {
        val resolution: String
            get() = "$videoWidth x $videoHeight"

        val isDirty: Boolean
            get() = path.isBlank()
                    || size <= 0L
                    || !length.isPositive()
                    || size <= 0L
                    || videoWidth <= 0L
                    || videoHeight <= 0L
    }

    data class AudioFile(
        val fileName: String,
        val path: String,
        val size: Long,
        val length: Duration,
        val bitRate: Long
    ) : SharkPlayerFile() {

        val quality: String
            get() = "$bitRate kbps"

        val isDirty: Boolean
            get() = path.isBlank()
                    || size <= 0L
                    || !length.isPositive()
                    || bitRate <= 0L
    }

    data class OtherFile(
        val fileName: String,
        val path: String,
        val size: Long
    ) : SharkPlayerFile()

    val absolutePath: String
        get() {
            return when (this) {
                is Directory -> this.path
                is VideoFile -> this.path
                is AudioFile -> this.path
                is OtherFile -> this.path
            }
        }

    val sortField: String
        get() {
            return when (this) {
                is Directory -> this.folderName.lowercase()
                is VideoFile -> this.fileName.lowercase()
                is AudioFile -> this.fileName.lowercase()
                is OtherFile -> this.fileName.lowercase()
            }
        }

    fun getFile(): File {
        return when (this) {
            is Directory -> File(this.path)
            is VideoFile -> File(this.path)
            is AudioFile -> File(this.path)
            is OtherFile -> File(this.path)
        }
    }

    val parentPath
        get() = this.absolutePath.substringBeforeLast(File.separator)

    companion object {
        fun directoryFromPath(path: String?): Directory {
            val requiredPath = path ?: Environment.getRootDirectory().absolutePath
            return File(requiredPath).toSharkPlayerDirectory()
        }
    }
}

internal fun File.toSharkPlayerFile(): SharkPlayerFile {
    return when {
        this.isDirectory -> this.toSharkPlayerDirectory()
        this.isVideoFile -> this.toSharkPlayerVideoFile()
        this.isAudioFile -> this.toSharkPlayerAudioFile()
        else -> this.toSharkPlayerOtherFile()
    }
}

private fun File.toSharkPlayerDirectory(): SharkPlayerFile.Directory {
    return SharkPlayerFile.Directory(
        folderName = this.name,
        path = this.absolutePath,
        childFileCount = this.childCount
    )
}

private fun File.toSharkPlayerVideoFile(): SharkPlayerFile.VideoFile {
    val (height, width) = this.videoResolution
    return SharkPlayerFile.VideoFile(
        fileName = this.nameWithoutExtension,
        path = this.absolutePath,
        videoHeight = height,
        videoWidth = width,
        length = this.videoOrAudioLength,
        size = this.fileSize
    )
}

private fun File.toSharkPlayerAudioFile(): SharkPlayerFile.AudioFile {
    return SharkPlayerFile.AudioFile(
        fileName = this.nameWithoutExtension,
        path = this.absolutePath,
        bitRate = this.audioBitrate,
        length = this.videoOrAudioLength,
        size = this.fileSize
    )
}

private fun File.toSharkPlayerOtherFile(): SharkPlayerFile.OtherFile {
    return SharkPlayerFile.OtherFile(
        fileName = this.name,
        path = this.absolutePath,
        size = this.fileSize
    )
}
```  
# common\util\TaskState.kt  
```kt
package com.sharkaboi.sharkplayer.common.util

sealed class TaskState<T : Any> {
    data class Success<T : Any>(val data: T) : TaskState<T>()
    data class Failure<T : Any>(val error: Exception) : TaskState<T>()

    fun toKotlinResult(): Result<T> {
        return when (this) {
            is Failure -> Result.failure(error)
            is Success -> Result.success(data)
        }
    }

    val isSuccess get() = this is Success

    val isFailure get() = this is Failure

    companion object {
        fun <T : Any> failureWithMessage(message: String): Failure<T> {
            return Failure(Exception(message))
        }
    }
}


```  
# data\datastore\DataStoreRepository.kt  
```kt
package com.sharkaboi.sharkplayer.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.sharkaboi.sharkplayer.common.extensions.childCount
import com.sharkaboi.sharkplayer.common.models.SharkPlayerFile
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

private const val PREFERENCES_NAME = "SharkPlayerDataStore"
internal val Context.dataStore by preferencesDataStore(
    name = PREFERENCES_NAME
)
private val FAVORITES_KEY = stringSetPreferencesKey("favourite_directories")
private val SUBTITLE_TRACK_INDEX = stringPreferencesKey("subtitle_track_index")
private val AUDIO_TRACK_INDEX = stringPreferencesKey("audio_track_index")

class DataStoreRepository(
    private val dataStore: DataStore<Preferences>,
    private val moshi: Moshi
) {
    private val trackIndexType =
        Types.newParameterizedType(Map::class.java, String::class.java, Integer::class.java)
    private val trackIndexAdapter: JsonAdapter<Map<String, Int>> =
        moshi.adapter<Map<String, Int>>(trackIndexType).nonNull()

    val favouritesDirsFlow: Flow<List<SharkPlayerFile.Directory>> =
        dataStore.data.catch { exception ->
            Timber.w(exception)
            emit(emptyPreferences())
        }.map { preferences ->
            preferences[FAVORITES_KEY]?.map { path ->
                val file = File(path)
                SharkPlayerFile.Directory(
                    folderName = file.nameWithoutExtension,
                    path = path,
                    childFileCount = file.childCount
                )
            } ?: emptyList()
        }.flowOn(Dispatchers.IO)

    val subtitleTrackIndices: Flow<Map<String, Int>> =
        dataStore.data.catch { exception ->
            Timber.w(exception)
            emit(emptyPreferences())
        }.map { preferences ->
            preferences[SUBTITLE_TRACK_INDEX]?.let { json ->
                trackIndexAdapter.fromJson(json)
            } ?: hashMapOf()
        }.flowOn(Dispatchers.IO)

    val audioTrackIndices: Flow<Map<String, Int>> =
        dataStore.data.catch { exception ->
            Timber.w(exception)
            emit(emptyPreferences())
        }.map { preferences ->
            preferences[AUDIO_TRACK_INDEX]?.let { json ->
                trackIndexAdapter.fromJson(json)
            } ?: hashMapOf()
        }.flowOn(Dispatchers.IO)

    suspend fun removeFavorite(favorite: SharkPlayerFile.Directory): Unit =
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                val oldSet: Set<String>? = preferences[FAVORITES_KEY]
                val newSet: MutableSet<String> = oldSet?.toMutableSet() ?: mutableSetOf()
                newSet.remove(favorite.path)
                preferences[FAVORITES_KEY] = newSet
            }
        }

    suspend fun addFavorite(favorite: SharkPlayerFile.Directory): Unit =
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                val oldSet: Set<String>? = preferences[FAVORITES_KEY]
                val newSet: MutableSet<String> = oldSet?.toMutableSet() ?: mutableSetOf()
                newSet.add(favorite.path)
                preferences[FAVORITES_KEY] = newSet
            }
        }

    suspend fun setAudioTrackIndexOfDir(trackId: Int, directory: SharkPlayerFile.Directory) =
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                val prefs = preferences[AUDIO_TRACK_INDEX]
                val newMap = hashMapOf<String, Int>()
                prefs?.let {
                    val map = trackIndexAdapter.fromJson(it)
                    if (map != null) {
                        newMap.putAll(map)
                    }
                }
                newMap[directory.path] = trackId
                preferences[AUDIO_TRACK_INDEX] = trackIndexAdapter.toJson(newMap)
            }
        }

    suspend fun setSubTrackIndexOfDir(trackId: Int, directory: SharkPlayerFile.Directory) =
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                val prefs = preferences[SUBTITLE_TRACK_INDEX]
                val newMap = hashMapOf<String, Int>()
                prefs?.let {
                    val map = trackIndexAdapter.fromJson(it)
                    if (map != null) {
                        newMap.putAll(map)
                    }
                }
                newMap[directory.path] = trackId
                preferences[SUBTITLE_TRACK_INDEX] = trackIndexAdapter.toJson(newMap)
            }
        }
}
```  
# data\sharedpref\SharedPrefKeys.kt  
```kt
package com.sharkaboi.sharkplayer.data.sharedpref

object SharedPrefKeys {
    const val DARK_THEME = "darkTheme"
    const val SUBTITLE_LANGUAGE = "subtitleLanguage"
    const val AUDIO_LANGUAGE = "audioLanguage"
    const val START_PAUSED_VIDEO = "startPausedVideo"
    const val START_PAUSED_AUDIO = "startPausedAudio"
    const val UPDATES = "updates"
    const val ABOUT = "about"
}
```  
# data\sharedpref\SharedPrefRepository.kt  
```kt
package com.sharkaboi.sharkplayer.data.sharedpref

import android.content.SharedPreferences
import com.sharkaboi.sharkplayer.common.extensions.emptyString

class SharedPrefRepository(
    private val sharedPreferences: SharedPreferences
) {

    fun isDarkTheme() = sharedPreferences.getBoolean(SharedPrefKeys.DARK_THEME, false)

    fun getSubtitleLanguages() =
        sharedPreferences.getString(SharedPrefKeys.SUBTITLE_LANGUAGE, String.emptyString)

    fun getAudioLanguages() =
        sharedPreferences.getString(SharedPrefKeys.AUDIO_LANGUAGE, String.emptyString)

    fun shouldStartVideoPaused() =
        sharedPreferences.getBoolean(SharedPrefKeys.START_PAUSED_VIDEO, false)

    fun shouldStartAudioPaused() =
        sharedPreferences.getBoolean(SharedPrefKeys.START_PAUSED_AUDIO, false)
}
```  
# di\AppUpdaterModule.kt  
```kt
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
```  
# di\AudioPlayerModule.kt  
```kt
package com.sharkaboi.sharkplayer.di

import com.sharkaboi.sharkplayer.data.datastore.DataStoreRepository
import com.sharkaboi.sharkplayer.data.sharedpref.SharedPrefRepository
import com.sharkaboi.sharkplayer.exoplayer.audio.repo.AudioPlayerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object AudioPlayerModule {

    @Provides
    @ViewModelScoped
    fun provideAudioPlayerRepository(
        sharedPrefRepository: SharedPrefRepository,
        dataStoreRepository: DataStoreRepository
    ) = AudioPlayerRepository(sharedPrefRepository, dataStoreRepository)
}
```  
# di\CoilModule.kt  
```kt
package com.sharkaboi.sharkplayer.di

import android.content.Context
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import coil.fetch.VideoFrameFileFetcher
import coil.fetch.VideoFrameUriFetcher
import coil.util.DebugLogger
import com.sharkaboi.sharkplayer.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoilModule {

    @Provides
    @Singleton
    fun provideCoilImageLoader(@ApplicationContext context: Context): ImageLoader =
        ImageLoader.Builder(context)
            .crossfade(true)
            .availableMemoryPercentage(0.25)
            .componentRegistry {
                add(VideoFrameFileFetcher(context))
                add(VideoFrameUriFetcher(context))
                add(VideoFrameDecoder(context))
            }.apply {
//                if (BuildConfig.DEBUG) {
//                    logger(DebugLogger())
//                }
            }.build()
}
```  
# di\DataModule.kt  
```kt
package com.sharkaboi.sharkplayer.di

import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.preference.PreferenceManager
import com.sharkaboi.sharkplayer.data.datastore.DataStoreRepository
import com.sharkaboi.sharkplayer.data.datastore.dataStore
import com.sharkaboi.sharkplayer.data.sharedpref.SharedPrefRepository
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DataModule {

    @Provides
    @Singleton
    fun provideSharedPrefs(@ApplicationContext context: Context): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    @Provides
    @Singleton
    fun provideSharedPrefRepository(sharedPreferences: SharedPreferences): SharedPrefRepository =
        SharedPrefRepository(sharedPreferences)

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.dataStore

    @Provides
    @Singleton
    fun provideDataStoreRepository(
        dataStore: DataStore<Preferences>,
        moshi: Moshi
    ): DataStoreRepository =
        DataStoreRepository(dataStore, moshi)

    @Provides
    @Singleton
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver =
        context.contentResolver

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder().build()
}
```  
# di\DirectoryModule.kt  
```kt
package com.sharkaboi.sharkplayer.di

import com.sharkaboi.sharkplayer.data.datastore.DataStoreRepository
import com.sharkaboi.sharkplayer.modules.directory.repo.DirectoryRepository
import com.sharkaboi.sharkplayer.modules.directory.repo.FileDirectoryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@InstallIn(ViewModelComponent::class)
@Module
object DirectoryModule {

    @Provides
    fun provideDirectoryRepository(dataStoreRepository: DataStoreRepository): DirectoryRepository =
        FileDirectoryRepository(dataStoreRepository)

//    @Provides
//    @ViewModelScoped
//    fun provideDirectoryRepository(
//        dataStoreRepository: DataStoreRepository,
//        contentResolver: ContentResolver
//    ): DirectoryRepository =
//        MediaStoreDirectoryRepository(dataStoreRepository, contentResolver)
}
```  
# di\ExoPlayerModule.kt  
```kt
package com.sharkaboi.sharkplayer.di

import com.masterwok.opensubtitlesandroid.services.OpenSubtitlesService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@InstallIn(ActivityRetainedComponent::class)
@Module
object ExoPlayerModule {

//    @Provides
//    @ActivityRetainedScoped
//    fun provideExoPlayer(@ApplicationContext context: Context) =
//        SimpleExoPlayer.Builder(context).build()

    @Provides
    @ActivityRetainedScoped
    fun provideOpenSubsService(): OpenSubtitlesService = OpenSubtitlesService()
}
```  
# di\FFmpegModule.kt  
```kt
package com.sharkaboi.sharkplayer.di

import android.content.Context
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.sharkaboi.sharkplayer.ffmpeg.FFMpegDataSource
import com.sharkaboi.sharkplayer.ffmpeg.sources.AndroidFFmpegDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object FFmpegModule {

    @Provides
    @Singleton
    fun provideFFmpeg(@ApplicationContext context: Context): FFmpeg = FFmpeg.getInstance(context)

    @Provides
    @Singleton
    fun provideFFmpegDataSource(fFmpeg: FFmpeg): FFMpegDataSource = AndroidFFmpegDataSource(fFmpeg)
}
```  
# di\HomeModule.kt  
```kt
package com.sharkaboi.sharkplayer.di

import com.sharkaboi.sharkplayer.data.datastore.DataStoreRepository
import com.sharkaboi.sharkplayer.modules.home.repo.HomeRepository
import com.sharkaboi.sharkplayer.modules.home.repo.HomeRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@InstallIn(ViewModelComponent::class)
@Module
object HomeModule {

    @Provides
    @ViewModelScoped
    fun provideHomeRepository(dataStoreRepository: DataStoreRepository): HomeRepository =
        HomeRepositoryImpl(dataStoreRepository)
}
```  
# di\VideoPlayerModule.kt  
```kt
package com.sharkaboi.sharkplayer.di

import com.sharkaboi.sharkplayer.data.datastore.DataStoreRepository
import com.sharkaboi.sharkplayer.data.sharedpref.SharedPrefRepository
import com.sharkaboi.sharkplayer.exoplayer.video.repo.VideoPlayerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object VideoPlayerModule {

    @Provides
    @ViewModelScoped
    fun provideVideoPlayerRepository(
        sharedPrefRepository: SharedPrefRepository,
        dataStoreRepository: DataStoreRepository
    ) = VideoPlayerRepository(sharedPrefRepository, dataStoreRepository)
}
```  
# exoplayer\audio\model\AudioInfo.kt  
```kt
package com.sharkaboi.sharkplayer.exoplayer.audio.model

import android.net.Uri

data class AudioInfo(
    val name: String,
    val audioUri: Uri,
    val playWhenReady: Boolean
)

```  
# exoplayer\audio\model\AudioNavArgs.kt  
```kt
package com.sharkaboi.sharkplayer.exoplayer.audio.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AudioNavArgs(
    val dirPath: String,
    val videoPaths: List<String>,
) : Parcelable

```  
# exoplayer\audio\repo\AudioPlayerRepository.kt  
```kt
package com.sharkaboi.sharkplayer.exoplayer.audio.repo

import androidx.core.net.toUri
import com.sharkaboi.sharkplayer.common.extensions.tryCatching
import com.sharkaboi.sharkplayer.common.util.TaskState
import com.sharkaboi.sharkplayer.data.datastore.DataStoreRepository
import com.sharkaboi.sharkplayer.data.sharedpref.SharedPrefRepository
import com.sharkaboi.sharkplayer.exoplayer.audio.model.AudioInfo
import java.io.File

class AudioPlayerRepository(
    private val sharedPrefRepository: SharedPrefRepository,
    private val dataStoreRepository: DataStoreRepository,
) {
    suspend fun getMetaDataOf(audioPath: String): TaskState<AudioInfo> =
        tryCatching {
            val playWhenReady = sharedPrefRepository.shouldStartAudioPaused().not()
            TaskState.Success(
                AudioInfo(
                    name = File(audioPath).nameWithoutExtension,
                    audioUri = audioPath.toUri(),
                    playWhenReady = playWhenReady
                )
            )
        }
}

```  
# exoplayer\audio\ui\AudioPlayerActivity.kt  
```kt
package com.sharkaboi.sharkplayer.exoplayer.audio.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navArgs
import com.google.android.exoplayer2.ExoPlayer
import com.sharkaboi.sharkplayer.common.extensions.observe
import com.sharkaboi.sharkplayer.common.extensions.setAudio
import com.sharkaboi.sharkplayer.common.extensions.showToast
import com.sharkaboi.sharkplayer.databinding.ActivityAudioPlayerBinding
import com.sharkaboi.sharkplayer.exoplayer.audio.model.AudioInfo
import com.sharkaboi.sharkplayer.exoplayer.audio.vm.AudioPlayerState
import com.sharkaboi.sharkplayer.exoplayer.audio.vm.AudioPlayerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class AudioPlayerActivity : AppCompatActivity() {
    private val args: AudioPlayerActivityArgs by navArgs()
    private lateinit var binding: ActivityAudioPlayerBinding
    private var player: ExoPlayer? = null
    private val audioPlayerViewModel by viewModels<AudioPlayerViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudioPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
        setObservers()
        initViews()
        setObservers()
    }

    private fun initViews() {
        //Nothing
    }

    private fun setObservers() {
        observe(audioPlayerViewModel.uiState) { state ->
            binding.progress.isVisible = state is AudioPlayerState.Loading
            when (state) {
                is AudioPlayerState.InvalidData -> showToast(state.message)
                is AudioPlayerState.Success -> {
                    handleMetaDataUpdate(state.audioInfo)
                }
                else -> Unit
            }
        }
    }

    private fun handleMetaDataUpdate(audioInfo: AudioInfo) =
        lifecycleScope.launch(Dispatchers.Main) {
            Timber.d("called")
            resetPlayer()
            player = ExoPlayer.Builder(this@AudioPlayerActivity).build()
            binding.playerView.player = player
            player?.setAudio(audioInfo.audioUri)
            player?.prepare()
//            player?.addAnalyticsListener(EventLogger(player?.trackSelector as MappingTrackSelector?))
            player?.playWhenReady = audioInfo.playWhenReady
            binding.tvFileName.isSelected = true
            binding.tvFileName.text = audioInfo.name
        }

    private fun resetPlayer() {
        binding.playerView.player = null
        player?.release()
        player?.cleanErrorCallback()
    }

    override fun onDestroy() {
        resetPlayer()
        super.onDestroy()
    }
}
```  
# exoplayer\audio\vm\AudioPlayerState.kt  
```kt
package com.sharkaboi.sharkplayer.exoplayer.audio.vm

import androidx.lifecycle.MutableLiveData
import com.sharkaboi.sharkplayer.common.extensions.emptyString
import com.sharkaboi.sharkplayer.exoplayer.audio.model.AudioInfo

sealed class AudioPlayerState {
    object Idle : AudioPlayerState()

    object Loading : AudioPlayerState()

    data class InvalidData(
        val message: String
    ) : AudioPlayerState()

    data class Success(
        val audioInfo: AudioInfo
    ) : AudioPlayerState()
}

internal fun MutableLiveData<AudioPlayerState>.setSuccess(
    audioInfo: AudioInfo
) {
    this.value = AudioPlayerState.Success(audioInfo)
}

internal fun MutableLiveData<AudioPlayerState>.setInvalidData(message: String) {
    this.value = AudioPlayerState.InvalidData(message)
}

internal fun MutableLiveData<AudioPlayerState>.setError(exception: Exception) {
    this.value = AudioPlayerState.InvalidData(exception.message ?: String.emptyString)
}

internal fun MutableLiveData<AudioPlayerState>.setIdle() {
    this.value = AudioPlayerState.Idle
}

internal fun MutableLiveData<AudioPlayerState>.setLoading() {
    this.value = AudioPlayerState.Loading
}

internal fun MutableLiveData<AudioPlayerState>.getDefault() = this.apply {
    this.setIdle()
}


```  
# exoplayer\audio\vm\AudioPlayerViewModel.kt  
```kt
package com.sharkaboi.sharkplayer.exoplayer.audio.vm

import androidx.lifecycle.*
import com.sharkaboi.sharkplayer.common.util.TaskState
import com.sharkaboi.sharkplayer.exoplayer.audio.repo.AudioPlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AudioPlayerViewModel
@Inject
constructor(
    savedStateHandle: SavedStateHandle,
    private val audioPlayerRepository: AudioPlayerRepository
) : ViewModel() {
    private val audioPath = savedStateHandle.get<String>(AUDIO_NAV_ARGS_KEY)
    private val _uiState = MutableLiveData<AudioPlayerState>().getDefault()
    val uiState: LiveData<AudioPlayerState> = _uiState

    init {
        if (audioPath == null || audioPath.isBlank()) {
            _uiState.setInvalidData("Path was null")
        } else {
            loadVideoMetadata(audioPath)
        }
    }

    private fun loadVideoMetadata(audioPath: String) {
        Timber.d("Vm called")
        _uiState.setLoading()
        viewModelScope.launch {
            when (val result = audioPlayerRepository.getMetaDataOf(audioPath)) {
                is TaskState.Failure -> _uiState.setError(result.error)
                is TaskState.Success -> _uiState.setSuccess(result.data)
            }
        }
    }

    companion object {
        const val AUDIO_NAV_ARGS_KEY = "path"
    }
}
```  
# exoplayer\download_sub\DownloadSubDialog.kt  
```kt
package com.sharkaboi.sharkplayer.exoplayer.download_sub

import android.app.Dialog
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sharkaboi.sharkplayer.common.extensions.debounce
import com.sharkaboi.sharkplayer.common.extensions.initLinearDefaults
import com.sharkaboi.sharkplayer.common.extensions.observe
import com.sharkaboi.sharkplayer.databinding.DialogDownloadSubBinding
import com.sharkaboi.sharkplayer.exoplayer.video.vm.VideoPlayerViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class DownloadSubDialog : BottomSheetDialogFragment() {
    private lateinit var adapter: DownloadSubsAdapter
    private var _binding: DialogDownloadSubBinding? = null
    private val binding get() = _binding!!
    private val downloadSubViewModel by viewModels<DownloadSubViewModel>()
    private val videoPlayerViewModel by activityViewModels<VideoPlayerViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        return dialog
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val parentLayout =
            dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        val layoutParams = parentLayout?.layoutParams
        layoutParams?.width = WindowManager.LayoutParams.MATCH_PARENT
        parentLayout?.layoutParams = layoutParams
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogDownloadSubBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
        setObservers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvSubs.adapter = null
        _binding = null
    }

    private fun setListeners() {
        val debounce = debounce<CharSequence?>(
            scope = lifecycleScope,
            delay = 500L
        ) {
            downloadSubViewModel.searchSubs(it)
        }
        binding.etDownloadSub.doOnTextChanged { text, _, _, _ ->
            debounce(text)
        }
    }

    private fun setObservers() {
        observe(downloadSubViewModel.subs) { list ->
            val rvSubs = binding.rvSubs
            adapter = DownloadSubsAdapter {
                downloadSubViewModel.downloadSub(it)
            }
            rvSubs.adapter = adapter
            rvSubs.initLinearDefaults(context)
            adapter.submitList(list)
        }
        observe(downloadSubViewModel.downloadedSubUri) { uri ->
            if (uri == null) {
                return@observe
            }

            Timber.d(videoPlayerViewModel.toString())
            videoPlayerViewModel.setDownloadedSubUri(uri)
            dismiss()
        }
    }
}
```  
# exoplayer\download_sub\DownloadSubRepository.kt  
```kt
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
```  
# exoplayer\download_sub\DownloadSubsAdapter.kt  
```kt
package com.sharkaboi.sharkplayer.exoplayer.download_sub

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.masterwok.opensubtitlesandroid.models.OpenSubtitleItem
import com.sharkaboi.sharkplayer.databinding.ItemDownloadSubBinding

class DownloadSubsAdapter(private val onClick: (OpenSubtitleItem) -> Unit) :
    RecyclerView.Adapter<DownloadSubsAdapter.DownloadSubsViewHolder>() {

    private val diffUtilItemCallback = object : DiffUtil.ItemCallback<OpenSubtitleItem>() {
        override fun areItemsTheSame(
            oldItem: OpenSubtitleItem,
            newItem: OpenSubtitleItem
        ): Boolean {
            return oldItem.IDSubtitle == newItem.IDSubtitle
        }

        override fun areContentsTheSame(
            oldItem: OpenSubtitleItem,
            newItem: OpenSubtitleItem
        ): Boolean {
            return oldItem == newItem
        }

    }

    private val listDiffer = AsyncListDiffer(this, diffUtilItemCallback)

    private lateinit var binding: ItemDownloadSubBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadSubsViewHolder {
        binding = ItemDownloadSubBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DownloadSubsViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: DownloadSubsViewHolder, position: Int) {
        holder.bind(listDiffer.currentList[position])
    }

    override fun getItemCount(): Int {
        return listDiffer.currentList.size
    }

    fun submitList(list: List<OpenSubtitleItem>) {
        listDiffer.submitList(list)
    }

    class DownloadSubsViewHolder
    constructor(
        private val binding: ItemDownloadSubBinding,
        private val onClick: (OpenSubtitleItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: OpenSubtitleItem) {
            binding.root.setOnClickListener {
                onClick(item)
            }
            binding.tvTitle.text = item.SubFileName
            binding.tvSubtitle.text = item.MovieName
        }
    }
}

```  
# exoplayer\download_sub\DownloadSubViewModel.kt  
```kt
package com.sharkaboi.sharkplayer.exoplayer.download_sub

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.masterwok.opensubtitlesandroid.models.OpenSubtitleItem
import com.sharkaboi.sharkplayer.SharkPlayer
import com.sharkaboi.sharkplayer.common.extensions.showToast
import com.sharkaboi.sharkplayer.common.util.TaskState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadSubViewModel
@Inject constructor(
    app: Application,
    private val downloadSubRepository: DownloadSubRepository
) : AndroidViewModel(app) {

    private val _subs = MutableLiveData<List<OpenSubtitleItem>>(emptyList())
    val subs: LiveData<List<OpenSubtitleItem>> = _subs

    private val _downloadedSubUri = MutableLiveData<Uri?>(null)
    val downloadedSubUri: LiveData<Uri?> = _downloadedSubUri

    fun searchSubs(text: CharSequence?) {
        if (text.isNullOrBlank()) {
            return
        }

        viewModelScope.launch {
            when (val result = downloadSubRepository.searchSubs(text.toString())) {
                is TaskState.Failure -> showToast(result.error.message)
                is TaskState.Success -> _subs.value = result.data
            }
        }
    }

    private fun showToast(message: String?) {
        getApplication<SharkPlayer>().applicationContext.showToast(message)
    }

    fun downloadSub(openSubtitleItem: OpenSubtitleItem) {
        viewModelScope.launch {
            when (val result = downloadSubRepository.downloadSub(openSubtitleItem)) {
                is TaskState.Failure -> showToast(result.error.message)
                is TaskState.Success -> _downloadedSubUri.value = result.data
            }
        }
    }
}
```  
# exoplayer\util\AudioOptions.kt  
```kt
package com.sharkaboi.sharkplayer.exoplayer.util

internal const val DEFAULT_AUDIO_TRACK = -1
internal const val DEFAULT_AUDIO_LANGUAGE = "eng"
internal const val AUDIO_LANGUAGE_SEPARATOR = ","

sealed class AudioOptions {

    data class WithTrackId(
        val trackId: Int = DEFAULT_AUDIO_TRACK
    ) : AudioOptions()

    data class WithLanguages(
        val languages: List<String> = listOf(DEFAULT_AUDIO_LANGUAGE)
    ) : AudioOptions()
}

```  
# exoplayer\util\SubtitleOptions.kt  
```kt
package com.sharkaboi.sharkplayer.exoplayer.util

internal const val DEFAULT_SUBTITLE_TRACK = -1
internal const val DEFAULT_SUBTITLE_LANGUAGE = "eng"
internal const val SUBTITLE_LANGUAGE_SEPARATOR = ","

sealed class SubtitleOptions {

    data class WithTrackId(
        val trackId: Int = DEFAULT_SUBTITLE_TRACK
    ) : SubtitleOptions()

    data class WithLanguages(
        val languages: List<String> = listOf(DEFAULT_SUBTITLE_LANGUAGE)
    ) : SubtitleOptions()
}

```  
# exoplayer\video\model\VideoInfo.kt  
```kt
package com.sharkaboi.sharkplayer.exoplayer.video.model

import com.google.android.exoplayer2.MediaItem
import com.sharkaboi.sharkplayer.exoplayer.util.AudioOptions
import com.sharkaboi.sharkplayer.exoplayer.util.SubtitleOptions

data class VideoInfo(
    val videoMediaItems: List<MediaItem>,
    val subtitleOptions: SubtitleOptions,
    val audioOptions: AudioOptions,
    val playWhenReady: Boolean
)

```  
# exoplayer\video\model\VideoNavArgs.kt  
```kt
package com.sharkaboi.sharkplayer.exoplayer.video.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VideoNavArgs(
    val dirPath: String,
    val videoPaths: List<String>,
) : Parcelable

```  
# exoplayer\video\repo\VideoPlayerRepository.kt  
```kt
package com.sharkaboi.sharkplayer.exoplayer.video.repo

import android.net.Uri
import androidx.core.net.toUri
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.util.MimeTypes
import com.sharkaboi.sharkplayer.common.extensions.tryCatching
import com.sharkaboi.sharkplayer.common.util.TaskState
import com.sharkaboi.sharkplayer.data.datastore.DataStoreRepository
import com.sharkaboi.sharkplayer.data.sharedpref.SharedPrefRepository
import com.sharkaboi.sharkplayer.exoplayer.util.AUDIO_LANGUAGE_SEPARATOR
import com.sharkaboi.sharkplayer.exoplayer.util.AudioOptions
import com.sharkaboi.sharkplayer.exoplayer.util.SUBTITLE_LANGUAGE_SEPARATOR
import com.sharkaboi.sharkplayer.exoplayer.util.SubtitleOptions
import com.sharkaboi.sharkplayer.exoplayer.video.model.VideoInfo
import com.sharkaboi.sharkplayer.exoplayer.video.model.VideoNavArgs
import kotlinx.coroutines.flow.firstOrNull
import timber.log.Timber
import java.io.File

class VideoPlayerRepository(
    private val sharedPrefRepository: SharedPrefRepository,
    private val dataStoreRepository: DataStoreRepository,
) {
    suspend fun getMetaDataOf(videoNavArgs: VideoNavArgs): TaskState<VideoInfo> =
        tryCatching {
            val dirPath = videoNavArgs.dirPath
            val subtitleOptions = getSubsConfiguration(dirPath, videoNavArgs)
            val audioOptions = getAudioConfiguration(dirPath, videoNavArgs)
            val playWhenReady = sharedPrefRepository.shouldStartVideoPaused().not()
            val mediaItems = videoNavArgs.videoPaths.map {
                val mediaItemBuilder = MediaItem.Builder().setUri(it)
                val subsList = mutableListOf<MediaItem.SubtitleConfiguration>()

                val sameNameSubUri = subOfVideoName(it, dirPath)
                if (sameNameSubUri != null) {
                    val mediaItemSubtitle =
                        MediaItem.SubtitleConfiguration.Builder(sameNameSubUri)
                            .setMimeType(MimeTypes.APPLICATION_SUBRIP)
                            .setRoleFlags(C.ROLE_FLAG_SUBTITLE)
                            .setLabel("Local - ${File(sameNameSubUri.toString()).name}")
                            .build()
                    subsList.add(mediaItemSubtitle)
                }

                val subsFolderSubUri = subOfVideoNameInSubsFolder(it, dirPath)
                if (subsFolderSubUri != null) {
                    val mediaItemSubtitle =
                        MediaItem.SubtitleConfiguration.Builder(subsFolderSubUri)
                            .setMimeType(MimeTypes.APPLICATION_SUBRIP)
                            .setRoleFlags(C.ROLE_FLAG_SUBTITLE)
                            .setLabel("Subs Folder - ${File(subsFolderSubUri.toString()).name}")
                            .build()
                    subsList.add(mediaItemSubtitle)
                }

                mediaItemBuilder.setSubtitleConfigurations(subsList)
                mediaItemBuilder.build()
            }

            TaskState.Success(
                VideoInfo(
                    videoMediaItems = mediaItems,
                    subtitleOptions = subtitleOptions,
                    audioOptions = audioOptions,
                    playWhenReady = playWhenReady
                )
            )
        }

    private fun subOfVideoNameInSubsFolder(videoUri: String, dirPath: String): Uri? {
        val videoFile = File(videoUri)
        val subFile = File(
            dirPath
                    + File.separator
                    + SUBS_FOLDER_NAME
                    + File.separator
                    + videoFile.nameWithoutExtension
                    + SUB_EXTENSION
        )
        return when {
            subFile.exists() -> subFile.absolutePath.toUri()
            else -> null
        }
    }

    private fun subOfVideoName(videoUri: String, dirPath: String): Uri? {
        val videoFile = File(videoUri)
        val subFile = File(
            dirPath
                    + File.separator
                    + videoFile.nameWithoutExtension
                    + SUB_EXTENSION
        )
        return when {
            subFile.exists() -> subFile.absolutePath.toUri()
            else -> null
        }
    }

    private suspend fun getAudioConfiguration(
        dirPath: String,
        videoNavArgs: VideoNavArgs
    ): AudioOptions {
        val audioLanguageOptions = sharedPrefRepository.getAudioLanguages()
        val audioIndexOptions =
            dataStoreRepository.audioTrackIndices.firstOrNull()?.get(dirPath)
        Timber.d("audioTrackIndices : ${dataStoreRepository.audioTrackIndices.firstOrNull()}")

        return when {
            audioIndexOptions != null -> AudioOptions.WithTrackId(audioIndexOptions)
            audioLanguageOptions != null -> {
                val languages =
                    audioLanguageOptions.split(AUDIO_LANGUAGE_SEPARATOR).map { it.trim() }
                AudioOptions.WithLanguages(languages)
            }
            else -> AudioOptions.WithTrackId()
        }
    }

    private suspend fun getSubsConfiguration(
        dirPath: String,
        videoNavArgs: VideoNavArgs
    ): SubtitleOptions {
        val subtitleLanguageOptions = sharedPrefRepository.getSubtitleLanguages()

        val subtitleIndexOptions =
            dataStoreRepository.subtitleTrackIndices.firstOrNull()?.get(dirPath)

        Timber.d("Language Options : $subtitleLanguageOptions")
        Timber.d("Sub Index Options : $subtitleIndexOptions")
        Timber.d("subtitleTrackIndices : ${dataStoreRepository.subtitleTrackIndices.firstOrNull()}")
        return when {
            subtitleIndexOptions != null -> SubtitleOptions.WithTrackId(subtitleIndexOptions)
            subtitleLanguageOptions != null -> {
                val languages =
                    subtitleLanguageOptions.split(SUBTITLE_LANGUAGE_SEPARATOR).map { it.trim() }
                SubtitleOptions.WithLanguages(languages)
            }
            else -> SubtitleOptions.WithLanguages()
        }
    }

    companion object {
        private const val SUB_EXTENSION = ".srt"
        private const val SUBS_FOLDER_NAME = "subs"
    }
}

```  
# exoplayer\video\ui\VideoPlayerActivity.kt  
```kt
package com.sharkaboi.sharkplayer.exoplayer.video.ui

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import coil.load
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionOverrides
import com.google.android.exoplayer2.util.MimeTypes
import com.sharkaboi.sharkplayer.R
import com.sharkaboi.sharkplayer.common.extensions.observe
import com.sharkaboi.sharkplayer.common.extensions.setVideosAsPlayList
import com.sharkaboi.sharkplayer.common.extensions.showToast
import com.sharkaboi.sharkplayer.databinding.ActivityVideoPlayerBinding
import com.sharkaboi.sharkplayer.exoplayer.download_sub.DownloadSubDialog
import com.sharkaboi.sharkplayer.exoplayer.util.AudioOptions
import com.sharkaboi.sharkplayer.exoplayer.util.SubtitleOptions
import com.sharkaboi.sharkplayer.exoplayer.video.model.VideoInfo
import com.sharkaboi.sharkplayer.exoplayer.video.vm.VideoPlayerState
import com.sharkaboi.sharkplayer.exoplayer.video.vm.VideoPlayerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File


@AndroidEntryPoint
class VideoPlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVideoPlayerBinding
    private var player: ExoPlayer? = null
    private var trackSelector: DefaultTrackSelector? = null
    private var playListListener: Player.Listener? = null
    private val videoPlayerViewModel by viewModels<VideoPlayerViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
        setObservers()
    }

    private fun initViews() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setLockOrientationListener()
    }

    private fun setLockOrientationListener() {
        val btnLockOrientation = findViewById<View>(R.id.exo_lock_orientation)
        btnLockOrientation.setOnClickListener {
            toggleLockedOrientation()
        }
    }

    private fun setObservers() {
        observe(videoPlayerViewModel.uiState) { state ->
            binding.progress.isVisible = state is VideoPlayerState.Loading
            when (state) {
                is VideoPlayerState.InvalidData -> showToast(state.message)
                is VideoPlayerState.Success -> handleMetaDataUpdate(state.videoInfo)
                else -> Unit
            }
        }
        observe(videoPlayerViewModel.downloadedSubUri) { uri ->
            if (uri == null) {
                return@observe
            }

            val currentMediaItem = player?.currentMediaItem ?: return@observe

            loadDownloadedSubOnto(currentMediaItem, uri)
        }
    }

    private fun loadDownloadedSubOnto(currentMediaItem: MediaItem, uri: Uri) {
        player?.let {
            val currentIndex = it.currentMediaItemIndex
            it.removeMediaItem(currentIndex)
            val subConfiguration =
                MediaItem.SubtitleConfiguration.Builder(uri)
                    .setMimeType(MimeTypes.APPLICATION_SUBRIP)
                    .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                    .setRoleFlags(C.ROLE_FLAG_SUBTITLE)
                    .setLabel("Downloads - ${File(uri.toString()).name}")
                    .build()
            val mergedMediaItem = currentMediaItem.buildUpon()
                .setSubtitleConfigurations(
                    currentMediaItem.localConfiguration?.subtitleConfigurations.orEmpty()
                        .plus(subConfiguration)
                ).build()
            Timber.d(mergedMediaItem.toString())
            it.addMediaItem(currentIndex, mergedMediaItem)
            it.play()
        }
    }

    private fun handleMetaDataUpdate(videoInfo: VideoInfo) =
        lifecycleScope.launch(Dispatchers.Main) {
            trackSelector = DefaultTrackSelector(this@VideoPlayerActivity)
            val builder = trackSelector!!.buildUponParameters()
            when (videoInfo.subtitleOptions) {
                is SubtitleOptions.WithLanguages -> {
                    builder?.setPreferredTextLanguages(*videoInfo.subtitleOptions.languages.toTypedArray())
                }
                else -> Unit
            }
            when (videoInfo.audioOptions) {
                is AudioOptions.WithLanguages -> {
                    builder?.setPreferredAudioLanguages(*videoInfo.audioOptions.languages.toTypedArray())
                }
                else -> Unit
            }
            trackSelector?.setParameters(builder)
            player = ExoPlayer.Builder(this@VideoPlayerActivity)
                .setTrackSelector(trackSelector!!)
                .build()
            binding.playerView.player = player
            player?.setVideosAsPlayList(videoInfo.videoMediaItems)
            player?.prepare()
            setSubErrorHandler()
            player?.playWhenReady = videoInfo.playWhenReady
            player?.addListener(getPlayListListener(videoInfo)!!)
//            player?.addAnalyticsListener(EventLogger(trackSelector))
            updateFileNameOf(player?.currentMediaItem)
            setDownloadListener()
        }

    private fun setSubErrorHandler() {
        player?.setErrorCallback {
            showToast("Corrupted subtitle file from open subtitles")
            videoPlayerViewModel.reloadVideo()
        }
    }

    private fun getPlayListListener(videoInfo: VideoInfo): Player.Listener? {
        playListListener = object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                updateFileNameOf(mediaItem)
            }

            override fun onTracksInfoChanged(tracksInfo: TracksInfo) {
                setTrackSelectionOptions(tracksInfo, videoInfo)
                Timber.d("onTracksInfoChanged called")
                super.onTracksInfoChanged(tracksInfo)
            }
        }
        return playListListener
    }

    private fun setTrackSelectionOptions(tracksInfo: TracksInfo?, videoInfo: VideoInfo) {
        val trackSelectorBuilder = player?.trackSelectionParameters?.buildUpon()
        val overrideBuilder = TrackSelectionOverrides.Builder()
        if (videoInfo.subtitleOptions is SubtitleOptions.WithTrackId) {
            val selectedTrackId = videoInfo.subtitleOptions.trackId

            val subGroups =
                tracksInfo?.trackGroupInfos?.filter { it.trackType == C.TRACK_TYPE_TEXT }
            Timber.d(subGroups.toString())
            val subGroup = subGroups?.map { it.trackGroup }?.getOrNull(selectedTrackId)
            Timber.d(subGroup.toString())
            if (subGroup != null && subGroup.length > 0) {
                Timber.d("Set to override sub at index $selectedTrackId")
                overrideBuilder
                    .setOverrideForType(
                        TrackSelectionOverrides.TrackSelectionOverride(
                            subGroup,
                            listOf(0)
                        )
                    )
            } else {
                Timber.d("Removed sub overrides as index - $selectedTrackId group - $subGroup")
                overrideBuilder.clearOverridesOfType(C.TRACK_TYPE_TEXT)
            }
        }

        if (videoInfo.audioOptions is AudioOptions.WithTrackId) {
            val selectedTrackId = videoInfo.audioOptions.trackId

            val audioGroups =
                tracksInfo?.trackGroupInfos?.filter { it.trackType == C.TRACK_TYPE_AUDIO }
            val audioGroup = audioGroups?.map { it.trackGroup }?.getOrNull(selectedTrackId)
            if (audioGroup != null && audioGroup.length > 0) {
                Timber.d("Set to override audio at index $selectedTrackId")
                overrideBuilder
                    .setOverrideForType(
                        TrackSelectionOverrides.TrackSelectionOverride(
                            audioGroup,
                            listOf(0)
                        )
                    )
            } else {
                Timber.d("Removed audio overrides as index - $selectedTrackId group - $audioGroup")
                overrideBuilder
                    .clearOverridesOfType(C.TRACK_TYPE_AUDIO)
            }
        }

        trackSelectorBuilder?.setTrackSelectionOverrides(overrideBuilder.build())

        trackSelectorBuilder?.build()?.let {
            player?.trackSelectionParameters = it
        }
    }

    private fun setDownloadListener() {
        val btnDownloadSub = binding.playerView.findViewById<View>(R.id.exo_download_sub)
        btnDownloadSub?.let {
            it.setOnClickListener {
                openDownloadSubDialog()
            }
        }
    }

    private fun openDownloadSubDialog() {
        DownloadSubDialog().show(supportFragmentManager, DownloadSubDialog::class.simpleName)
    }

    private fun updateFileNameOf(mediaItem: MediaItem?) {
        val tvFileName = binding.playerView.findViewById<TextView>(R.id.exo_video_file_name)
        tvFileName?.isSelected = true
        tvFileName?.text =
            mediaItem?.localConfiguration?.uri?.path?.let { File(it).nameWithoutExtension }
    }

    override fun onDestroy() {
        resetPlayer()
        super.onDestroy()
    }

    private fun resetPlayer() {
        binding.playerView.player = null
        playListListener?.let { player?.removeListener(it) }
        player?.release()
        player?.cleanErrorCallback()
    }

    var isOrientationLocked = false
    private fun toggleLockedOrientation() {
        if (isOrientationLocked) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        } else {
            when (resources.configuration.orientation) {
                ORIENTATION_PORTRAIT -> setPortraitMode()
                ORIENTATION_LANDSCAPE -> setLandScapeMode()
                else -> setLandScapeMode()
            }
        }
        isOrientationLocked = !isOrientationLocked
        val btnLockOrientation = findViewById<ImageView>(R.id.exo_lock_orientation)
        btnLockOrientation?.load(
            if (isOrientationLocked) R.drawable.ic_locked_rotation else R.drawable.ic_lock_rotation
        )
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun setPortraitMode() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private fun setLandScapeMode(orientation: Int = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
        requestedOrientation = orientation
    }
}
```  
# exoplayer\video\vm\VideoPlayerState.kt  
```kt
package com.sharkaboi.sharkplayer.exoplayer.video.vm

import androidx.lifecycle.MutableLiveData
import com.sharkaboi.sharkplayer.common.extensions.emptyString
import com.sharkaboi.sharkplayer.exoplayer.video.model.VideoInfo

sealed class VideoPlayerState {
    object Idle : VideoPlayerState()

    object Loading : VideoPlayerState()

    data class InvalidData(
        val message: String
    ) : VideoPlayerState()

    data class Success(
        val videoInfo: VideoInfo
    ) : VideoPlayerState()
}

internal fun MutableLiveData<VideoPlayerState>.setSuccess(
    videoInfo: VideoInfo
) {
    this.value = VideoPlayerState.Success(videoInfo)
}

internal fun MutableLiveData<VideoPlayerState>.setInvalidData(message: String) {
    this.value = VideoPlayerState.InvalidData(message)
}

internal fun MutableLiveData<VideoPlayerState>.setError(exception: Exception) {
    this.value = VideoPlayerState.InvalidData(exception.message ?: String.emptyString)
}

internal fun MutableLiveData<VideoPlayerState>.setIdle() {
    this.value = VideoPlayerState.Idle
}

internal fun MutableLiveData<VideoPlayerState>.setLoading() {
    this.value = VideoPlayerState.Loading
}

internal fun MutableLiveData<VideoPlayerState>.getDefault() = this.apply {
    this.setIdle()
}


```  
# exoplayer\video\vm\VideoPlayerViewModel.kt  
```kt
package com.sharkaboi.sharkplayer.exoplayer.video.vm

import android.net.Uri
import androidx.lifecycle.*
import com.sharkaboi.sharkplayer.common.util.TaskState
import com.sharkaboi.sharkplayer.exoplayer.video.model.VideoInfo
import com.sharkaboi.sharkplayer.exoplayer.video.model.VideoNavArgs
import com.sharkaboi.sharkplayer.exoplayer.video.repo.VideoPlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class VideoPlayerViewModel
@Inject
constructor(
    savedStateHandle: SavedStateHandle,
    private val videoPlayerRepository: VideoPlayerRepository
) : ViewModel() {
    private val videoNavArgs = savedStateHandle.get<VideoNavArgs>(VIDEO_NAV_ARGS_KEY)
    private val _uiState = MutableLiveData<VideoPlayerState>().getDefault()
    val uiState: LiveData<VideoPlayerState> = _uiState

    private val _downloadedSubUri = MutableLiveData<Uri?>(null)
    val downloadedSubUri: LiveData<Uri?> = _downloadedSubUri

    init {
        loadVideoFromArgs()
    }

    private fun loadVideoFromArgs() {
        if (videoNavArgs == null || videoNavArgs.videoPaths.isEmpty()) {
            _uiState.setInvalidData("Path was null")
        } else {
            loadVideoMetadata(videoNavArgs)
        }
    }

    private fun loadVideoMetadata(videoNavArgs: VideoNavArgs) {
        _uiState.setLoading()
        viewModelScope.launch {
            when (val result = videoPlayerRepository.getMetaDataOf(videoNavArgs)) {
                is TaskState.Failure -> _uiState.setError(result.error)
                is TaskState.Success -> _uiState.setSuccess(result.data)
            }
        }
    }

    private fun updateMetadata(videoInfo: VideoInfo) {
        _uiState.setLoading()
        _uiState.setSuccess(videoInfo)
    }

    fun setDownloadedSubUri(uri: Uri) {
        _downloadedSubUri.value = uri
        Timber.d("Set downloaded sub uri $uri")
    }

    fun reloadVideo() {
        viewModelScope.launch(Dispatchers.Main) {
            loadVideoFromArgs()
        }
    }

    companion object {
        const val VIDEO_NAV_ARGS_KEY = "videoNavArgs"
    }
}
```  
# ffmpeg\command\FFMpegCommandWrapper.kt  
```kt
package com.sharkaboi.sharkplayer.ffmpeg.command

import com.sharkaboi.sharkplayer.common.extensions.nextEvenInt
import com.sharkaboi.sharkplayer.common.models.SharkPlayerFile
import java.io.File

typealias FFMpegCommand = Array<String>

object FFMpegCommandWrapper {
    fun rescaleVideo(
        videoFile: SharkPlayerFile.VideoFile,
        targetResolution: String
    ): Pair<FFMpegCommand, String> {
        val dirPath = videoFile.parentPath
        val outputPath = buildString {
            append(dirPath)
            append(File.separator)
            append(videoFile.fileName)
            append("_$targetResolution")
            append("_${System.currentTimeMillis()}")
            append('.')
            append(videoFile.getFile().extension)
        }

        val outputHeight = targetResolution.trimEnd('p').toInt()
        val inputWidth = videoFile.videoWidth.toFloat()
        val inputHeight = videoFile.videoHeight.toFloat()
        val outputWidth = (inputWidth / (inputHeight / outputHeight)).nextEvenInt()

        // ffmpeg -i input.mp4 -vcodec libx264 -crf 27 -preset veryfast -c:a copy -s 960x540 output.mp4
        val cmd = arrayOf(
            "-i",
            videoFile.path,
            "-vcodec",
            "libx264",
            "-crf",
            "27",
            "-preset",
            "veryfast",
            "-c:a",
            "copy",
            "-s",
            "${outputWidth}x$outputHeight",
            outputPath
        )
        return Pair(cmd, outputPath)
    }
}
```  
# ffmpeg\FFMpegDataSource.kt  
```kt
package com.sharkaboi.sharkplayer.ffmpeg

import com.sharkaboi.sharkplayer.common.util.TaskState

interface FFMpegDataSource {
    val isRunning: Boolean
    suspend fun loadBinary(): TaskState<Unit>
    suspend fun execute(command: Array<String>): TaskState<String>
    fun killProcess(): Boolean
    fun setTimeout(timeout: Long)
}
```  
# ffmpeg\sources\AndroidFFmpegDataSource.kt  
```kt
package com.sharkaboi.sharkplayer.ffmpeg.sources

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException
import com.sharkaboi.sharkplayer.common.util.TaskState
import com.sharkaboi.sharkplayer.ffmpeg.FFMpegDataSource
import com.sharkaboi.sharkplayer.ffmpeg.command.FFMpegCommand
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AndroidFFmpegDataSource(
    private val ffmpeg: FFmpeg
) : FFMpegDataSource {
    override val isRunning: Boolean get() = ffmpeg.isFFmpegCommandRunning

    override suspend fun loadBinary(): TaskState<Unit> {
        return suspendCoroutine { continuation ->
            try {
                ffmpeg.loadBinary(object : LoadBinaryResponseHandler() {
                    override fun onFailure() {
                        continuation.resume(TaskState.failureWithMessage("FFmpeg failure"))
                    }

                    override fun onSuccess() {
                        continuation.resume(TaskState.Success(Unit))
                    }
                })
            } catch (e: FFmpegNotSupportedException) {
                continuation.resume(TaskState.Failure(e))
            }
        }
    }

    override fun killProcess(): Boolean = ffmpeg.killRunningProcesses()

    override fun setTimeout(timeout: Long) = ffmpeg.setTimeout(timeout)

    override suspend fun execute(
        command: FFMpegCommand
    ): TaskState<String> {
        return suspendCoroutine { continuation ->
            try {
                ffmpeg.execute(command, object : ExecuteBinaryResponseHandler() {
                    override fun onStart() {}

                    override fun onProgress(message: String) {
                        Timber.d(message)
                    }

                    override fun onFailure(message: String) {
                        continuation.resume(TaskState.failureWithMessage(message))
                    }

                    override fun onSuccess(message: String) {
                        continuation.resume(TaskState.Success(message))
                    }

                    override fun onFinish() {}
                })
            } catch (e: FFmpegNotSupportedException) {
                continuation.resume(TaskState.Failure(e))
            }
        }
    }
}
```  
# ffmpeg\workers\FFMpegWorker.kt  
```kt
package com.sharkaboi.sharkplayer.ffmpeg.workers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.sharkaboi.sharkplayer.R
import com.sharkaboi.sharkplayer.common.extensions.emptyString
import com.sharkaboi.sharkplayer.common.util.TaskState
import com.sharkaboi.sharkplayer.ffmpeg.FFMpegDataSource
import com.sharkaboi.sharkplayer.ffmpeg.command.FFMpegCommand
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.File

@HiltWorker
class FFMpegWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val ffMpegDataSource: FFMpegDataSource
) : Worker(context, params) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager
    private val job = Job()
    private val exceptionHandler = CoroutineExceptionHandler { _, _ ->
        cleanup()
    }

    override fun onStopped() {
        super.onStopped()
        job.cancel()
        cleanup()
    }

    private fun cleanup() {
        inputData.getString(TARGET_FILE_PATH_KEY)?.let {
            File(it).delete()
            Timber.d("File cleaned")
            ffMpegDataSource.killProcess()
            Timber.d("FFMPEG Killed")
        }
    }

    override fun doWork(): Result {
        return runBlocking(Dispatchers.IO + job + exceptionHandler) {
            doFFMpegTask()
        }
    }

    private suspend fun doFFMpegTask() = withContext(Dispatchers.IO) {
        val cmd = inputData.getStringArray(FFMPEG_CMD_KEY)
            ?: return@withContext Result.failure()

        val notificationTitle = inputData.getString(NOTIFICATION_TITLE_KEY)
            ?: return@withContext Result.failure()

        val content = inputData.getString(NOTIFICATION_CONTENT_KEY)
            ?: String.emptyString

        val foregroundInfo = createForegroundInfo(notificationTitle, content)
        setForegroundAsync(foregroundInfo).await()

        Timber.d("cmd : ${cmd.joinToString()}")
        Timber.d("content : $content")

        val result = ffMpegDataSource.loadBinary()
        if (result.isFailure) {
            Timber.d("Couldn't load ffmpeg lib due to ${(result as TaskState.Failure).error.message}")
            return@withContext Result.failure()
        }

        return@withContext when (val operationResult = ffMpegDataSource.execute(cmd)) {
            is TaskState.Failure -> {
                Timber.d("ffmpeg failure : ${operationResult.error.message}")
                Result.failure()
            }
            is TaskState.Success -> {
                Timber.d("ffmpeg success : ${operationResult.data}")
                Result.success()
            }
        }
    }

    private fun createForegroundInfo(
        title: String,
        content: String
    ): ForegroundInfo {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }

        val notification = getWorkNotification(title, content)

        return ForegroundInfo(NOTIFICATION_ID, notification)
    }

    private fun getWorkNotification(title: String, content: String): Notification {
        val channelId = applicationContext.getString(R.string.ffmpeg_notification_channel_id)
        val cancel = applicationContext.getString(R.string.rescale_cancel_notification)

        val cancelWorkPendingIntent = WorkManager.getInstance(applicationContext)
            .createCancelPendingIntent(id)

        return NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_service)
            .setOngoing(true)
            .addAction(R.drawable.ic_close, cancel, cancelWorkPendingIntent)
            .build()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val id = applicationContext.getString(R.string.ffmpeg_notification_channel_id)
        val name = applicationContext.getString(R.string.ffmpeg_channel_name)
        val descriptionText = applicationContext.getString(R.string.ffmpeg_notif_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel(id, name, importance)
        mChannel.description = descriptionText
        notificationManager.createNotificationChannel(mChannel)

    }

    companion object {
        private const val FFMPEG_CMD_KEY = "cmdKey"
        private const val NOTIFICATION_CONTENT_KEY = "notificationContent"
        private const val NOTIFICATION_TITLE_KEY = "notificationTitle"
        private const val TARGET_FILE_PATH_KEY = "targetFilePath"
        private const val NOTIFICATION_ID = 42069
        val packages = setOf(
            "com.sharkaboi.sharkplayer.ffmpeg.workers.FFMpegWorker",
            "com.sharkaboi.sharkplayer.ffmpeg.workers.RescaleVideoWorker"
        )

        fun getWorkData(
            cmd: FFMpegCommand,
            notificationTitle: String,
            notificationContent: String,
            targetFilePath: String
        ): Data {
            return workDataOf(
                FFMPEG_CMD_KEY to cmd,
                NOTIFICATION_CONTENT_KEY to notificationContent,
                NOTIFICATION_TITLE_KEY to notificationTitle,
                TARGET_FILE_PATH_KEY to targetFilePath
            )
        }
    }
}

```  
# modules\directory\adapters\DirectoryAdapter.kt  
```kt
package com.sharkaboi.sharkplayer.modules.directory.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.sharkaboi.sharkplayer.R
import com.sharkaboi.sharkplayer.common.extensions.getSizeString
import com.sharkaboi.sharkplayer.common.extensions.getTimeString
import com.sharkaboi.sharkplayer.common.extensions.setThumbnailOf
import com.sharkaboi.sharkplayer.common.models.SharkPlayerFile
import com.sharkaboi.sharkplayer.databinding.ItemDirectoryFileBinding
import me.saket.cascade.CascadePopupMenu

class DirectoryAdapter(
    private val onClick: (SharkPlayerFile) -> Unit,
    private val onVideoRescale: (SharkPlayerFile.VideoFile) -> Unit,
    private val onDeleteVideo: (SharkPlayerFile.VideoFile) -> Unit
) : ListAdapter<SharkPlayerFile, DirectoryAdapter.DirectoryViewHolder>(diffUtilItemCallback) {

    private lateinit var binding: ItemDirectoryFileBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DirectoryViewHolder {
        binding = ItemDirectoryFileBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DirectoryViewHolder(binding, onClick, onVideoRescale, onDeleteVideo)
    }

    override fun onBindViewHolder(holder: DirectoryViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    class DirectoryViewHolder(
        private val binding: ItemDirectoryFileBinding,
        private val onClick: (SharkPlayerFile) -> Unit,
        private val onVideoRescale: (SharkPlayerFile.VideoFile) -> Unit,
        private val onDeleteVideo: (SharkPlayerFile.VideoFile) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.ibMore.isGone = true
            binding.tvName.isSelected = true
        }

        fun bind(item: SharkPlayerFile) {
            binding.root.setOnClickListener {
                onClick(item)
            }
            when (item) {
                is SharkPlayerFile.AudioFile -> {
                    binding.ivThumbnail.load(item.path.toUri()) {
                        error(R.drawable.ic_audio_file)
                        fallback(R.drawable.ic_audio_file)
                        placeholder(R.drawable.ic_audio_file)
                    }
                    binding.tvName.text = item.fileName
                    binding.tvDetails.text = buildString {
                        append(item.quality)
                        append(" - ")
                        append(item.length.getTimeString())
                        append(" - ")
                        append(item.size.getSizeString())
                    }
                }
                is SharkPlayerFile.Directory -> {
                    binding.ivThumbnail.load(R.drawable.ic_directory)
                    binding.tvName.text = item.folderName
                    binding.tvDetails.text = ("${item.childFileCount} items")
                }
                is SharkPlayerFile.OtherFile -> {
                    binding.ivThumbnail.load(R.drawable.ic_other_file)
                    binding.tvName.text = item.fileName
                    binding.tvDetails.text = (item.size.getSizeString())
                }
                is SharkPlayerFile.VideoFile -> {
                    binding.ivThumbnail.setThumbnailOf(item) {
                        error(R.drawable.ic_video_file)
                        fallback(R.drawable.ic_video_file)
                        placeholder(R.drawable.ic_video_file)
                    }
                    binding.tvName.text = item.fileName
                    binding.tvDetails.text = buildString {
                        append(item.resolution)
                        append(" - ")
                        append(item.length.getTimeString())
                        append(" - ")
                        append(item.size.getSizeString())
                    }
                    binding.ibMore.isVisible = true
                    binding.ibMore.setOnClickListener {
                        val menu = CascadePopupMenu(it.context, it)
                        menu.inflate(R.menu.video_options_menu)
                        menu.setOnMenuItemClickListener { menuItem ->
                            when (menuItem.itemId) {
                                R.id.rescale_video_item -> onVideoRescale(item)
                                R.id.delete_video_item -> onDeleteVideo(item)
                            }
                            true
                        }
                        menu.show()
                    }
                }
            }
        }
    }
}

private val diffUtilItemCallback = object : DiffUtil.ItemCallback<SharkPlayerFile>() {
    override fun areItemsTheSame(oldItem: SharkPlayerFile, newItem: SharkPlayerFile): Boolean {
        return oldItem.absolutePath == newItem.absolutePath
    }

    override fun areContentsTheSame(
        oldItem: SharkPlayerFile,
        newItem: SharkPlayerFile
    ): Boolean {
        return oldItem == newItem
    }
}

```  
# modules\directory\repo\DirectoryRepository.kt  
```kt
package com.sharkaboi.sharkplayer.modules.directory.repo

import com.sharkaboi.sharkplayer.common.models.SharkPlayerFile
import com.sharkaboi.sharkplayer.common.util.TaskState
import kotlinx.coroutines.flow.Flow

interface DirectoryRepository {
    val favorites: Flow<List<SharkPlayerFile.Directory>>
    val subtitleTrackIndices: Flow<Map<String, Int>>
    val audioTrackIndices: Flow<Map<String, Int>>

    suspend fun getFilesInFolder(directory: SharkPlayerFile.Directory): TaskState<List<SharkPlayerFile>>

    suspend fun setFolderAsFavorite(directory: SharkPlayerFile.Directory): TaskState<Unit>

    suspend fun removeFolderAsFavorite(directory: SharkPlayerFile.Directory): TaskState<Unit>

    suspend fun setSubTrackIndexOfDir(
        trackId: Int,
        directory: SharkPlayerFile.Directory
    ): TaskState<Unit>

    suspend fun setAudioTrackIndexOfDir(
        trackId: Int,
        directory: SharkPlayerFile.Directory
    ): TaskState<Unit>

    suspend fun doesExist(selectedDir: SharkPlayerFile.Directory): TaskState<Unit>

    suspend fun deleteVideo(videoFile: SharkPlayerFile.VideoFile): TaskState<Unit>
}
```  
# modules\directory\repo\FileDirectoryRepository.kt  
```kt
package com.sharkaboi.sharkplayer.modules.directory.repo

import com.sharkaboi.sharkplayer.common.extensions.tryCatching
import com.sharkaboi.sharkplayer.common.models.SharkPlayerFile
import com.sharkaboi.sharkplayer.common.models.toSharkPlayerFile
import com.sharkaboi.sharkplayer.common.util.TaskState
import com.sharkaboi.sharkplayer.data.datastore.DataStoreRepository
import kotlinx.coroutines.flow.Flow
import java.io.File

class FileDirectoryRepository(
    private val dataStoreRepository: DataStoreRepository,
) : DirectoryRepository {

    override val favorites: Flow<List<SharkPlayerFile.Directory>> =
        dataStoreRepository.favouritesDirsFlow
    override val subtitleTrackIndices: Flow<Map<String, Int>> =
        dataStoreRepository.subtitleTrackIndices
    override val audioTrackIndices: Flow<Map<String, Int>> = dataStoreRepository.audioTrackIndices

    override suspend fun getFilesInFolder(directory: SharkPlayerFile.Directory): TaskState<List<SharkPlayerFile>> =
        tryCatching {
            val currentDirectory = File(directory.path)
            val filesInDir = currentDirectory.listFiles().orEmpty()
            val sharkFiles: List<SharkPlayerFile> = filesInDir.map {
                it.toSharkPlayerFile()
            }.sortedWith(compareBy({ it !is SharkPlayerFile.Directory }, { it.sortField }))
            TaskState.Success(sharkFiles)
        }

    override suspend fun setFolderAsFavorite(directory: SharkPlayerFile.Directory): TaskState<Unit> =
        tryCatching {
            dataStoreRepository.addFavorite(directory)
            TaskState.Success(Unit)
        }

    override suspend fun removeFolderAsFavorite(directory: SharkPlayerFile.Directory): TaskState<Unit> =
        tryCatching {
            dataStoreRepository.removeFavorite(directory)
            TaskState.Success(Unit)
        }

    override suspend fun setSubTrackIndexOfDir(
        trackId: Int,
        directory: SharkPlayerFile.Directory
    ): TaskState<Unit> = tryCatching {
        dataStoreRepository.setSubTrackIndexOfDir(trackId, directory)
        TaskState.Success(Unit)
    }

    override suspend fun setAudioTrackIndexOfDir(
        trackId: Int,
        directory: SharkPlayerFile.Directory
    ): TaskState<Unit> = tryCatching {
        dataStoreRepository.setAudioTrackIndexOfDir(trackId, directory)
        TaskState.Success(Unit)
    }

    override suspend fun doesExist(selectedDir: SharkPlayerFile.Directory): TaskState<Unit> =
        tryCatching {
            val file = File(selectedDir.path)
            if (file.exists() && file.isDirectory) {
                TaskState.Success(Unit)
            } else {
                TaskState.failureWithMessage("Directory does not exist.")
            }
        }

    override suspend fun deleteVideo(videoFile: SharkPlayerFile.VideoFile): TaskState<Unit> =
        tryCatching {
            val file = videoFile.getFile()
            if (!file.exists()) {
                return@tryCatching TaskState.failureWithMessage("Video file does not exist.")
            }

            val isSuccess = file.delete()
            if (!isSuccess) {
                return@tryCatching TaskState.failureWithMessage("Couldn't delete video file.")
            }

            TaskState.Success(Unit)
        }
}
```  
# modules\directory\repo\MediaStoreDirectoryRepository.kt  
```kt
package com.sharkaboi.sharkplayer.modules.directory.repo

import android.content.ContentResolver
import android.provider.MediaStore
import androidx.core.content.ContentResolverCompat
import com.sharkaboi.sharkplayer.common.extensions.getLongOfColumnName
import com.sharkaboi.sharkplayer.common.extensions.getStringOfColumnName
import com.sharkaboi.sharkplayer.common.extensions.tryCatching
import com.sharkaboi.sharkplayer.common.models.SharkPlayerFile
import com.sharkaboi.sharkplayer.common.util.TaskState
import com.sharkaboi.sharkplayer.data.datastore.DataStoreRepository
import kotlinx.coroutines.flow.Flow
import java.io.File
import kotlin.time.Duration

class MediaStoreDirectoryRepository(
    private val dataStoreRepository: DataStoreRepository,
    private val contentResolver: ContentResolver,
) : DirectoryRepository {

    override val favorites: Flow<List<SharkPlayerFile.Directory>> =
        dataStoreRepository.favouritesDirsFlow
    override val subtitleTrackIndices: Flow<Map<String, Int>> =
        dataStoreRepository.subtitleTrackIndices
    override val audioTrackIndices: Flow<Map<String, Int>> =
        dataStoreRepository.audioTrackIndices

    override suspend fun getFilesInFolder(directory: SharkPlayerFile.Directory): TaskState<List<SharkPlayerFile>> =
        tryCatching {
            val sharkFiles = mutableListOf<SharkPlayerFile.VideoFile>()
            val mediaUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            val cursor = ContentResolverCompat.query(
                contentResolver,
                mediaUri,
                arrayOf(
                    MediaStore.Video.Media.DATA,
                    MediaStore.Video.Media.DISPLAY_NAME,
                    MediaStore.Video.Media.RESOLUTION,
                    MediaStore.Video.Media.DURATION,
                    MediaStore.Video.Media.SIZE,
                ),
                null,
                null,
                MediaStore.Video.Media.DATE_ADDED + " DESC",
                null
            )
            while (cursor != null && cursor.moveToNext()) {
                val path = cursor.getStringOfColumnName(MediaStore.Video.Media.DATA)
                val name = cursor.getStringOfColumnName(MediaStore.Video.Media.DISPLAY_NAME)
                val height = cursor.getLongOfColumnName(MediaStore.Video.Media.HEIGHT)
                val width = cursor.getLongOfColumnName(MediaStore.Video.Media.WIDTH)
                val length = cursor.getLongOfColumnName(MediaStore.Video.Media.DURATION)
                val size = cursor.getLongOfColumnName(MediaStore.Video.Media.SIZE)
                sharkFiles.add(
                    SharkPlayerFile.VideoFile(
                        fileName = name,
                        path = path,
                        videoHeight = height.toInt(),
                        videoWidth = width.toInt(),
                        length = Duration.milliseconds(length),
                        size = size
                    )
                )
            }
            TaskState.Success(sharkFiles)
        }

    override suspend fun setFolderAsFavorite(directory: SharkPlayerFile.Directory): TaskState<Unit> =
        tryCatching {
            dataStoreRepository.addFavorite(directory)
            TaskState.Success(Unit)
        }

    override suspend fun removeFolderAsFavorite(directory: SharkPlayerFile.Directory): TaskState<Unit> =
        tryCatching {
            dataStoreRepository.removeFavorite(directory)
            TaskState.Success(Unit)
        }

    override suspend fun setSubTrackIndexOfDir(
        trackId: Int,
        directory: SharkPlayerFile.Directory
    ): TaskState<Unit> = tryCatching {
        dataStoreRepository.setSubTrackIndexOfDir(trackId, directory)
        TaskState.Success(Unit)
    }

    override suspend fun setAudioTrackIndexOfDir(
        trackId: Int,
        directory: SharkPlayerFile.Directory
    ): TaskState<Unit> = tryCatching {
        dataStoreRepository.setAudioTrackIndexOfDir(trackId, directory)
        TaskState.Success(Unit)
    }

    override suspend fun doesExist(selectedDir: SharkPlayerFile.Directory): TaskState<Unit> =
        tryCatching {
            val file = File(selectedDir.path)
            if (file.exists() && file.isDirectory) {
                TaskState.Success(Unit)
            } else {
                TaskState.failureWithMessage("Directory does not exist.")
            }
        }

    override suspend fun deleteVideo(videoFile: SharkPlayerFile.VideoFile): TaskState<Unit> =
        tryCatching {
            val file = videoFile.getFile()
            if (!file.exists()) {
                return@tryCatching TaskState.failureWithMessage("Video file does not exist.")
            }

            val isSuccess = file.delete()
            if (!isSuccess) {
                return@tryCatching TaskState.failureWithMessage("Couldn't delete video file.")
            }

            TaskState.Success(Unit)
        }
}
```  
# modules\directory\ui\DirectoryFragment.kt  
```kt
package com.sharkaboi.sharkplayer.modules.directory.ui

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sharkaboi.sharkplayer.BottomNavGraphDirections
import com.sharkaboi.sharkplayer.R
import com.sharkaboi.sharkplayer.common.extensions.*
import com.sharkaboi.sharkplayer.common.models.SharkPlayerFile
import com.sharkaboi.sharkplayer.databinding.FragmentDirectoryBinding
import com.sharkaboi.sharkplayer.exoplayer.video.model.VideoNavArgs
import com.sharkaboi.sharkplayer.modules.directory.adapters.DirectoryAdapter
import com.sharkaboi.sharkplayer.modules.directory.vm.DirectoryState
import com.sharkaboi.sharkplayer.modules.directory.vm.DirectoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class DirectoryFragment : Fragment() {
    private var _binding: FragmentDirectoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var directoryAdapter: DirectoryAdapter
    private val navController by lazy { findNavController() }
    private val directoryViewModel by viewModels<DirectoryViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDirectoryBinding.inflate(inflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.invalidateOptionsMenu()
        (activity as? AppCompatActivity)?.setSupportActionBar(null)
        binding.rvDirectories.adapter = null
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        setObservers()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.directory_options_menu, menu)
        val favoriteItem = menu.findItem(R.id.item_add_favorite)
        if (directoryViewModel.isFavorite.value == true) {
            favoriteItem?.title = getString(R.string.remove_from_favorite)
            favoriteItem?.icon = AppCompatResources.getDrawable(
                requireContext(),
                R.drawable.ic_favorite_selected
            )
        } else {
            favoriteItem?.title = getString(R.string.add_to_favorite)
            favoriteItem?.icon = AppCompatResources.getDrawable(
                requireContext(),
                R.drawable.ic_add_to_favorite
            )
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_add_favorite -> directoryViewModel.toggleFavorite()
            R.id.item_subtitle_track -> showSubtitleTrackDialog(directoryViewModel.subtitleIndexOfDirectory.value)
            R.id.item_audio_track -> showAudioTrackDialog(directoryViewModel.audioIndexOfDirectory.value)
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun initViews() {
        setupSwipeRefresh()
        setupTitle()
        setupBackButton()
        setupPathTextView()
        setupRecyclerView()
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshDirectory.setOnRefreshListener {
            directoryViewModel.refresh()
            binding.swipeRefreshDirectory.isRefreshing = false
        }
    }

    private fun setupTitle() {
        binding.toolbar.title = directoryViewModel.selectedDir.folderName
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        setHasOptionsMenu(true)
    }

    private fun setupBackButton() {
        binding.toolbar.setNavigationOnClickListener { navController.navigateUp() }
    }

    private fun setupPathTextView() {
        binding.tvPath.text = directoryViewModel.selectedDir.path
        binding.tvPath.isSelected = true
    }

    private fun setupRecyclerView() {
        val rvDirectories = binding.rvDirectories
        directoryAdapter = DirectoryAdapter(
            onClick = { file ->
                navigateToFile(file)
            },
            onVideoRescale = { videoFile ->
                showRescaleOptions(videoFile)
            },
            onDeleteVideo = { videoFile ->
                context?.showOneOpDialog(R.string.delete_video) {
                    directoryViewModel.deleteVideo(videoFile)
                }
            }
        )
        rvDirectories.adapter = directoryAdapter
        rvDirectories.initLinearDefaults(context, hasFixedSize = true)
    }

    private fun showRescaleOptions(videoFile: SharkPlayerFile.VideoFile) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.rescale_options_title)
            .setItems(R.array.rescale_supported_resolutions) { dialog, which ->
                directoryViewModel.runRescaleWork(
                    videoFile,
                    resources.getStringArray(R.array.rescale_supported_resolutions).getOrNull(which)
                )
                dialog.dismiss()
            }.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun setObservers() {
        observe(directoryViewModel.uiState) { state ->
            binding.progress.isVisible = state is DirectoryState.Loading
            binding.tvExistHint.isVisible = state is DirectoryState.DirectoryNotFound
            when (state) {
                is DirectoryState.Failure -> showToast(state.message)
                else -> Unit
            }
        }
        observe(directoryViewModel.isFavorite) {
            activity?.invalidateOptionsMenu()
        }
        observe(directoryViewModel.files) { files ->
            binding.tvEmptyHint.isVisible = files.isEmpty()
            directoryAdapter.submitList(files)
            setPlayListListener(files)
        }
        observe(directoryViewModel.subtitleIndexOfDirectory) {
            activity?.invalidateOptionsMenu()
        }
        observe(directoryViewModel.audioIndexOfDirectory) {
            activity?.invalidateOptionsMenu()
        }
    }

    private fun navigateToFile(file: SharkPlayerFile) {
        when (file) {
            is SharkPlayerFile.AudioFile -> openAudio(file)
            is SharkPlayerFile.Directory -> openDirectory(file)
            is SharkPlayerFile.OtherFile -> showToast(R.string.unsupported_file)
            is SharkPlayerFile.VideoFile -> openVideo(file)
        }
    }

    private fun showAudioTrackDialog(defaultValue: Int?) {
        requireContext().showIntegerValuePromptDialog(
            titleId = R.string.enter_track_index,
            defaultValue = defaultValue
        ) { value ->
            directoryViewModel.setAudioTrackIndexOfDir(value)
        }
    }

    private fun showSubtitleTrackDialog(defaultValue: Int?) {
        requireContext().showIntegerValuePromptDialog(
            titleId = R.string.enter_track_index,
            defaultValue = defaultValue
        ) { value ->
            directoryViewModel.setSubTrackIndexOfDir(value)
        }
    }

    private fun setPlayListListener(files: List<SharkPlayerFile>) {
        val videoPaths = files.filterIsInstance<SharkPlayerFile.VideoFile>().map { it.path }
        Timber.d(videoPaths.toString())
        binding.fabPlay.setOnClickListener { openAsPlaylist(videoPaths) }
    }

    private fun openAsPlaylist(videoPaths: List<String>) {
        if (videoPaths.isEmpty()) {
            showToast(R.string.no_videos_in_folder)
            return
        }

        val action = BottomNavGraphDirections.openVideos(
            videoNavArgs = VideoNavArgs(
                dirPath = directoryViewModel.selectedDir.path,
                videoPaths = videoPaths
            )
        )
        navController.navigate(action)
    }

    private fun openDirectory(file: SharkPlayerFile.Directory) {
        val action = BottomNavGraphDirections.openDirectory(file.path)
        navController.navigate(action)
    }

    private fun openVideo(file: SharkPlayerFile.VideoFile) {
        if (file.isDirty) {
            showToast(R.string.video_corrupted)
            return
        }

        val action = BottomNavGraphDirections.openVideos(
            videoNavArgs = VideoNavArgs(
                dirPath = directoryViewModel.selectedDir.path,
                videoPaths = listOf(file.path)
            )
        )
        navController.navigate(action)
    }

    private fun openAudio(file: SharkPlayerFile.AudioFile) {
        if (file.isDirty) {
            showToast(R.string.audio_corrupted)
            return
        }

        val action = BottomNavGraphDirections.openAudio(file.path)
        navController.navigate(action)
    }
}
```  
# modules\directory\vm\DirectoryState.kt  
```kt
package com.sharkaboi.sharkplayer.modules.directory.vm

import androidx.lifecycle.MutableLiveData
import com.sharkaboi.sharkplayer.common.extensions.emptyString

sealed class DirectoryState {
    object Idle : DirectoryState()
    object Loading : DirectoryState()
    object DirectoryNotFound : DirectoryState()
    data class Failure(val message: String) : DirectoryState()
}

internal fun MutableLiveData<DirectoryState>.setError(message: String) {
    this.value = DirectoryState.Failure(message)
}

internal fun MutableLiveData<DirectoryState>.setError(exception: Exception) {
    this.value = DirectoryState.Failure(exception.message ?: String.emptyString)
}

internal fun MutableLiveData<DirectoryState>.setIdle() {
    this.value = DirectoryState.Idle
}

internal fun MutableLiveData<DirectoryState>.setLoading() {
    this.value = DirectoryState.Loading
}

internal fun MutableLiveData<DirectoryState>.setDirectoryNotFound() {
    this.value = DirectoryState.DirectoryNotFound
}

internal fun MutableLiveData<DirectoryState>.getDefault() = this.apply {
    this.setIdle()
}

```  
# modules\directory\vm\DirectoryViewModel.kt  
```kt
package com.sharkaboi.sharkplayer.modules.directory.vm

import android.app.Application
import androidx.lifecycle.*
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.sharkaboi.sharkplayer.R
import com.sharkaboi.sharkplayer.SharkPlayer
import com.sharkaboi.sharkplayer.common.models.SharkPlayerFile
import com.sharkaboi.sharkplayer.common.util.TaskState
import com.sharkaboi.sharkplayer.ffmpeg.command.FFMpegCommandWrapper
import com.sharkaboi.sharkplayer.ffmpeg.workers.FFMpegWorker
import com.sharkaboi.sharkplayer.modules.directory.repo.DirectoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DirectoryViewModel
@Inject constructor(
    app: Application,
    private val directoryRepository: DirectoryRepository,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(app) {
    private val path = savedStateHandle.get<String>(PATH_KEY)
    val selectedDir = SharkPlayerFile.directoryFromPath(path)

    val isFavorite: LiveData<Boolean> =
        directoryRepository.favorites.map { list ->
            list.firstOrNull { it.path == selectedDir.path } != null
        }.asLiveData()

    val subtitleIndexOfDirectory: LiveData<Int?> =
        directoryRepository.subtitleTrackIndices.map { subtitleIndices ->
            subtitleIndices[selectedDir.path]
        }.asLiveData()

    val audioIndexOfDirectory: LiveData<Int?> =
        directoryRepository.audioTrackIndices.map { audioIndices ->
            audioIndices[selectedDir.path]
        }.asLiveData()

    private val _uiState = MutableLiveData<DirectoryState>().getDefault()
    val uiState: LiveData<DirectoryState> = _uiState

    private val _files = MutableLiveData<List<SharkPlayerFile>>()
    val files: LiveData<List<SharkPlayerFile>> = _files

    init {
        loadDirectory()
    }

    private fun loadDirectory() {
        _uiState.setLoading()
        viewModelScope.launch {
            when (directoryRepository.doesExist(selectedDir)) {
                is TaskState.Failure -> {
                    _uiState.setDirectoryNotFound()
                    return@launch
                }
                else -> Unit
            }
            when (val result = directoryRepository.getFilesInFolder(selectedDir)) {
                is TaskState.Failure -> _uiState.setError(result.error)
                is TaskState.Success -> {
                    _files.value = result.data
                    _uiState.setIdle()
                }
            }
        }
    }

    fun toggleFavorite() {
        _uiState.setLoading()
        viewModelScope.launch {
            val result =
                if (isFavorite.value == true) {
                    directoryRepository.removeFolderAsFavorite(selectedDir)
                } else {
                    directoryRepository.setFolderAsFavorite(selectedDir)
                }
            when (result) {
                is TaskState.Failure -> _uiState.setError(result.error)
                else -> _uiState.setIdle()
            }
        }
    }

    fun setSubTrackIndexOfDir(trackId: Int) {
        _uiState.setLoading()
        viewModelScope.launch {
            when (val result = directoryRepository.setSubTrackIndexOfDir(trackId, selectedDir)) {
                is TaskState.Failure -> _uiState.setError(result.error)
                is TaskState.Success -> _uiState.setIdle()
            }
        }
    }

    fun setAudioTrackIndexOfDir(trackId: Int) {
        _uiState.setLoading()
        viewModelScope.launch {
            when (val result = directoryRepository.setAudioTrackIndexOfDir(trackId, selectedDir)) {
                is TaskState.Failure -> _uiState.setError(result.error)
                is TaskState.Success -> _uiState.setIdle()
            }
        }
    }

    fun runRescaleWork(videoFile: SharkPlayerFile.VideoFile, targetResolution: String?) {
        if (targetResolution == null) {
            _uiState.setError("Invalid resolution passed")
            return
        }

        val (cmd, outputPath) = FFMpegCommandWrapper.rescaleVideo(videoFile, targetResolution)
        val notificationContent = getApplication<SharkPlayer>()
            .getString(
                R.string.rescale_notification_content,
                videoFile.fileName,
                videoFile.videoHeight,
                targetResolution
            )
        val notificationTitle = getApplication<SharkPlayer>()
            .getString(R.string.rescale_notification_title)

        val inputData = FFMpegWorker.getWorkData(
            cmd,
            notificationTitle = notificationTitle,
            notificationContent = notificationContent,
            targetFilePath = outputPath
        )
        val rescaleWorkRequest: WorkRequest = OneTimeWorkRequestBuilder<FFMpegWorker>()
            .setInputData(inputData)
            .addTag(notificationContent)
            .build()
        WorkManager.getInstance(getApplication<SharkPlayer>().applicationContext)
            .enqueue(rescaleWorkRequest)
    }

    fun refresh() {
        loadDirectory()
    }

    fun deleteVideo(videoFile: SharkPlayerFile.VideoFile) {
        _uiState.setLoading()
        viewModelScope.launch {
            when (val result = directoryRepository.deleteVideo(videoFile)) {
                is TaskState.Failure -> _uiState.setError(result.error)
                is TaskState.Success -> {
                    refresh()
                    _uiState.setIdle()
                }
            }
        }
    }

    companion object {
        const val PATH_KEY = "path"
    }
}
```  
# modules\home\adapters\HomeDirectoriesAdapter.kt  
```kt
package com.sharkaboi.sharkplayer.modules.home.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sharkaboi.sharkplayer.R
import com.sharkaboi.sharkplayer.common.models.SharkPlayerFile
import com.sharkaboi.sharkplayer.databinding.ItemDirectoryFileBinding
import me.saket.cascade.CascadePopupMenu

class HomeDirectoriesAdapter(
    private val isHomeDirs: Boolean,
    private val onItemClick: (SharkPlayerFile.Directory) -> Unit,
    private val onItemRemove: (SharkPlayerFile.Directory) -> Unit
) : ListAdapter<SharkPlayerFile.Directory, HomeDirectoriesAdapter.HomeDirectoriesViewHolder>(
    diffUtilItemCallback
) {

    private lateinit var binding: ItemDirectoryFileBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeDirectoriesViewHolder {
        binding = ItemDirectoryFileBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HomeDirectoriesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HomeDirectoriesViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    inner class HomeDirectoriesViewHolder(
        private val binding: ItemDirectoryFileBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.tvDetails.isGone = true
            binding.ibMore.isGone = isHomeDirs
        }

        fun bind(item: SharkPlayerFile.Directory) {
            binding.root.setOnClickListener { onItemClick(item) }
            binding.tvName.text = item.folderName
            binding.tvName.isSelected = true

            if (!isHomeDirs) {
                binding.ibMore.setOnClickListener {
                    val menu = CascadePopupMenu(it.context, it)
                    menu.inflate(R.menu.favorites_options_menu)
                    menu.setOnMenuItemClickListener { menuItem ->
                        if (menuItem.itemId == R.id.remove_item) {
                            onItemRemove(item)
                        }
                        true
                    }
                    menu.show()
                }
            }
        }
    }
}

private val diffUtilItemCallback = object : DiffUtil.ItemCallback<SharkPlayerFile.Directory>() {
    override fun areItemsTheSame(
        oldItem: SharkPlayerFile.Directory,
        newItem: SharkPlayerFile.Directory
    ): Boolean {
        return oldItem.absolutePath == newItem.absolutePath
    }

    override fun areContentsTheSame(
        oldItem: SharkPlayerFile.Directory,
        newItem: SharkPlayerFile.Directory
    ): Boolean {
        return oldItem == newItem
    }
}
```  
# modules\home\adapters\HomeHintAdapter.kt  
```kt
package com.sharkaboi.sharkplayer.modules.home.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sharkaboi.sharkplayer.databinding.ItemHomeHintBinding

class HomeHintAdapter(private val hintText: String) :
    RecyclerView.Adapter<HomeHintAdapter.HomeHintViewHolder>() {

    private lateinit var binding: ItemHomeHintBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeHintViewHolder {
        binding = ItemHomeHintBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HomeHintViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HomeHintViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int = 1

    inner class HomeHintViewHolder(
        private val binding: ItemHomeHintBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            binding.tvHomeHint.text = hintText
        }
    }
}

```  
# modules\home\repo\HomeRepository.kt  
```kt
package com.sharkaboi.sharkplayer.modules.home.repo

import com.sharkaboi.sharkplayer.common.models.SharkPlayerFile
import com.sharkaboi.sharkplayer.common.util.TaskState
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
    val favorites: Flow<List<SharkPlayerFile.Directory>>
    suspend fun removeFavorite(favorite: SharkPlayerFile.Directory): TaskState<Unit>
}

```  
# modules\home\repo\HomeRepositoryImpl.kt  
```kt
package com.sharkaboi.sharkplayer.modules.home.repo

import com.sharkaboi.sharkplayer.common.extensions.tryCatching
import com.sharkaboi.sharkplayer.common.models.SharkPlayerFile
import com.sharkaboi.sharkplayer.common.util.TaskState
import com.sharkaboi.sharkplayer.data.datastore.DataStoreRepository
import kotlinx.coroutines.flow.Flow
import java.io.File

class HomeRepositoryImpl(
    private val dataStoreRepository: DataStoreRepository
) : HomeRepository {
    override val favorites: Flow<List<SharkPlayerFile.Directory>> =
        dataStoreRepository.favouritesDirsFlow

    override suspend fun removeFavorite(favorite: SharkPlayerFile.Directory): TaskState<Unit> =
        tryCatching {
            TaskState.Success(dataStoreRepository.removeFavorite(favorite))
        }
}
```  
# modules\home\ui\HomeFragment.kt  
```kt
package com.sharkaboi.sharkplayer.modules.home.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import com.sharkaboi.sharkplayer.BottomNavGraphDirections
import com.sharkaboi.sharkplayer.common.extensions.getDefaultDirectories
import com.sharkaboi.sharkplayer.common.extensions.initLinearDefaults
import com.sharkaboi.sharkplayer.common.extensions.observe
import com.sharkaboi.sharkplayer.common.extensions.showToast
import com.sharkaboi.sharkplayer.common.models.SharkPlayerFile
import com.sharkaboi.sharkplayer.databinding.FragmentHomeBinding
import com.sharkaboi.sharkplayer.modules.home.adapters.HomeDirectoriesAdapter
import com.sharkaboi.sharkplayer.modules.home.adapters.HomeHintAdapter
import com.sharkaboi.sharkplayer.modules.home.vm.HomeState
import com.sharkaboi.sharkplayer.modules.home.vm.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var favoritesAdapter: HomeDirectoriesAdapter
    private val homeViewModel by viewModels<HomeViewModel>()
    private val navController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvHomeDirectories.adapter = null
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        setObservers()
    }

    private fun initViews() {
        setupHomeDirsList()
    }

    private fun setupHomeDirsList() {
        val defaultFileDirs = requireContext().getDefaultDirectories()
        val rvHomeDirectories = binding.rvHomeDirectories
        rvHomeDirectories.initLinearDefaults(context)
        val homeHintAdapter = HomeHintAdapter("Home")
        val homeDirAdapter = HomeDirectoriesAdapter(
            isHomeDirs = true,
            onItemClick = { item ->
                openDirectory(item)
            },
            onItemRemove = { }
        )
        homeDirAdapter.submitList(defaultFileDirs)
        val favsHintAdapter = HomeHintAdapter("Favorites")
        favoritesAdapter = HomeDirectoriesAdapter(
            isHomeDirs = false,
            onItemClick = { item ->
                openDirectory(item)
            },
            onItemRemove = { item ->
                homeViewModel.removeFavorite(item)
            }
        )
        favoritesAdapter.submitList(defaultFileDirs)
        rvHomeDirectories.adapter = ConcatAdapter(
            homeHintAdapter,
            homeDirAdapter,
            favsHintAdapter,
            favoritesAdapter
        )
    }

    private fun setObservers() {
        observe(homeViewModel.favorites) { favorites ->
            favoritesAdapter.submitList(favorites)
        }
        observe(homeViewModel.uiState) { state ->
            binding.progress.isVisible = state is HomeState.Loading
            when (state) {
                is HomeState.Failure -> showToast(state.message)
                else -> Unit
            }
        }
    }

    private fun openDirectory(item: SharkPlayerFile.Directory) {
        val action = BottomNavGraphDirections.openDirectory(path = item.path)
        navController.navigate(action)
    }
}
```  
# modules\home\vm\HomeState.kt  
```kt
package com.sharkaboi.sharkplayer.modules.home.vm

import androidx.lifecycle.MutableLiveData
import com.sharkaboi.sharkplayer.common.extensions.emptyString

sealed class HomeState {
    object Idle : HomeState()
    object Loading : HomeState()
    data class Failure(val message: String) : HomeState()
}

internal fun MutableLiveData<HomeState>.setError(message: String) {
    this.value = HomeState.Failure(message)
}

internal fun MutableLiveData<HomeState>.setError(exception: Exception) {
    this.value = HomeState.Failure(exception.message ?: String.emptyString)
}

internal fun MutableLiveData<HomeState>.setIdle() {
    this.value = HomeState.Idle
}

internal fun MutableLiveData<HomeState>.setLoading() {
    this.value = HomeState.Loading
}

internal fun MutableLiveData<HomeState>.getDefault() = this.apply {
    this.setIdle()
}

```  
# modules\home\vm\HomeViewModel.kt  
```kt
package com.sharkaboi.sharkplayer.modules.home.vm

import androidx.lifecycle.*
import com.sharkaboi.sharkplayer.common.models.SharkPlayerFile
import com.sharkaboi.sharkplayer.common.util.TaskState
import com.sharkaboi.sharkplayer.modules.directory.vm.setLoading
import com.sharkaboi.sharkplayer.modules.home.repo.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
@Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {
    private val _uiState = MutableLiveData<HomeState>().getDefault()
    val uiState: LiveData<HomeState> = _uiState
    val favorites = homeRepository.favorites.asLiveData()

    fun removeFavorite(favorite: SharkPlayerFile.Directory) {
        _uiState.setLoading()
        viewModelScope.launch {
            when (val result = homeRepository.removeFavorite(favorite)) {
                is TaskState.Failure -> _uiState.setError(result.error)
                is TaskState.Success -> _uiState.setIdle()
            }
        }
    }
}
```  
# modules\main\ui\MainActivity.kt  
```kt
package com.sharkaboi.sharkplayer.modules.main.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.sharkaboi.sharkplayer.R
import com.sharkaboi.sharkplayer.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navController = findNavController(R.id.fragmentContainer)
        setupBottomNav()
    }

    private fun setupBottomNav() {
        binding.bottomNavigationView.setupWithNavController(navController)
    }
}
```  
# modules\settings\ui\SettingsFragment.kt  
```kt
package com.sharkaboi.sharkplayer.modules.settings.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.sharkaboi.sharkplayer.BuildConfig
import com.sharkaboi.sharkplayer.R
import com.sharkaboi.sharkplayer.data.sharedpref.SharedPrefKeys
import com.sharkaboi.sharkplayer.modules.settings.vm.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {

    private val settingsViewModel by viewModels<SettingsViewModel>()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        setupObservers()
    }

    private fun setupObservers() {
    }

    private fun setupListeners() {
        findPreference<Preference>(SharedPrefKeys.ABOUT)?.summaryProvider =
            Preference.SummaryProvider { _: Preference ->
                getString(R.string.version, BuildConfig.VERSION_NAME)
            }
        findPreference<SwitchPreference>(SharedPrefKeys.DARK_THEME)?.setOnPreferenceChangeListener { _, newValue ->
            AppCompatDelegate.setDefaultNightMode(
                if (newValue == true)
                    AppCompatDelegate.MODE_NIGHT_YES
                else
                    AppCompatDelegate.MODE_NIGHT_NO
            )
            true
        }
    }

}
```  
# modules\settings\vm\SettingsState.kt  
```kt
package com.sharkaboi.sharkplayer.modules.settings.vm

import androidx.lifecycle.MutableLiveData
import com.sharkaboi.appupdatechecker.models.UpdateState
import com.sharkaboi.sharkplayer.common.extensions.emptyString


sealed class SettingsState {

    object Idle : SettingsState()

    object Loading : SettingsState()

    data class Failure(val message: String) : SettingsState()

    data class Success(val message: UpdateState.UpdateAvailable) : SettingsState()

}

internal fun MutableLiveData<SettingsState>.setError(message: String) {
    this.value = SettingsState.Failure(message)
}

internal fun MutableLiveData<SettingsState>.setError(exception: Exception) {
    this.value = SettingsState.Failure(exception.message ?: String.emptyString)
}

internal fun MutableLiveData<SettingsState>.setSuccess(data: UpdateState.UpdateAvailable) {
    this.value = SettingsState.Success(data)
}

internal fun MutableLiveData<SettingsState>.setIdle() {
    this.value = SettingsState.Idle
}

internal fun MutableLiveData<SettingsState>.setLoading() {
    this.value = SettingsState.Loading
}

internal fun MutableLiveData<SettingsState>.getDefault() = this.apply {
    this.setIdle()
}
```  
# modules\settings\vm\SettingsViewModel.kt  
```kt
package com.sharkaboi.sharkplayer.modules.settings.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharkaboi.appupdatechecker.AppUpdateChecker
import com.sharkaboi.appupdatechecker.models.UpdateState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
@Inject constructor(
    private val appUpdateChecker: AppUpdateChecker
) : ViewModel() {
    private val _uiState = MutableLiveData<SettingsState>().getDefault()
    val uiState: LiveData<SettingsState> = _uiState

    fun checkUpdate() {
        viewModelScope.launch {
            _uiState.setLoading()
            when (val updateState = appUpdateChecker.checkUpdate()) {
                is UpdateState.UpdateAvailable -> _uiState.setSuccess(updateState)
                UpdateState.LatestVersionInstalled -> _uiState.setError("Latest version already installed")
                UpdateState.GithubInvalid -> _uiState.setError("Could not fetch update info, Try again later")
                UpdateState.NoNetworkFound -> _uiState.setError("No network found")
                else -> _uiState.setError("An error occurred")
            }
        }
    }

}

```  
# modules\splash\ui\SplashActivity.kt  
```kt
package com.sharkaboi.sharkplayer.modules.splash.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.sharkaboi.sharkplayer.R
import com.sharkaboi.sharkplayer.common.constants.AppConstants
import com.sharkaboi.sharkplayer.common.extensions.launchAndFinishAffinity
import com.sharkaboi.sharkplayer.common.extensions.showToast
import com.sharkaboi.sharkplayer.data.sharedpref.SharedPrefRepository
import com.sharkaboi.sharkplayer.modules.main.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { isGranted ->
            handlePermissionResult(isGranted)
        }

    @Inject
    lateinit var sharedPrefRepository: SharedPrefRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions()
        configTheme()
    }

    private fun checkPermissions() {
        requestPermissions.launch(AppConstants.requiredPermissions)
    }

    private fun configTheme() {
        val isDarkTheme = sharedPrefRepository.isDarkTheme()
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkTheme)
                AppCompatDelegate.MODE_NIGHT_YES
            else
                AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun openAppFlow() = launchAndFinishAffinity<MainActivity>()

    private fun handlePermissionResult(grantedMap: Map<String, Boolean>) {
        var isGranted = true
        grantedMap.forEach { isGranted = isGranted && it.value }

        if (grantedMap.isEmpty()) {
            showToast(R.string.permissions_hint)
            checkPermissions()
            return
        }

        if (!isGranted) {
            showToast(R.string.permissions_hint)
            checkPermissions()
            return
        }

        openAppFlow()
    }
}
```  
# modules\workers\adapters\WorkersAdapter.kt  
```kt
package com.sharkaboi.sharkplayer.modules.workers.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkInfo
import com.sharkaboi.sharkplayer.databinding.ItemWorkersBinding
import com.sharkaboi.sharkplayer.ffmpeg.workers.FFMpegWorker

class WorkersAdapter : RecyclerView.Adapter<WorkersAdapter.WorkersViewHolder>() {

    private val diffUtilItemCallback = object : DiffUtil.ItemCallback<WorkInfo>() {
        override fun areItemsTheSame(oldItem: WorkInfo, newItem: WorkInfo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: WorkInfo, newItem: WorkInfo): Boolean {
            return oldItem == newItem
        }
    }

    private val listDiffer = AsyncListDiffer(this, diffUtilItemCallback)

    private lateinit var binding: ItemWorkersBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkersViewHolder {
        binding = ItemWorkersBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WorkersViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WorkersViewHolder, position: Int) {
        holder.bind(listDiffer.currentList[position])
    }

    override fun getItemCount(): Int = listDiffer.currentList.size

    fun submitList(list: List<WorkInfo>) {
        listDiffer.submitList(list)
    }

    class WorkersViewHolder(
        private val binding: ItemWorkersBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: WorkInfo) {
            binding.tvTitle.text =
                item.tags.firstOrNull { it !in FFMpegWorker.packages } ?: "Unnamed Work Task"
            binding.tvDetails.text = ("ID : ${item.id}")
            binding.tvState.text = item.state.toString()
        }
    }
}

```  
# modules\workers\ui\WorkersFragment.kt  
```kt
package com.sharkaboi.sharkplayer.modules.workers.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.sharkaboi.sharkplayer.common.extensions.initLinearDefaults
import com.sharkaboi.sharkplayer.common.extensions.observe
import com.sharkaboi.sharkplayer.databinding.FragmentWorkersBinding
import com.sharkaboi.sharkplayer.modules.workers.adapters.WorkersAdapter
import com.sharkaboi.sharkplayer.modules.workers.vm.WorkersViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WorkersFragment : Fragment() {
    private lateinit var workersAdapter: WorkersAdapter
    private var _binding: FragmentWorkersBinding? = null
    private val binding get() = _binding!!
    private val workersViewModel by viewModels<WorkersViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkersBinding.inflate(inflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvWorkers.adapter = null
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        setObservers()
    }

    private fun initViews() {
        setupRecyclerViews()
    }

    private fun setupRecyclerViews() {
        workersAdapter = WorkersAdapter()
        binding.rvWorkers.adapter = workersAdapter
        binding.rvWorkers.initLinearDefaults(context, hasFixedSize = true)
    }

    private fun setObservers() {
        observe(workersViewModel.workers) { workers ->
            binding.tvNoWorkers.isVisible = workers.isEmpty()
            workersAdapter.submitList(workers)
        }
    }
}
```  
# modules\workers\vm\WorkersViewModel.kt  
```kt
package com.sharkaboi.sharkplayer.modules.workers.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import com.sharkaboi.sharkplayer.SharkPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WorkersViewModel
@Inject constructor(
    app: Application
) : AndroidViewModel(app) {
    val workers: LiveData<List<WorkInfo>> =
        WorkManager.getInstance(getApplication<SharkPlayer>().applicationContext)
            .getWorkInfosLiveData(
                WorkQuery.Builder.fromStates(
                    listOf(
                        WorkInfo.State.RUNNING,
                        WorkInfo.State.ENQUEUED,
                        WorkInfo.State.SUCCEEDED
                    )
                ).build()
            ).map { it.toList() }
}
```  
# SharkPlayer.kt  
```kt
package com.sharkaboi.sharkplayer

import androidx.hilt.work.HiltWorkerFactory
import androidx.multidex.MultiDexApplication
import androidx.work.Configuration
import coil.Coil
import coil.ImageLoader
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class SharkPlayer : MultiDexApplication(), Configuration.Provider {

    @Inject
    lateinit var coilImageLoader: ImageLoader

    override fun onCreate() {
        super.onCreate()
        Coil.setImageLoader(coilImageLoader)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

}
```