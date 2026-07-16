package com.example.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import com.example.database.HistoryEntity
import com.example.database.DownloadEntity
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.database.PlaylistWithSongs
import com.example.model.SongItem
import com.example.ui.theme.*
import com.example.viewmodel.RepeatMode
import com.example.viewmodel.Screen
import com.example.viewmodel.XmusicViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.absoluteValue

@Composable
fun MainApp(viewModel: XmusicViewModel) {
    val context = LocalContext.current
    var splashFinished by remember { mutableStateOf(false) }

    // Collect Toasts
    LaunchedEffect(Unit) {
        viewModel.toastFlow.collectLatest { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        // Ambient background orbs
        AmbientBackground()

        // Invisible YouTube Player integration behind UI
        YouTubePlayerBridge(viewModel)

        if (!splashFinished) {
            SplashScreen(onFinished = { splashFinished = true })
        } else {
            // Main app content
            AppContent(viewModel)
        }
    }
}

@Composable
fun AmbientBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "breathe")
    val scale1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "orb1"
    )
    val scale2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "orb2"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Orb 1: Top Left
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x1AFFFFFF), Color.Transparent),
                center = Offset(0f, 0f),
                radius = 350.dp.toPx() * scale1
            ),
            radius = 350.dp.toPx() * scale1,
            center = Offset(0f, 0f)
        )

        // Orb 2: Bottom Right
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x2E969CA8), Color.Transparent),
                center = Offset(width, height - 100f),
                radius = 400.dp.toPx() * scale2
            ),
            radius = 400.dp.toPx() * scale2,
            center = Offset(width, height - 100f)
        )

        // Orb 3: Middle
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x0DFFFFFF), Color.Transparent),
                center = Offset(width * 0.5f, height * 0.45f),
                radius = 280.dp.toPx() * scale1
            ),
            radius = 280.dp.toPx() * scale1,
            center = Offset(width * 0.5f, height * 0.45f)
        )
    }
}

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "splash")

    // Vinyl rotation
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Waves scaling
    val waveScale1 by infiniteTransition.animateFloat(
        initialValue = 0.28f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "wave1"
    )
    val waveAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "waveAlpha1"
    )

    LaunchedEffect(Unit) {
        delay(1900)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF0D0D10), Color(0xFF050507)),
                    center = Offset.Unspecified,
                    radius = Float.POSITIVE_INFINITY
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                // Rippling waves
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .graphicsLayer {
                            scaleX = waveScale1
                            scaleY = waveScale1
                            alpha = waveAlpha1
                        }
                        .border(1.dp, Color(0x66DCE0E4), CircleShape)
                )

                // Vinyl disk background
                Box(
                    modifier = Modifier
                        .size(128.dp)
                        .graphicsLayer { rotationZ = rotation }
                        .drawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0x12FFFFFF), Color.Transparent),
                                    radius = 64.dp.toPx()
                                )
                            )
                        }
                        .border(1.dp, Color(0x1AFFFFFF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Lines detailing the vinyl record
                    for (i in 1..4) {
                        Box(
                            modifier = Modifier
                                .size((128 - i * 20).dp)
                                .border(0.5.dp, Color(0x0DFFFFFF), CircleShape)
                        )
                    }
                }

                // Center logo circle (Glassmorphic)
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .clip(CircleShape)
                        .background(Color(0x0FFFFFFF))
                        .border(1.dp, Color(0x29FFFFFF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.MusicNote,
                        contentDescription = "Logo",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(42.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            Text(
                text = "Xmusic",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "MENYIAPKAN MUSIKMU",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(22.dp))

            // Loading loading line bar
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(2.dp)
                    .background(Color(0x1AFFFFFF), RoundedCornerShape(1.dp))
            ) {
                val animationVal by infiniteTransition.animateFloat(
                    initialValue = -1f,
                    targetValue = 2.5f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1400, easing = LinearOutSlowInEasing),
                        repeatMode = androidx.compose.animation.core.RepeatMode.Restart
                    ),
                    label = "bar"
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(48.dp)
                        .graphicsLayer { translationX = animationVal * 48.dp.toPx() }
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color.Transparent, Color.White, Color.Transparent)
                            )
                        )
                )
            }
        }
    }
}

