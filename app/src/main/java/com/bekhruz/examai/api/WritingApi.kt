package com.bekhruz.examai.api

import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface WritingApi {
        @Headers(
            "content-type: application/json",
            "X-RapidAPI-Key: 55ca6b1ddemshaaa5c05f0c6131ep1fde7bjsnfb685aab17a6",
            "X-RapidAPI-Host: simple-chatgpt-api.p.rapidapi.com"
        )
        @POST("ask")
        suspend fun askQuestion(@Body requestBody: RequestBody): WritingResultResponse
}