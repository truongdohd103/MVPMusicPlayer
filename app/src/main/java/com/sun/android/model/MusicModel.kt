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
                Song(R.raw.song4, "Bầu Trời Mới", "Da LAB, Minh Tốc & Lam", R.drawable.imgsong4),
                Song(R.raw.song5, "Chốn Sa Mạc", "Minh Tốc & Lam", R.drawable.imgsong5),
                Song(R.raw.song6, "Vùng Ký Ức", "Chillies", R.drawable.imgsong6),
                Song(R.raw.song7, "Ghé Qua", "Dick x Tofu x PC", R.drawable.imgsong7),
                Song(R.raw.song8, "Lối Nhỏ", "Đen", R.drawable.imgsong8),
                Song(R.raw.song9, "Đoạn Kết Mới", "Hoàng Dũng", R.drawable.imgsong9),
                Song(R.raw.song10, "Nếu Ngày Mai Tôi Không Trở Về", "The Cassette", R.drawable.imgsong10),
            )
        }
    }
}
