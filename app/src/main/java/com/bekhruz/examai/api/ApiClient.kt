package com.bekhruz.examai.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "https://api.speechsuper.com/"

    private val logging = HttpLoggingInterceptor().also { it.setLevel(HttpLoggingInterceptor.Level.BODY) }
    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()


    val retrofit: SpeechSuperApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(SpeechSuperApi::class.java)
}