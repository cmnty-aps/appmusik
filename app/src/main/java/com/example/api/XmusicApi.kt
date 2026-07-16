package com.example.api

import com.example.model.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface XmusicApi {
    @GET("api/search")
    suspend fun search(
        @Query("query") query: String
    ): SearchResponse

    @GET("api/suggest")
    suspend fun suggest(
        @Query("q") query: String
    ): List<String>

    @GET("api/artist")
    suspend fun getArtist(
        @Query("id") id: String
    ): ArtistResponse

    @GET("api/lyrics")
    suspend fun getLyrics(
        @Query("id") id: String
    ): LyricsResponse

    companion object {
        private const val BASE_URL = "https://xymusic.vercel.app/"

        fun create(): XmusicApi {
            val logger = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(XmusicApi::class.java)
        }
    }
}
