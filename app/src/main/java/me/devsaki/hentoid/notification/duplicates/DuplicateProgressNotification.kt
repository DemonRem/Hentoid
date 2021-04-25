package me.devsaki.hentoid.notification.duplicates

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import me.devsaki.hentoid.R
import me.devsaki.hentoid.receiver.DownloadNotificationPauseReceiver
import me.devsaki.hentoid.receiver.DuplicateNotificationStopReceiver
import me.devsaki.hentoid.util.ThemeHelper
import me.devsaki.hentoid.util.notification.Notification
import java.util.*

class DuplicateProgressNotification(
        private val progress: Int,
        private val max: Int
) : Notification {

    private val progressString: String = " %.2f%%".format(Locale.US, progress * 100f / max)

    override fun onCreateNotification(context: Context): android.app.Notification {
        return NotificationCompat.Builder(context, DuplicateNotificationChannel.ID)
                .setSmallIcon(R.drawable.ic_hentoid_shape)
                .setContentTitle(context.resources.getString(R.string.duplicate_processing))
                .setContentText(progressString)
                .setProgress(max, progress, false)
                .setColor(ThemeHelper.getColor(context, R.color.secondary_light))
                .addAction(R.drawable.ic_action_pause, context.getString(R.string.stop), getStopIntent(context))
                .setLocalOnly(true)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .build()
    }

    private fun getStopIntent(context: Context): PendingIntent {
        val intent = Intent(context, DuplicateNotificationStopReceiver::class.java)
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
    }
}
