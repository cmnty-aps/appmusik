package com.example.database

import kotlinx.coroutines.flow.Flow

class PlaylistRepository(private val playlistDao: PlaylistDao) {
    val playlistsWithSongs: Flow<List<PlaylistWithSongs>> = playlistDao.getPlaylistsWithSongs()

    fun getPlaylistWithSongsById(playlistId: String): Flow<PlaylistWithSongs?> {
        return playlistDao.getPlaylistWithSongsById(playlistId)
    }

    suspend fun createPlaylist(id: String, name: String, image: String? = null) {
        playlistDao.insertPlaylist(PlaylistEntity(id = id, name = name, image = image))
    }

    suspend fun deletePlaylist(playlistId: String) {
        playlistDao.deletePlaylistAndSongs(playlistId)
    }

    suspend fun addSongToPlaylist(
        playlistId: String,
        videoId: String,
        title: String,
        artist: String,
        artistId: String? = null,
        thumbnail: String? = null,
        url: String? = null
    ) {
        playlistDao.insertSong(
            PlaylistSongEntity(
                playlistId = playlistId,
                videoId = videoId,
                title = title,
                artist = artist,
                artistId = artistId,
                thumbnail = thumbnail,
                url = url
            )
        )
    }

    suspend fun removeSongFromPlaylist(playlistId: String, videoId: String) {
        playlistDao.deleteSongFromPlaylist(playlistId, videoId)
    }

    // History
    val history: Flow<List<HistoryEntity>> = playlistDao.getHistory()

    suspend fun addSongToHistory(
        videoId: String,
        title: String,
        artist: String,
        artistId: String? = null,
        thumbnail: String? = null,
        url: String? = null
    ) {
        playlistDao.insertHistory(
            HistoryEntity(
                videoId = videoId,
                title = title,
                artist = artist,
                artistId = artistId,
                thumbnail = thumbnail,
                url = url
            )
        )
    }

    // Downloads
    val downloads: Flow<List<DownloadEntity>> = playlistDao.getDownloads()

    suspend fun addSongToDownloads(
        videoId: String,
        title: String,
        artist: String,
        artistId: String? = null,
        thumbnail: String? = null,
        url: String? = null
    ) {
        playlistDao.insertDownload(
            DownloadEntity(
                videoId = videoId,
                title = title,
                artist = artist,
                artistId = artistId,
                thumbnail = thumbnail,
                url = url
            )
        )
    }

    suspend fun removeSongFromDownloads(videoId: String) {
        playlistDao.deleteDownload(videoId)
    }
}
