package com.bekhruz.examai.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface SpeechSuperApi {
    @Multipart
    @POST("{coreType}")
    suspend fun httpAPI(
        @Path("coreType") coreType: String,
        @Header("Request-Index") requestIndex: Int,
        @Part("text") comment: RequestBody,
        @Part audio: MultipartBody.Part
    ): SpeechResponse
}