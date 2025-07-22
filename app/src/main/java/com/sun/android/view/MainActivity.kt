package com.sun.android.view

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sun.android.adapter.MusicAdapter
import com.sun.android.model.Song
import com.sun.android.model.Song.MusicModel
import com.sun.android.presenter.MusicPresenter
import com.sun.structure_android.R
import android.widget.TextView
import android.widget.ImageView
import android.content.Context
import android.os.Build
import android.Manifest
import android.content.pm.PackageManager
import android.widget.SeekBar
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper

class MainActivity : AppCompatActivity(), MusicView {
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnPlayPause: ImageView
    private lateinit var btnNext: ImageView
    private lateinit var btnPrev: ImageView
    private lateinit var presenter: MusicPresenter
    private lateinit var adapter: MusicAdapter
    private lateinit var tvCurrentSong: TextView
    private lateinit var tvCurrentArtist: TextView
    private lateinit var imgCurrentSong: ImageView
    private lateinit var seekBar: SeekBar
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvDuration: TextView
    private var isUserSeeking = false
    private var currentDuration = 0
    private var progressReceiver: BroadcastReceiver? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Xin quyền POST_NOTIFICATIONS cho Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
            }
        }

        recyclerView = findViewById(R.id.recyclerView)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnNext = findViewById(R.id.btnNext)
        btnPrev = findViewById(R.id.btnPrev)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MusicAdapter(emptyList()) { song -> presenter.onSongSelected(song) }
        recyclerView.adapter = adapter

        tvCurrentSong = findViewById(R.id.tvCurrentSong)
        tvCurrentArtist = findViewById(R.id.tvCurrentArtist)
        imgCurrentSong = findViewById(R.id.imgCurrentSong)
        tvCurrentTime = findViewById(R.id.tvCurrentTime)
        tvDuration = findViewById(R.id.tvDuration)
        seekBar = findViewById(R.id.seekBar)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Không làm gì ở đây
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = true
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = false
                val seekTo = (seekBar?.progress ?: 0) * currentDuration / 100
                presenter.seekTo(seekTo)
            }
        })

        presenter = MusicPresenter(this, this, MusicModel())
        presenter.loadSongs()

        btnPlayPause.setOnClickListener {
            presenter.togglePlayPause()
        }
        btnNext.setOnClickListener {
            presenter.playNextSong()
        }
        btnPrev.setOnClickListener {
            presenter.playPrevSong()
        }
    }

    override fun onStart() {
        super.onStart()
        if (progressReceiver == null) {
            progressReceiver = object : BroadcastReceiver() {
                override fun onReceive(ctx: Context?, intent: android.content.Intent?) {
                    if (intent?.action == "com.sun.android.ACTION_UPDATE_PROGRESS") {
                        val current = intent.getIntExtra("currentPosition", 0)
                        val duration = intent.getIntExtra("duration", 0)
                        android.util.Log.d("MiniPlayer", "BroadcastReceiver: current=$current, duration=$duration")
                        mainHandler.post {
                            updateSeekBar(current, duration)
                        }
                    }
                    if (intent?.action == "com.sun.android.ACTION_NEXT_SONG") {
                        mainHandler.post {
                            presenter.playNextSong()
                        }
                    }
                }
            }
            val filter = IntentFilter()
            filter.addAction("com.sun.android.ACTION_UPDATE_PROGRESS")
            filter.addAction("com.sun.android.ACTION_NEXT_SONG")
            registerReceiver(progressReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        }
    }

    override fun onStop() {
        super.onStop()
        progressReceiver?.let {
            unregisterReceiver(it)
            progressReceiver = null
        }
    }

    override fun showSongs(songs: List<Song>) {
        adapter.updateSongs(songs)
    }

    override fun updatePlayButton(isPlaying: Boolean) {
        btnPlayPause.setImageResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
    }

    override fun updateMiniPlayer(song: Song) {
        tvCurrentSong.text = song.title
        tvCurrentArtist.text = song.artist
        imgCurrentSong.setImageResource(song.imageResId)
    }

    override fun updateSeekBar(current: Int, duration: Int) {
        android.util.Log.d("MiniPlayer", "updateSeekBar CALLED: current=$current, duration=$duration, isUserSeeking=$isUserSeeking")
        if (!isUserSeeking && duration > 0) {
            currentDuration = duration
            val percent = if (duration > 0) (current * 100 / duration) else 0
            seekBar.progress = percent
            tvCurrentTime.text = formatTime(current)
            tvDuration.text = formatTime(duration)
            android.util.Log.d("MiniPlayer", "updateSeekBar: current=$current, duration=$duration, percent=$percent")
        }
    }

    private fun formatTime(ms: Int): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}

interface MusicView {
    fun showSongs(songs: List<Song>)
    fun updatePlayButton(isPlaying: Boolean)
    fun updateMiniPlayer(song: Song)
    fun updateSeekBar(current: Int, duration: Int)
}
