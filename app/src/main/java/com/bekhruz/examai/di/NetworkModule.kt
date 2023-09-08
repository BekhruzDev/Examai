package com.bekhruz.examai.di

import android.content.Context
import androidx.viewbinding.BuildConfig
import com.bekhruz.examai.api.SpeechSuperApi
import com.bekhruz.examai.api.WritingApi
import com.chuckerteam.chucker.api.ChuckerInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val TIME_OUT = 40L
    private const val BASE_URL = "https://api.speechsuper.com/"

    @Singleton
    @Provides
    fun provideOkhttpClient(@ApplicationContext context: Context): OkHttpClient {
        val clientBuilder = OkHttpClient.Builder()
            .connectTimeout(TIME_OUT * 3, TimeUnit.SECONDS)
            .readTimeout(TIME_OUT, TimeUnit.SECONDS)
            .writeTimeout(TIME_OUT, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor {
            }.apply { this.level = HttpLoggingInterceptor.Level.BODY })
            .addInterceptor(ChuckerInterceptor(context))
        //If debugged version, network request debugger added
        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            clientBuilder.addInterceptor(logging)
            clientBuilder.addInterceptor(ChuckerInterceptor(context))
        }
        return clientBuilder.build()
    }

    @Singleton
    @Provides
    fun provideSpeechApi(
        okHttpClient: OkHttpClient,
    ): SpeechSuperApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(SpeechSuperApi::class.java)

    @Singleton
    @Provides
    fun provideWritingApi(okHttpClient: OkHttpClient): WritingApi {
        return Retrofit.Builder()
            .baseUrl("https://simple-chatgpt-api.p.rapidapi.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WritingApi::class.java)
    }
}