package com.example.ui

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.model.SongItem
import com.example.ui.theme.*
import com.example.viewmodel.RepeatMode
import com.example.viewmodel.XmusicViewModel
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.launch

@Composable
fun YouTubePlayerBridge(viewModel: XmusicViewModel) {
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    AndroidView(
        factory = { context ->
            YouTubePlayerView(context).apply {
                lifecycleOwner.lifecycle.addObserver(this)
                enableAutomaticInitialization = false
                initialize(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        viewModel.setYouTubePlayer(youTubePlayer)
                    }

                    override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                        viewModel.updatePlaybackPosition(second)
                    }

                    override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
                        viewModel.updatePlaybackDuration(duration)
                    }

                    override fun onStateChange(youTubePlayer: YouTubePlayer, state: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState) {
                        val isPlaying = state == com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState.PLAYING
                        val isBuffering = state == com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState.BUFFERING
                        viewModel.updatePlaybackState(playing = isPlaying, buffering = isBuffering)

                        if (state == com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState.ENDED) {
                            viewModel.handleVideoEnded()
                        }
                    }
                })
            }
        },
        modifier = Modifier.offset(x = (-10000).dp, y = (-10000).dp).size(1.dp)
    )
}

@Composable
fun ExpandedPlayer(viewModel: XmusicViewModel) {
    val track by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isBuffering by viewModel.isBuffering.collectAsState()
    val position by viewModel.playbackPosition.collectAsState()
    val duration by viewModel.playbackDuration.collectAsState()
    val isLyricsOpen by viewModel.isLyricsOpen.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val isAutoNext by viewModel.isAutoNext.collectAsState()
    val downloads by viewModel.downloads.collectAsState()

    var showPlaylistPicker by remember { mutableStateOf(false) }

    if (track == null) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        // Blurred Art background for atmosphere
        AsyncImage(
            model = track!!.thumbnail,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .blur(80.dp),
            contentScale = ContentScale.Crop,
            alpha = 0.2f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.setPlayerExpanded(false) }) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Minimize",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PLAYING FROM",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = track!!.artist,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .clickable {
                                track!!.artistId?.let { id ->
                                    viewModel.openArtist(id, track!!.artist)
                                }
                            }
                            .testTag("full-header-artist")
                    )
                }
                IconButton(onClick = { showPlaylistPicker = true }) {
                    Icon(
                        imageVector = Icons.Filled.QueueMusic,
                        contentDescription = "Add to playlist",
                        tint = Color.White
                    )
                }
            }

            // Body (Middle artwork or lyrics)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = isLyricsOpen,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                    },
                    label = "artwork_or_lyrics"
                ) { targetState ->
                    if (targetState) {
                        LyricsPanel(viewModel = viewModel)
                    } else {
                        // Artwork
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(24.dp))
                                .background(GlassColor)
                                .border(0.5.dp, GlassBorderColor, RoundedCornerShape(24.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = track!!.thumbnail,
                                contentDescription = track!!.title,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("full-cover"),
                                contentScale = ContentScale.Crop,
                                error = rememberVectorPainter(Icons.Filled.MusicNote)
                            )
                        }
                    }
                }
            }

            // Footer controls
            Column(modifier = Modifier.fillMaxWidth()) {
                // Song Metadata
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track!!.title,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("full-title")
                        )
                        Text(
                            text = track!!.artist,
                            fontSize = 15.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .clickable {
                                    track!!.artistId?.let { id ->
                                        viewModel.openArtist(id, track!!.artist)
                                    }
                                }
                                .testTag("full-artist")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Seekbar slider
                Column(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = position,
                        onValueChange = { viewModel.seekTo(it) },
                        valueRange = 0f..duration.coerceAtLeast(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White,
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("seek-bar")
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTime(position),
                            fontSize = 11.sp,
                            color = TextSecondary,
                            modifier = Modifier.testTag("time-curr")
                        )
                        Text(
                            text = formatTime(duration),
                            fontSize = 11.sp,
                            color = TextSecondary,
                            modifier = Modifier.testTag("time-dur")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Core Controls row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.toggleRepeatMode() },
                        modifier = Modifier.testTag("btn-repeat")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Repeat,
                            contentDescription = "Repeat Mode",
                            tint = if (repeatMode != RepeatMode.OFF) Color.White else TextMuted
                        )
                    }

                    IconButton(onClick = { viewModel.prevTrack() }) {
                        Icon(
                            imageVector = Icons.Filled.SkipPrevious,
                            contentDescription = "Previous",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { viewModel.togglePlay() }
                            .testTag("full-play-btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isBuffering) {
                            CircularProgressIndicator(
                                color = Color.Black,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Icon(
                                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = Color.Black,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    IconButton(onClick = { viewModel.nextTrack() }) {
                        Icon(
                            imageVector = Icons.Filled.SkipNext,
                            contentDescription = "Next",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Share button
                    val context = LocalContext.current
                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Dengarkan lagu di Xmusic")
                                putExtra(Intent.EXTRA_TEXT, "Dengarkan '${track!!.title}' oleh ${track!!.artist} di Xmusic: https://xymusic.vercel.app/?play=${track!!.videoId}&share=1")
                            }
                            context.startActivity(Intent.createChooser(intent, "Bagikan Lagu"))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Options Row (Lyrics toggle, Download, autoNext toggle)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Lyrics toggle
                        Button(
                            onClick = { viewModel.toggleLyrics() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isLyricsOpen) Color.White else GlassColor,
                                contentColor = if (isLyricsOpen) Color.Black else Color.White
                            ),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Lirik", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // Download toggle
                        val isDownloaded = downloads.any { it.videoId == track!!.videoId }
                        IconButton(
                            onClick = { viewModel.toggleDownload(track!!) },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isDownloaded) Color.White else GlassColor)
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download",
                                tint = if (isDownloaded) Color.Black else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // AutoNext toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { viewModel.toggleAutoNext() }
                    ) {
                        Text(
                            text = "Putar Berikutnya",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isAutoNext) Color.White else TextMuted
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = isAutoNext,
                            onCheckedChange = { viewModel.toggleAutoNext() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = Color.White,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = GlassColor
                            ),
                            modifier = Modifier.scale(0.8f)
                        )
                    }
                }
            }
        }
    }

    if (showPlaylistPicker) {
        PlaylistPickerOverlay(
            viewModel = viewModel,
            onDismiss = { showPlaylistPicker = false },
            onSelectPlaylist = { playlistId ->
                viewModel.addSongToPlaylist(playlistId, track!!)
                showPlaylistPicker = false
            }
        )
    }
}

