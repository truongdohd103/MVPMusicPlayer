package com.sun.android.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.MediaPlayer
import android.os.IBinder
import com.sun.android.view.MainActivity
import com.sun.structure_android.R
import android.util.Log
import android.os.Handler
import android.os.Looper
import android.media.MediaPlayer.OnCompletionListener

class MusicService : Service(){
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var notitficationManager: NotificationManager
    private var channelId = "MusicChannel"
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 500L // ms
    private var currentResourceId: Int? = null
    private val updateRunnable = object : Runnable {
        override fun run() {
            mediaPlayer?.let {
                val intent = Intent("com.sun.android.ACTION_UPDATE_PROGRESS")
                intent.setPackage(packageName)
                intent.putExtra("currentPosition", it.currentPosition)
                intent.putExtra("duration", it.duration)
                sendBroadcast(intent)
            }
            handler.postDelayed(this, updateInterval)
        }
    }
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
        when (intent?.action) {
            "ACTION_SEEK_TO" -> {
                val seekTo = intent.getIntExtra("seekTo", 0)
                val wasPlaying = mediaPlayer?.isPlaying == true
                mediaPlayer?.seekTo(seekTo)
                if (wasPlaying) mediaPlayer?.start()
                return START_NOT_STICKY
            }
            "ACTION_PLAY" -> {
                mediaPlayer?.start()
                return START_NOT_STICKY
            }
            "ACTION_PAUSE" -> {
                mediaPlayer?.pause()
                return START_NOT_STICKY
            }
        }
        val resourceId = intent?.getIntExtra("resourceId", -1) ?: -1
        val title = intent?.getStringExtra("title") ?: "Unknown"
        val artist = intent?.getStringExtra("artist") ?: "Unknown"
        val imageResId = intent?.getIntExtra("imageResId", R.drawable.default_img) ?: R.drawable.default_img
        Log.d("MusicService", "Received: resourceId=$resourceId, title=$title, artist=$artist, imageResId=$imageResId")
        if (resourceId != -1 && resourceId != currentResourceId) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                startForeground(
                    1,
                    buildNotification(title, artist, imageResId),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(1, buildNotification(title, artist, imageResId))
            }
            playMusic(resourceId)
            currentResourceId = resourceId
        }
        handler.post(updateRunnable)
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
        mediaPlayer?.setOnCompletionListener {
            val intent = Intent("com.sun.android.ACTION_NEXT_SONG")
            intent.setPackage(packageName)
            sendBroadcast(intent)
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
        handler.removeCallbacks(updateRunnable)
    }
}
