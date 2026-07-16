package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val image: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlist_songs")
data class PlaylistSongEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val playlistId: String,
    val videoId: String,
    val title: String,
    val artist: String,
    val artistId: String? = null,
    val thumbnail: String? = null,
    val url: String? = null,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "history_songs")
data class HistoryEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val artist: String,
    val artistId: String? = null,
    val thumbnail: String? = null,
    val url: String? = null,
    val playedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "downloaded_songs")
data class DownloadEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val artist: String,
    val artistId: String? = null,
    val thumbnail: String? = null,
    val url: String? = null,
    val downloadedAt: Long = System.currentTimeMillis()
)
