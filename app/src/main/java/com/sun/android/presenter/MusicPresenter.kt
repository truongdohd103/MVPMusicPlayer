package com.sun.android.presenter

import android.content.Context
import android.content.Intent
import com.sun.android.model.Song
import com.sun.android.model.Song.MusicModel
import com.sun.android.service.MusicService
import com.sun.android.view.MainActivity
import com.sun.android.view.MusicView

class MusicPresenter(private val context: Context, private val view: MusicView, private val model: MusicModel){
    private var isPlaying = false
    private var currentSong : Song? = null
    private val serviceIntent = Intent(context, MusicService::class.java)

    init {
    }

    fun loadSongs(){
        val songs = model.getSongList()
        view.showSongs(songs)
    }
    fun onSongSelected(song: Song){
        currentSong = song
        startMusicServiceWithSong(song)
        view.updatePlayButton(true)
        view.updateMiniPlayer(song)
        isPlaying = true
    }

    private fun startMusicServiceWithSong(song: Song) {
        val intent = Intent(context.applicationContext, MusicService::class.java)
        intent.putExtra("resourceId", song.resourceId)
        intent.putExtra("title", song.title)
        intent.putExtra("artist", song.artist)
        intent.putExtra("imageResId", song.imageResId)
        context.applicationContext.startService(intent)
    }

    fun togglePlayPause(){
        if (isPlaying){
            stopService()
            view.updatePlayButton(false)
        }else if (currentSong != null){
            startMusicServiceWithSong(currentSong!!)
            view.updatePlayButton(true)
        }
        isPlaying = !isPlaying
    }

    private fun stopService() {
        context.stopService(serviceIntent)
    }
}
