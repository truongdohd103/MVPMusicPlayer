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

class MainActivity : AppCompatActivity(), MusicView {
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnPlayPause: ImageView
    private lateinit var presenter: MusicPresenter
    private lateinit var adapter: MusicAdapter
    private lateinit var tvCurrentSong: TextView
    private lateinit var tvCurrentArtist: TextView
    private lateinit var imgCurrentSong: ImageView

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
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MusicAdapter(emptyList()) { song -> presenter.onSongSelected(song) }
        recyclerView.adapter = adapter

        tvCurrentSong = findViewById(R.id.tvCurrentSong)
        tvCurrentArtist = findViewById(R.id.tvCurrentArtist)
        imgCurrentSong = findViewById(R.id.imgCurrentSong)

        presenter = MusicPresenter(this, this, MusicModel())
        presenter.loadSongs()

        btnPlayPause.setOnClickListener {
            presenter.togglePlayPause()
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
}

interface MusicView {
    fun showSongs(songs: List<Song>)
    fun updatePlayButton(isPlaying: Boolean)
    fun updateMiniPlayer(song: Song)
}
