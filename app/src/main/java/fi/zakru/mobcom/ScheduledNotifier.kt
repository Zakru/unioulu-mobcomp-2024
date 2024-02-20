package fi.zakru.mobcom

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class ScheduledNotifier : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("alarm", "Alarm fired")

        if (context == null) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, MainActivity.NOTIFICATIION_CHANNEL)
            .setSmallIcon(R.drawable.z_circle)
            .setContentTitle("Hi!")
            .setContentText("10 seconds have now passed.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notification = builder.build()

        with (NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return@with
            }

            notify(MainActivity.NOTIFICATIION_ID, notification)
        }
    }
}
