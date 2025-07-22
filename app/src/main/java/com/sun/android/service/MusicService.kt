package com.sun.android.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import com.sun.android.view.MainActivity
import com.sun.structure_android.R
import android.util.Log

class MusicService : Service(){
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var notitficationManager: NotificationManager
    private var channelId = "MusicChannel"
    override fun onBind(p0: Intent?): IBinder? = null
    override fun onCreate() {
        super.onCreate()
        Log.d("MusicService", "onCreate called")
        notitficationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Music Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                setShowBadge(true)
            }
            notitficationManager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("MusicService", "onStartCommand called")
        val resourceId = intent?.getIntExtra("resourceId", R.raw.song1) ?: R.raw.song1
        val title = intent?.getStringExtra("title") ?: "Unknown"
        val artist = intent?.getStringExtra("artist") ?: "Unknown"
        val imageResId = intent?.getIntExtra("imageResId", R.drawable.default_img) ?: R.drawable.default_img
        Log.d("MusicService", "Received: resourceId=$resourceId, title=$title, artist=$artist, imageResId=$imageResId")
        startForeground(1, buildNotification(title, artist, imageResId))
        playMusic(resourceId)
        return START_NOT_STICKY
    }

    private fun playMusic(resourceId: Int) {
        Log.d("MusicService", "playMusic called with resourceId: $resourceId")
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(this, resourceId)
        if (mediaPlayer == null) {
            Log.e("MusicService", "MediaPlayer.create returned null!")
        }
        mediaPlayer?.setOnErrorListener { mp, what, extra ->
            Log.e("MusicService", "MediaPlayer error: what=$what, extra=$extra")
            false
        }
        mediaPlayer?.start()
    }

    @SuppressLint("NewApi")
    private fun buildNotification(title: String, artist: String, imageResId: Int): Notification {
        Log.d("MusicService", "buildNotification called with title=$title, artist=$artist, imageResId=$imageResId")
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return Notification.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(artist)
            .setSmallIcon(imageResId)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        stopForeground(true)
    }
}
