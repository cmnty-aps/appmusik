package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class SongItem(
    val videoId: String,
    val title: String,
    val artist: String,
    val artistId: String? = null,
    val thumbnail: String? = null,
    val url: String? = null
)

@Serializable
data class SearchResponse(
    val status: Boolean,
    val result: SearchResult? = null
)

@Serializable
data class SearchResult(
    val songs: List<SongItem>? = null
)

@Serializable
data class LyricsResponse(
    val status: Boolean,
    val result: LyricsResult? = null
)

@Serializable
data class LyricsResult(
    val lyrics: LyricsData? = null
)

@Serializable
data class LyricsData(
    val lines: List<LyricLine>? = null
)

@Serializable
data class LyricLine(
    val time: Float,
    val text: String
)

@Serializable
data class ArtistResponse(
    val status: Boolean,
    val result: ArtistResult? = null
)

@Serializable
data class ArtistResult(
    val name: String,
    val thumbnails: List<ThumbnailItem>? = null,
    val topSongs: List<ArtistSongItem>? = null,
    val topAlbums: List<ArtistAlbumItem>? = null,
    val topSingles: List<ArtistAlbumItem>? = null,
    val topVideos: List<ArtistVideoItem>? = null,
    val featuredOn: List<ArtistAlbumItem>? = null,
    val similarArtists: List<SimilarArtistItem>? = null
)

@Serializable
data class ThumbnailItem(
    val url: String
)

@Serializable
data class ArtistSongItem(
    val videoId: String,
    val title: String,
    val artist: String? = null,
    val thumbnails: List<ThumbnailItem>? = null
)

@Serializable
data class ArtistAlbumItem(
    val browseId: String,
    val name: String,
    val artist: String? = null,
    val thumbnails: List<ThumbnailItem>? = null
)

@Serializable
data class ArtistVideoItem(
    val videoId: String? = null,
    val name: String,
    val artist: String? = null,
    val thumbnails: List<ThumbnailItem>? = null
)

@Serializable
data class SimilarArtistItem(
    val browseId: String,
    val name: String,
    val thumbnails: List<ThumbnailItem>? = null
)
