package kr.hyperlink.app.sample.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kr.hyperlink.app.sample.MainActivity
import kr.hyperlink.app.sample.R

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class MessagingService : FirebaseMessagingService()  {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.let {
            it.data.let { item ->
                val title = item["title"]
                val requestID = System.currentTimeMillis().toInt()
                val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                notificationManager.cancelAll()

                val builder: Notification.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val importance = NotificationManager.IMPORTANCE_HIGH
                    val mChannel = NotificationChannel(title, item["body"], importance)
                    notificationManager.createNotificationChannel(mChannel)
                    Notification.Builder(this, title)
                } else {
                    Notification.Builder(this)
                }


                var count = 0
                item["badge"]?.let{ badge ->
                    count = badge.toInt()
                }

                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                item["link"]?.let { link ->
                    if(link.isNotEmpty()) {
                        intent.putExtra("link", link)
                    }
                }

                val pendingIntent : PendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.getActivity(
                        this@MessagingService,
                        System.currentTimeMillis().toInt(),
                        intent,
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                } else {
                    PendingIntent.getActivity(
                        this@MessagingService,
                        System.currentTimeMillis().toInt(),
                        intent,
                        PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                }
                builder
                    .setSmallIcon(R.drawable.ic_stat_circle_notifications)
                    .setTicker(title)
                    .setWhen(System.currentTimeMillis())
                    .setNumber(count)
                    .setContentTitle(title)
                    .setContentText(item["body"])
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
                    builder.setSmallIcon(R.drawable.ic_stat_circle_notifications)
                    builder.setColor(ContextCompat.getColor(this@MessagingService, R.color.black))
                } else {
                    builder.setSmallIcon(R.drawable.ic_stat_circle_notifications)
                }
                notificationManager.notify(requestID, builder.build())
            }
        }
    }
}