package com.sun.android.model

import com.sun.structure_android.R

data class Song(
    val resourceId : Int,
    val title : String,
    val artist : String,
    val imageResId : Int
){
    class MusicModel {
        fun getSongList(): List<Song> {
            return listOf(
                Song(R.raw.song1, "Mùa Mưa Ấy", "Vũ", R.drawable.imgsong1),
                Song(R.raw.song2, "Phép Màu", "MAYDAYs, Minh Tốc & Lam", R.drawable.imgsong2),
                Song(R.raw.song3, "Treo", "The Cassette", R.drawable.imgsong3),
                Song(R.raw.song4, "Bầu Trời Mới", "Da LAB, Minh Tốc & Lam", R.drawable.imgsong4)
            )
        }
    }
}
