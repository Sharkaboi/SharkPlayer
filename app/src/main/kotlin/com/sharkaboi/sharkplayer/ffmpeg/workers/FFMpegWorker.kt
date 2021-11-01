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
import timber.log.Timber

@HiltWorker
class FFMpegWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val ffMpegDataSource: FFMpegDataSource
) : CoroutineWorker(context, params) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager

    override suspend fun doWork(): Result {
        val cmd = inputData.getStringArray(FFMPEG_CMD_KEY)
            ?: return Result.failure()

        val notificationTitle = inputData.getString(NOTIFICATION_TITLE_KEY)
            ?: return Result.failure()

        val content = inputData.getString(NOTIFICATION_CONTENT_KEY)
            ?: String.emptyString

        val foregroundInfo = createForegroundInfo(notificationTitle, content)
        setForeground(foregroundInfo)

        Timber.d("cmd : ${cmd.joinToString()}")
        Timber.d("content : $content")

        val result = ffMpegDataSource.loadBinary()
        if (result.isFailure) {
            Timber.d("Couldn't load ffmpeg lib due to ${(result as TaskState.Failure).error.message}")
            return Result.failure()
        }

        return when (val operationResult = ffMpegDataSource.execute(cmd)) {
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
        private const val NOTIFICATION_ID = 42069
        val packages = setOf(
            "com.sharkaboi.sharkplayer.ffmpeg.workers.FFMpegWorker",
            "com.sharkaboi.sharkplayer.ffmpeg.workers.RescaleVideoWorker"
        )

        fun getWorkData(
            cmd: FFMpegCommand,
            notificationTitle: String,
            notificationContent: String,
        ): Data {
            return workDataOf(
                FFMPEG_CMD_KEY to cmd,
                NOTIFICATION_CONTENT_KEY to notificationContent,
                NOTIFICATION_TITLE_KEY to notificationTitle,
            )
        }
    }
}