@Composable
fun LyricsPanel(viewModel: XmusicViewModel) {
    val lyricsData by viewModel.currentLyrics.collectAsState()
    val loading by viewModel.lyricsLoading.collectAsState()
    val activeIndex by viewModel.activeLyricIndex.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Scroll active lyric line to center
    LaunchedEffect(activeIndex) {
        if (activeIndex != -1 && lyricsData?.lines?.isNotEmpty() == true) {
            coroutineScope.launch {
                listState.animateScrollToItem(activeIndex)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black.copy(alpha = 0.4f))
            .border(0.5.dp, GlassBorderColor, RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
        } else if (lyricsData == null || lyricsData!!.lines.isNullOrEmpty()) {
            Text(
                text = "Lirik tidak tersedia untuk lagu ini",
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 140.dp, horizontal = 16.dp)
            ) {
                itemsIndexed(lyricsData!!.lines!!) { index, line ->
                    val isActive = index == activeIndex
                    val alphaVal by animateFloatAsState(if (isActive) 1f else 0.4f, label = "alpha")
                    val sizeVal by animateFloatAsState(if (isActive) 18f else 15f, label = "size")

                    Text(
                        text = line.text,
                        fontSize = sizeVal.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                        color = if (isActive) Color.White else TextSecondary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(alphaVal)
                            .clickable { viewModel.seekTo(line.time) },
                        textAlign = TextAlign.Left
                    )
                }
            }
        }
    }
}

@Composable
fun PlaylistPickerOverlay(
    viewModel: XmusicViewModel,
    onDismiss: () -> Unit,
    onSelectPlaylist: (String) -> Unit
) {
    val playlists by viewModel.playlistsWithSongs.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Stop click propagation inside the dialog sheet
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(GlassStrongColor)
                .border(1.dp, GlassBorderColor, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .clickable(enabled = false) {}
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 16.dp)
                        .size(width = 40.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                )

                Text(
                    text = "Tambah ke Playlist",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (playlists.isEmpty()) {
                    Text(
                        text = "Belum ada playlist! Buat di Library dulu",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp)
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        playlists.forEach { p ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GlassColor)
                                    .clickable { onSelectPlaylist(p.playlist.id) }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.White.copy(alpha = 0.05f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val cover = p.songs.firstOrNull()?.thumbnail
                                    if (cover != null) {
                                        AsyncImage(
                                            model = cover,
                                            contentDescription = p.playlist.name,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop,
                                            error = rememberVectorPainter(Icons.Filled.MusicNote)
                                        )
                                    } else {
                                        Icon(Icons.Filled.MusicNote, contentDescription = null, tint = TextSecondary)
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = p.playlist.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${p.songs.size} lagu",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GlassColor, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Batal", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Add a helper wrapper for Compose scaling
fun Modifier.scale(scale: Float): Modifier = graphicsLayer {
    scaleX = scale
    scaleY = scale
}
