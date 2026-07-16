package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.XmusicApi
import com.example.database.AppDatabase
import com.example.database.PlaylistRepository
import com.example.database.*
import com.example.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class Screen {
    HOME, SEARCH, LIBRARY
}

enum class RepeatMode {
    OFF, ALL, ONE
}

class XmusicViewModel(application: Application) : AndroidViewModel(application) {

    private val api = XmusicApi.create()
    private val database = AppDatabase.getDatabase(application)
    private val repository = PlaylistRepository(database.playlistDao())

    // UI state for current screen
    private val _currentScreen = MutableStateFlow(Screen.HOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Home Screen Data
    private val _homeTracks = MutableStateFlow<List<SongItem>>(emptyList())
    val homeTracks: StateFlow<List<SongItem>> = _homeTracks.asStateFlow()

    private val _homeLoading = MutableStateFlow(false)
    val homeLoading: StateFlow<Boolean> = _homeLoading.asStateFlow()

    private val _activeGenre = MutableStateFlow("Pop")
    val activeGenre: StateFlow<String> = _activeGenre.asStateFlow()

    // Search Screen Data
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions: StateFlow<List<String>> = _suggestions.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SongItem>>(emptyList())
    val searchResults: StateFlow<List<SongItem>> = _searchResults.asStateFlow()

    private val _searchLoading = MutableStateFlow(false)
    val searchLoading: StateFlow<Boolean> = _searchLoading.asStateFlow()

    // Playback State
    private val _currentTrack = MutableStateFlow<SongItem?>(null)
    val currentTrack: StateFlow<SongItem?> = _currentTrack.asStateFlow()

    private val _playQueue = MutableStateFlow<List<SongItem>>(emptyList())
    val playQueue: StateFlow<List<SongItem>> = _playQueue.asStateFlow()

    private val _queueIndex = MutableStateFlow(-1)
    val queueIndex: StateFlow<Int> = _queueIndex.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val _playbackPosition = MutableStateFlow(0f)
    val playbackPosition: StateFlow<Float> = _playbackPosition.asStateFlow()

    private val _playbackDuration = MutableStateFlow(0f)
    val playbackDuration: StateFlow<Float> = _playbackDuration.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _isAutoNext = MutableStateFlow(true)
    val isAutoNext: StateFlow<Boolean> = _isAutoNext.asStateFlow()

    // Player Overlay Open
    private val _isPlayerExpanded = MutableStateFlow(false)
    val isPlayerExpanded: StateFlow<Boolean> = _isPlayerExpanded.asStateFlow()

    // Lyrics State
    private val _currentLyrics = MutableStateFlow<LyricsData?>(null)
    val currentLyrics: StateFlow<LyricsData?> = _currentLyrics.asStateFlow()

    private val _isLyricsOpen = MutableStateFlow(false)
    val isLyricsOpen: StateFlow<Boolean> = _isLyricsOpen.asStateFlow()

    private val _lyricsLoading = MutableStateFlow(false)
    val lyricsLoading: StateFlow<Boolean> = _lyricsLoading.asStateFlow()

    private val _activeLyricIndex = MutableStateFlow(-1)
    val activeLyricIndex: StateFlow<Int> = _activeLyricIndex.asStateFlow()

    // Artist Detail Overlay State
    private val _activeArtistId = MutableStateFlow<String?>(null)
    val activeArtistId: StateFlow<String?> = _activeArtistId.asStateFlow()

    private val _activeArtistName = MutableStateFlow<String?>(null)
    val activeArtistName: StateFlow<String?> = _activeArtistName.asStateFlow()

    private val _artistDetail = MutableStateFlow<ArtistResult?>(null)
    val artistDetail: StateFlow<ArtistResult?> = _artistDetail.asStateFlow()

    private val _artistLoading = MutableStateFlow(false)
    val artistLoading: StateFlow<Boolean> = _artistLoading.asStateFlow()

    // Playlists (from Database)
    val playlistsWithSongs: StateFlow<List<PlaylistWithSongs>> = repository.playlistsWithSongs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val history: StateFlow<List<HistoryEntity>> = repository.history.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val downloads: StateFlow<List<DownloadEntity>> = repository.downloads.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Toast and notifications Flow
    private val _toastFlow = MutableSharedFlow<String>()
    val toastFlow: SharedFlow<String> = _toastFlow.asSharedFlow()

    // Native Player Reference
    private var youtubePlayer: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer? = null
    private var pendingVideoIdToPlay: String? = null

    init {
        fetchHomeTracks()
    }

    fun setYouTubePlayer(player: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer) {
        youtubePlayer = player
        pendingVideoIdToPlay?.let {
            player.loadVideo(it, 0f)
            pendingVideoIdToPlay = null
        }
    }

    fun switchScreen(screen: Screen) {
        _currentScreen.value = screen
    }

    fun setPlayerExpanded(expanded: Boolean) {
        _isPlayerExpanded.value = expanded
        // If expanding the player and no lyrics loaded, load them
        if (expanded && _isLyricsOpen.value) {
            _currentTrack.value?.let { fetchLyrics(it.videoId) }
        }
    }

    fun toggleLyrics() {
        val open = !_isLyricsOpen.value
        _isLyricsOpen.value = open
        if (open) {
            _currentTrack.value?.let { fetchLyrics(it.videoId) }
        }
    }

    // Home Operations
    fun fetchHomeTracks(genre: String? = null) {
        val targetGenre = genre ?: _activeGenre.value
        if (genre != null) {
            _activeGenre.value = genre
        }
        viewModelScope.launch {
            try {
                _homeLoading.value = true
                val query = "Lagu $targetGenre terbaru populer"
                val response = api.search(query)
                val songs = response.result?.songs ?: emptyList()
                _homeTracks.value = songs
            } catch (e: Exception) {
                e.printStackTrace()
                _toastFlow.emit("Gagal memuat rekomendasi musik")
            } finally {
                _homeLoading.value = false
            }
        }
    }

    // Search Operations
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.trim().isEmpty()) {
            _suggestions.value = emptyList()
        } else {
            fetchSuggestions(query)
        }
    }

    private fun fetchSuggestions(query: String) {
        viewModelScope.launch {
            try {
                val list = api.suggest(query)
                _suggestions.value = list
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun executeSearch(query: String) {
        if (query.trim().isEmpty()) return
        _searchQuery.value = query
        _searchLoading.value = true
        _suggestions.value = emptyList()
        viewModelScope.launch {
            try {
                val response = api.search(query)
                _searchResults.value = response.result?.songs ?: emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
                _searchResults.value = emptyList()
                _toastFlow.emit("Pencarian gagal")
            } finally {
                _searchLoading.value = false
            }
        }
    }

    // Artist Operations
    fun openArtist(id: String, name: String) {
        _activeArtistId.value = id
        _activeArtistName.value = name
        _artistLoading.value = true
        _artistDetail.value = null
        // Collapse player if open to focus on artist profile
        _isPlayerExpanded.value = false
        
        viewModelScope.launch {
            try {
                val response = api.getArtist(id)
                if (response.status) {
                    _artistDetail.value = response.result
                } else {
                    _toastFlow.emit("Gagal memuat profil artis")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _toastFlow.emit("Kesalahan memuat data artis")
            } finally {
                _artistLoading.value = false
            }
        }
    }

    fun closeArtist() {
        _activeArtistId.value = null
        _activeArtistName.value = null
        _artistDetail.value = null
    }

    // Lyrics Operations
    private fun fetchLyrics(videoId: String) {
        if (_currentLyrics.value != null && _activeLyricIndex.value != -1) return // Already loaded
        _lyricsLoading.value = true
        _currentLyrics.value = null
        _activeLyricIndex.value = -1
        viewModelScope.launch {
            try {
                val response = api.getLyrics(videoId)
                if (response.status) {
                    _currentLyrics.value = response.result?.lyrics
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _lyricsLoading.value = false
            }
        }
    }

    // Database Playlist Operations
    fun createPlaylist(name: String) {
        viewModelScope.launch {
            val id = "pl_${System.currentTimeMillis()}"
            repository.createPlaylist(id, name)
            _toastFlow.emit("Playlist '$name' berhasil dibuat")
        }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
            _toastFlow.emit("Playlist telah dihapus")
        }
    }

    fun addSongToPlaylist(playlistId: String, song: SongItem) {
        viewModelScope.launch {
            repository.addSongToPlaylist(
                playlistId = playlistId,
                videoId = song.videoId,
                title = song.title,
                artist = song.artist,
                artistId = song.artistId,
                thumbnail = song.thumbnail,
                url = song.url
            )
            _toastFlow.emit("Ditambahkan ke playlist")
        }
    }

    fun removeSongFromPlaylist(playlistId: String, videoId: String) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, videoId)
            _toastFlow.emit("Lagu dihapus dari playlist")
        }
    }

    fun toggleDownload(song: SongItem) {
        viewModelScope.launch {
            val isDownloaded = downloads.value.any { it.videoId == song.videoId }
            if (isDownloaded) {
                repository.removeSongFromDownloads(song.videoId)
                _toastFlow.emit("Dihapus dari Download")
            } else {
                repository.addSongToDownloads(
                    videoId = song.videoId,
                    title = song.title,
                    artist = song.artist,
                    artistId = song.artistId,
                    thumbnail = song.thumbnail,
                    url = song.url
                )
                _toastFlow.emit("Disimpan untuk Offline")
            }
        }
    }

    // Playback Core Operations
    fun playTrack(track: SongItem, queue: List<SongItem>) {
        _currentTrack.value = track
        _playQueue.value = queue
        _queueIndex.value = queue.indexOfFirst { it.videoId == track.videoId }.coerceAtLeast(0)
        _isPlaying.value = true
        _playbackPosition.value = 0f
        _playbackDuration.value = 0f
        
        if (youtubePlayer != null) {
            youtubePlayer?.loadVideo(track.videoId, 0f)
        } else {
            pendingVideoIdToPlay = track.videoId
        }
        fetchLyrics(track.videoId)
        
        viewModelScope.launch {
            repository.addSongToHistory(
                videoId = track.videoId,
                title = track.title,
                artist = track.artist,
                artistId = track.artistId,
                thumbnail = track.thumbnail,
                url = track.url
            )
        }
    }

    fun togglePlay() {
        val track = _currentTrack.value ?: return
        val playing = !_isPlaying.value
        _isPlaying.value = playing
        if (playing) {
            youtubePlayer?.play()
        } else {
            youtubePlayer?.pause()
        }
    }

    fun nextTrack() {
        val queue = _playQueue.value
        if (queue.isEmpty()) return

        val currentMode = _repeatMode.value
        val currentIndex = _queueIndex.value

        val nextIndex = when (currentMode) {
            RepeatMode.ONE -> currentIndex // Manual next on RepeatOne just restarts the same song
            RepeatMode.ALL -> (currentIndex + 1) % queue.size
            RepeatMode.OFF -> {
                if (currentIndex + 1 < queue.size) currentIndex + 1 else -1
            }
        }

        if (nextIndex != -1) {
            val nextTrack = queue[nextIndex]
            _queueIndex.value = nextIndex
            _currentTrack.value = nextTrack
            _isPlaying.value = true
            _playbackPosition.value = 0f
            _playbackDuration.value = 0f
            
            if (youtubePlayer != null) {
                youtubePlayer?.loadVideo(nextTrack.videoId, 0f)
            } else {
                pendingVideoIdToPlay = nextTrack.videoId
            }
            fetchLyrics(nextTrack.videoId)
        } else {
            _isPlaying.value = false
            youtubePlayer?.pause()
            viewModelScope.launch {
                _toastFlow.emit("Selesai memutar antrean")
            }
        }
    }

    fun prevTrack() {
        val queue = _playQueue.value
        if (queue.isEmpty()) return

        if (_playbackPosition.value > 3f) {
            // Seek to start if playing for more than 3 seconds
            youtubePlayer?.seekTo(0f)
            _playbackPosition.value = 0f
            return
        }

        val currentIndex = _queueIndex.value
        var prevIndex = currentIndex - 1
        if (prevIndex < 0) {
            prevIndex = queue.size - 1
        }

        val prevTrack = queue[prevIndex]
        _queueIndex.value = prevIndex
        _currentTrack.value = prevTrack
        _isPlaying.value = true
        _playbackPosition.value = 0f
        _playbackDuration.value = 0f
        
        if (youtubePlayer != null) {
            youtubePlayer?.loadVideo(prevTrack.videoId, 0f)
        } else {
            pendingVideoIdToPlay = prevTrack.videoId
        }
        fetchLyrics(prevTrack.videoId)
    }

    fun seekTo(seconds: Float) {
        youtubePlayer?.seekTo(seconds)
        _playbackPosition.value = seconds
    }

    fun toggleRepeatMode() {
        val nextMode = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _repeatMode.value = nextMode
        viewModelScope.launch {
            _toastFlow.emit(
                when (nextMode) {
                    RepeatMode.OFF -> "🔁 Pengulangan: MATI"
                    RepeatMode.ALL -> "🔁 Pengulangan: SEMUA"
                    RepeatMode.ONE -> "🔁 Pengulangan: SATU"
                }
            )
        }
    }

    fun toggleAutoNext() {
        val nextVal = !_isAutoNext.value
        _isAutoNext.value = nextVal
        viewModelScope.launch {
            _toastFlow.emit(if (nextVal) "✅ Putar Berikutnya: ON" else "⏸️ Putar Berikutnya: OFF")
        }
    }

    // Called from YouTube player listeners to update state in real-time
    fun updatePlaybackPosition(position: Float) {
        _playbackPosition.value = position
        updateLyricsIndex(position)
    }

    fun updatePlaybackDuration(duration: Float) {
        _playbackDuration.value = duration
    }

    fun updatePlaybackState(playing: Boolean, buffering: Boolean) {
        _isPlaying.value = playing
        _isBuffering.value = buffering
    }

    fun handleVideoEnded() {
        if (_repeatMode.value == RepeatMode.ONE) {
            youtubePlayer?.seekTo(0f)
            youtubePlayer?.play()
            _playbackPosition.value = 0f
        } else if (_isAutoNext.value) {
            nextTrack()
        } else {
            _isPlaying.value = false
        }
    }

    private fun updateLyricsIndex(currentTime: Float) {
        val lines = _currentLyrics.value?.lines ?: return
        var activeIndex = -1
        for (i in lines.indices) {
            if (currentTime >= lines[i].time) {
                activeIndex = i
            }
        }
        if (activeIndex != _activeLyricIndex.value) {
            _activeLyricIndex.value = activeIndex
        }
    }
}
