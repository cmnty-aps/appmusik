package com.example.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

data class PlaylistWithSongs(
    @Embedded val playlist: PlaylistEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "playlistId"
    )
    val songs: List<PlaylistSongEntity>
)

@Dao
interface PlaylistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylistOnly(playlistId: String)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun deleteSongsByPlaylistId(playlistId: String)

    @Transaction
    suspend fun deletePlaylistAndSongs(playlistId: String) {
        deleteSongsByPlaylistId(playlistId)
        deletePlaylistOnly(playlistId)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: PlaylistSongEntity)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND videoId = :videoId")
    suspend fun deleteSongFromPlaylist(playlistId: String, videoId: String)

    @Transaction
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getPlaylistsWithSongs(): Flow<List<PlaylistWithSongs>>

    @Transaction
    @Query("SELECT * FROM playlists WHERE id = :playlistId LIMIT 1")
    fun getPlaylistWithSongsById(playlistId: String): Flow<PlaylistWithSongs?>

    // History methods
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity)

    @Query("SELECT * FROM history_songs ORDER BY playedAt DESC LIMIT 50")
    fun getHistory(): Flow<List<HistoryEntity>>

    // Download methods
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: DownloadEntity)

    @Query("DELETE FROM downloaded_songs WHERE videoId = :videoId")
    suspend fun deleteDownload(videoId: String)

    @Query("SELECT * FROM downloaded_songs ORDER BY downloadedAt DESC")
    fun getDownloads(): Flow<List<DownloadEntity>>
}