@Composable
fun AppContent(viewModel: XmusicViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isPlayerExpanded by viewModel.isPlayerExpanded.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()
    val artistId by viewModel.activeArtistId.collectAsState()

    Scaffold(
        bottomBar = {
            Column {
                // Sticky Mini Player
                if (currentTrack != null) {
                    MiniPlayer(viewModel = viewModel)
                }
                // Custom Bottom Navigation
                BottomNavBar(currentScreen = currentScreen, onNavigate = { viewModel.switchScreen(it) })
            }
        },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            when (currentScreen) {
                Screen.HOME -> HomeScreen(viewModel)
                Screen.SEARCH -> SearchScreen(viewModel)
                Screen.LIBRARY -> LibraryScreen(viewModel)
            }
        }
    }

    // Artist Detail view overlay
    AnimatedVisibility(
        visible = artistId != null,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        ArtistDetailOverlay(viewModel = viewModel)
    }

    // Full Screen Player overlay
    AnimatedVisibility(
        visible = isPlayerExpanded,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        ExpandedPlayer(viewModel = viewModel)
    }
}

@Composable
fun BottomNavBar(currentScreen: Screen, onNavigate: (Screen) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x8C060609))
            .border(width = 0.5.dp, color = Color(0x14FFFFFF), shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp))
            .navigationBarsPadding()
            .height(65.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavBarItem(
            iconSelected = Icons.Default.Home,
            iconUnselected = Icons.Default.Home,
            label = "Home",
            isSelected = currentScreen == Screen.HOME,
            onClick = { onNavigate(Screen.HOME) },
            modifier = Modifier.testTag("nav_home")
        )
        NavBarItem(
            iconSelected = Icons.Default.Search,
            iconUnselected = Icons.Default.Search,
            label = "Search",
            isSelected = currentScreen == Screen.SEARCH,
            onClick = { onNavigate(Screen.SEARCH) },
            modifier = Modifier.testTag("nav_search")
        )
        NavBarItem(
            iconSelected = Icons.Filled.QueueMusic,
            iconUnselected = Icons.Filled.QueueMusic,
            label = "Library",
            isSelected = currentScreen == Screen.LIBRARY,
            onClick = { onNavigate(Screen.LIBRARY) },
            modifier = Modifier.testTag("nav_library")
        )
    }
}

@Composable
fun NavBarItem(
    iconSelected: androidx.compose.ui.graphics.vector.ImageVector,
    iconUnselected: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val color = if (isSelected) Color.White else TextMuted
    val scale by animateFloatAsState(if (isSelected) 1.05f else 1f, label = "scale")

    Column(
        modifier = modifier
            .width(64.dp)
            .clickable(onClick = onClick)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isSelected) iconSelected else iconUnselected,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 9.5.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = color
        )
    }
}

