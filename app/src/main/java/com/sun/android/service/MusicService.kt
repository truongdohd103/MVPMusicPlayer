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
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle

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
            "ACTION_SEEK_FROM_NOTIFICATION" -> {
                val seekTo = intent.getIntExtra(androidx.core.app.NotificationCompat.EXTRA_PROGRESS, 0)
                val wasPlaying = mediaPlayer?.isPlaying == true
                mediaPlayer?.seekTo(seekTo)
                if (wasPlaying) mediaPlayer?.start()
                updateNotification()
                return START_NOT_STICKY
            }
            "ACTION_PLAY" -> {
                mediaPlayer?.start()
                updateNotification()
                return START_NOT_STICKY
            }
            "ACTION_PAUSE" -> {
                mediaPlayer?.pause()
                updateNotification()
                return START_NOT_STICKY
            }
            "ACTION_NEXT" -> {
                val intentNext = Intent("com.sun.android.ACTION_NEXT_SONG")
                intentNext.setPackage(packageName)
                sendBroadcast(intentNext)
                return START_NOT_STICKY
            }
            "ACTION_PREV" -> {
                val intentPrev = Intent("com.sun.android.ACTION_PREV_SONG")
                intentPrev.setPackage(packageName)
                sendBroadcast(intentPrev)
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
        } else {
            updateNotification()
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

    private fun updateNotification() {
        val title = "" // Có thể lưu lại title hiện tại nếu cần
        val artist = "" // Có thể lưu lại artist hiện tại nếu cần
        val imageResId = R.drawable.default_img // Có thể lưu lại imageResId hiện tại nếu cần
        val current = mediaPlayer?.currentPosition ?: 0
        val duration = mediaPlayer?.duration ?: 0
        val notification = buildNotification(title, artist, imageResId, current, duration)
        notitficationManager.notify(1, notification)
    }

    @SuppressLint("NewApi")
    private fun buildNotification(title: String, artist: String, imageResId: Int, current: Int = 0, duration: Int = 0): Notification {
        Log.d("MusicService", "buildNotification called with title=$title, artist=$artist, imageResId=$imageResId")
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val playIntent = Intent(this, MusicService::class.java).setAction("ACTION_PLAY")
        val playPendingIntent = PendingIntent.getService(this, 1, playIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val pauseIntent = Intent(this, MusicService::class.java).setAction("ACTION_PAUSE")
        val pausePendingIntent = PendingIntent.getService(this, 2, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val nextIntent = Intent(this, MusicService::class.java).setAction("ACTION_NEXT")
        val nextPendingIntent = PendingIntent.getService(this, 3, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val prevIntent = Intent(this, MusicService::class.java).setAction("ACTION_PREV")
        val prevPendingIntent = PendingIntent.getService(this, 4, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val isPlaying = mediaPlayer?.isPlaying == true
        val playPauseAction = if (isPlaying)
            NotificationCompat.Action(R.drawable.ic_pause, "Pause", pausePendingIntent)
        else
            NotificationCompat.Action(R.drawable.ic_play, "Play", playPendingIntent)

        val builder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(artist)
            .setSmallIcon(imageResId)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .setOngoing(isPlaying)
            .addAction(R.drawable.ic_prev, "Previous", prevPendingIntent)
            .addAction(playPauseAction)
            .addAction(R.drawable.ic_next, "Next", nextPendingIntent)
            .setStyle(
                MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
            )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU && duration > 0) {
            builder.setProgress(duration, current, false)
        }
        return builder.build()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        stopForeground(true)
        handler.removeCallbacks(updateRunnable)
    }
}