// ------------------------------------ HOME SCREEN ------------------------------------
@Composable
fun HomeScreen(viewModel: XmusicViewModel) {
    val homeTracks by viewModel.homeTracks.collectAsState()
    val loading by viewModel.homeLoading.collectAsState()
    val activeGenre by viewModel.activeGenre.collectAsState()
    val genres = listOf("Pop", "Dangdut", "Rock", "Indie", "K-Pop", "Jazz")

    Column(modifier = Modifier.fillMaxSize()) {
        // Sticky Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0x66050507))
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Xmusic",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Rekomendasi buat kamu",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                IconButton(
                    onClick = { viewModel.fetchHomeTracks() },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(GlassColor)
                        .border(1.dp, GlassBorderColor, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            // Genres list
            LazyRow(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(genres) { genre ->
                    val isSelected = genre == activeGenre
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) Color.White else GlassColor)
                            .clickable { viewModel.fetchHomeTracks(genre) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = genre,
                            color = if (isSelected) Color.Black else Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        if (loading && homeTracks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.5.dp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
            ) {
                // Recommendation Grid (6 tracks)
                item {
                    val gridTracks = homeTracks.take(6)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.height(240.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        userScrollEnabled = false
                    ) {
                        itemsIndexed(gridTracks) { index, track ->
                            HomeGridItem(
                                track = track,
                                onClick = { viewModel.playTrack(track, homeTracks) }
                            )
                        }
                    }
                }

                // Dynamic playlist carousels (Horizontal scroll, tracks 6-12)
                item {
                    val scrollTracks = homeTracks.drop(6).take(6)
                    Column {
                        Text(
                            text = "Daftar Lagu Populer",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(end = 16.dp)
                        ) {
                            itemsIndexed(scrollTracks) { index, track ->
                                HomeScrollItem(
                                    track = track,
                                    onClick = { viewModel.playTrack(track, homeTracks) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeGridItem(track: SongItem, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(GlassColor)
            .border(1.dp, GlassBorderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AsyncImage(
                model = track.thumbnail,
                contentDescription = track.title,
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                error = rememberVectorPainter(Icons.Filled.MusicNote)
            )
            Text(
                text = track.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun HomeScrollItem(track: SongItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(GlassColor)
                .border(0.5.dp, GlassBorderColor, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = track.thumbnail,
                contentDescription = track.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                error = rememberVectorPainter(Icons.Filled.MusicNote)
            )
            // Tiny gloss play button overlay
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = track.title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = track.artist,
            fontSize = 10.5.sp,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ------------------------------------ SEARCH SCREEN ------------------------------------
@Composable
fun SearchScreen(viewModel: XmusicViewModel) {
    val query by viewModel.searchQuery.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val results by viewModel.searchResults.collectAsState()
    val loading by viewModel.searchLoading.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
    ) {
        Text(
            text = "Cari",
            fontSize = 30.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Search Input Bar
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_input"),
                placeholder = { Text("Cari lagu, artis, atau album...", color = TextMuted, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondary) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary)
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    viewModel.executeSearch(query)
                    keyboardController?.hide()
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFD2D5DA),
                    unfocusedBorderColor = GlassBorderColor,
                    focusedContainerColor = GlassColor,
                    unfocusedContainerColor = GlassColor,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Suggestions Dropdown
        if (suggestions.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xE00D0D10))
                    .border(1.dp, GlassBorderColor, RoundedCornerShape(12.dp))
                    .heightIn(max = 240.dp)
                    .verticalScroll(rememberScrollState())
                    .testTag("suggestions_dropdown")
            ) {
                Column {
                    suggestions.forEach { suggestion ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.executeSearch(suggestion)
                                    keyboardController?.hide()
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(end = 4.dp)
                            )
                            Text(
                                text = suggestion,
                                color = Color.White,
                                fontSize = 13.5.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Results list
        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.5.dp)
            }
        } else if (results.isEmpty() && query.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Tidak ada hasil", color = TextSecondary, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                itemsIndexed(results) { index, track ->
                    SearchResultItem(
                        track = track,
                        onClick = { viewModel.playTrack(track, results) }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(track: SongItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AsyncImage(
            model = track.thumbnail,
            contentDescription = track.title,
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(6.dp)),
            contentScale = ContentScale.Crop,
            error = rememberVectorPainter(Icons.Filled.MusicNote)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = track.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track.artist,
                fontSize = 11.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ------------------------------------ LIBRARY SCREEN ------------------------------------
@Composable
fun LibraryScreen(viewModel: XmusicViewModel) {
    val playlists by viewModel.playlistsWithSongs.collectAsState()
    val history by viewModel.history.collectAsState()
    val downloads by viewModel.downloads.collectAsState()
    var openCreateDialog by remember { mutableStateOf(false) }
    var selectedPlaylist by remember { mutableStateOf<PlaylistWithSongs?>(null) }
    var showHistory by remember { mutableStateOf(false) }
    var showDownloads by remember { mutableStateOf(false) }

    if (selectedPlaylist != null) {
        // Render playlist details view
        PlaylistDetailScreen(
            playlistWithSongs = selectedPlaylist!!,
            onBack = { selectedPlaylist = null },
            viewModel = viewModel
        )
    } else if (showHistory) {
        HistoryScreen(history = history, onBack = { showHistory = false }, viewModel = viewModel)
    } else if (showDownloads) {
        DownloadsScreen(downloads = downloads, onBack = { showDownloads = false }, viewModel = viewModel)
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
        ) {
            Text(
                text = "Library",
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Special Library Folders
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // History
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GlassColor)
                        .clickable { showHistory = true }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.History, contentDescription = "History", tint = Color.White, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Riwayat", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
                
                // Downloads
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GlassColor)
                        .clickable { showDownloads = true }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Download, contentDescription = "Offline", tint = Color.White, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Download", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Playlists",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Create Playlist Button
            Button(
                onClick = { openCreateDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("create_playlist_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Buat Playlist Baru", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (playlists.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.QueueMusic,
                        contentDescription = null,
                        tint = TextSecondary.copy(alpha = 0.3f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Belum ada playlist", color = TextSecondary, fontSize = 14.sp)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(playlists) { pl ->
                        PlaylistItem(
                            playlistWithSongs = pl,
                            onClick = { selectedPlaylist = pl }
                        )
                    }
                }
            }
        }
    }

    if (openCreateDialog) {
        CreatePlaylistDialog(
            onDismiss = { openCreateDialog = false },
            onCreate = { name ->
                viewModel.createPlaylist(name)
                openCreateDialog = false
            }
        )
    }
}

@Composable
fun HistoryScreen(history: List<HistoryEntity>, onBack: () -> Unit, viewModel: XmusicViewModel) {
    val songs = history.map { 
        SongItem(videoId = it.videoId, title = it.title, artist = it.artist, artistId = it.artistId, thumbnail = it.thumbnail, url = it.url) 
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("Riwayat Pemutaran", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
        }

        if (songs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Belum ada riwayat pemutaran.", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                itemsIndexed(songs) { index, track ->
                    SearchResultItem(
                        track = track,
                        onClick = { viewModel.playTrack(track, songs) }
                    )
                }
            }
        }
    }
}

@Composable
fun DownloadsScreen(downloads: List<DownloadEntity>, onBack: () -> Unit, viewModel: XmusicViewModel) {
    val songs = downloads.map { 
        SongItem(videoId = it.videoId, title = it.title, artist = it.artist, artistId = it.artistId, thumbnail = it.thumbnail, url = it.url) 
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("Download / Offline", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
        }

        if (songs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Belum ada lagu yang didownload.", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                itemsIndexed(songs) { index, track ->
                    SearchResultItem(
                        track = track,
                        onClick = { viewModel.playTrack(track, songs) }
                    )
                }
            }
        }
    }
}

@Composable
fun PlaylistItem(playlistWithSongs: PlaylistWithSongs, onClick: () -> Unit) {
    val playlist = playlistWithSongs.playlist
    val songs = playlistWithSongs.songs

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(GlassColor)
            .border(0.5.dp, GlassBorderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            val firstSongCover = songs.firstOrNull()?.thumbnail
            if (firstSongCover != null) {
                AsyncImage(
                    model = firstSongCover,
                    contentDescription = playlist.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = rememberVectorPainter(Icons.Filled.MusicNote)
                )
            } else {
                // Generated abstract placeholder
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF2E2E2E), Color(0xFF1E1E1E))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.MusicNote,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = playlist.name,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "${songs.size} lagu",
            fontSize = 11.sp,
            color = TextSecondary
        )
    }
}

@Composable
fun CreatePlaylistDialog(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(300.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(GlassStrongColor)
                .border(1.dp, GlassBorderColor, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Buat Playlist Baru", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Nama Playlist", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = GlassBorderColor,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("playlist_name_input")
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = GlassColor, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Batal")
                    }
                    Button(
                        onClick = { if (name.trim().isNotEmpty()) onCreate(name) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        enabled = name.trim().isNotEmpty()
                    ) {
                        Text("Buat")
                    }
                }
            }
        }
    }
}

@Composable
fun PlaylistDetailScreen(
    playlistWithSongs: PlaylistWithSongs,
    onBack: () -> Unit,
    viewModel: XmusicViewModel
) {
    val playlist = playlistWithSongs.playlist
    // Listen directly to dynamic updates of this specific playlist
    val livePlaylists by viewModel.playlistsWithSongs.collectAsState()
    val livePlaylist = livePlaylists.find { it.playlist.id == playlist.id } ?: playlistWithSongs

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(GlassColor)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = livePlaylist.playlist.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${livePlaylist.songs.size} lagu",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
            IconButton(
                onClick = {
                    viewModel.deletePlaylist(livePlaylist.playlist.id)
                    onBack()
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.Red.copy(alpha = 0.15f))
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Hapus Playlist", tint = Color.Red)
            }
        }

        if (livePlaylist.songs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("Playlist ini belum ada lagu", color = TextSecondary, fontSize = 14.sp)
            }
        } else {
            val songsList = livePlaylist.songs.map { entity ->
                SongItem(
                    videoId = entity.videoId,
                    title = entity.title,
                    artist = entity.artist,
                    artistId = entity.artistId,
                    thumbnail = entity.thumbnail,
                    url = entity.url
                )
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp)
            ) {
                itemsIndexed(livePlaylist.songs) { index, entity ->
                    val song = SongItem(
                        videoId = entity.videoId,
                        title = entity.title,
                        artist = entity.artist,
                        artistId = entity.artistId,
                        thumbnail = entity.thumbnail,
                        url = entity.url
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.playTrack(song, songsList) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AsyncImage(
                            model = song.thumbnail,
                            contentDescription = song.title,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            contentScale = ContentScale.Crop,
                            error = rememberVectorPainter(Icons.Filled.MusicNote)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.title,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = song.artist,
                                fontSize = 10.5.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(
                            onClick = { viewModel.removeSongFromPlaylist(livePlaylist.playlist.id, song.videoId) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Hapus",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------ INFO / DEV SCREEN ------------------------------------
@Composable
fun InfoScreen(viewModel: XmusicViewModel) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero Avatar
        Box(
            modifier = Modifier
                .padding(top = 16.dp, bottom = 24.dp)
                .size(96.dp)
                .clip(CircleShape)
                .background(GlassColor)
                .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.MusicNote,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(48.dp)
            )
        }

        Text(
            text = "Xmusic",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
        Text(
            text = "Streaming Musik YouTube dengan Lirik",
            fontSize = 13.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        // Specs block
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = GlassColor),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(0.5.dp, GlassBorderColor)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("📱 Aplikasi", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary, letterSpacing = 1.sp)
                SpecRow("Nama", "Xmusic")
                SpecRow("Versi", "v2.0.0")
                SpecRow("Dirilis", "Januari 2025")
                SpecRow("Framework", "Jetpack Compose + Kotlin")
                SpecRow("Pemberi Daya", "Google AI Studio")
            }
        }

        // Developer block
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(containerColor = GlassColor),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(0.5.dp, GlassBorderColor)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("👤 Developer", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary, letterSpacing = 1.sp)
                SpecRow("Nama", "XTan")
                SpecRow("Instagram", "@abctanuu")
            }
        }

        // WhatsApp Channel Action
        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://whatsapp.com/channel/0029VbD8Muz9WtBuZR9UMq0x"))
                context.startActivity(intent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("wa_channel_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
            shape = RoundedCornerShape(26.dp)
        ) {
            Icon(Icons.Default.List, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Gabung Channel WhatsApp", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextMuted, fontSize = 13.sp)
        Text(value, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 13.sp)
    }
}

// ------------------------------------ MINI PLAYER ------------------------------------
@Composable
fun MiniPlayer(viewModel: XmusicViewModel) {
    val track by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isBuffering by viewModel.isBuffering.collectAsState()
    val position by viewModel.playbackPosition.collectAsState()
    val duration by viewModel.playbackDuration.collectAsState()

    val progress = if (duration > 0f) position / duration else 0f

    if (track != null) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xE608080B))
                .border(0.5.dp, GlassBorderColor, RoundedCornerShape(0.dp))
                .clickable { viewModel.setPlayerExpanded(true) }
                .padding(top = 8.dp, bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AsyncImage(
                    model = track!!.thumbnail,
                    contentDescription = track!!.title,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    contentScale = ContentScale.Crop,
                    error = rememberVectorPainter(Icons.Filled.MusicNote)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track!!.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = track!!.artist,
                        fontSize = 10.5.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Controls
                IconButton(
                    onClick = { viewModel.togglePlay() },
                    modifier = Modifier.testTag("mini-play-btn")
                ) {
                    if (isBuffering) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                    } else {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Inline Slim Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.5.dp)
                    .background(Color(0x1AFFFFFF))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .background(Color.White)
                        .testTag("mini-progress")
                )
            }
        }
    }
}

// Helper to format time
fun formatTime(seconds: Float): String {
    if (seconds.isNaN() || seconds.isInfinite()) return "0:00"
    val m = (seconds / 60).toInt()
    val s = (seconds % 60).toInt()
    return "$m:${if (s < 10) "0" else ""}$s"
}
