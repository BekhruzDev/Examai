package com.bekhruz.examai

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bekhruz.examai.api.ApiClient
import com.bekhruz.examai.api.Speech
import com.bekhruz.examai.api.SpeechResponse
import com.bekhruz.examai.api.SpeechSuperApi
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.codec.binary.Hex
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.codec.digest.DigestUtils
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import retrofit2.Response
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject

@HiltViewModel
class ExamaiViewModel @Inject constructor(
    private val speechSuperApi: SpeechSuperApi
) : ViewModel() {
    val map = mutableMapOf<Int, Speech>()
    val speechResult = MutableLiveData<Speech>()
    val isLoading = MutableLiveData<Boolean>()
    fun httpAPI(
        audioPath: String,
        audioType: String,
        audioSampleRate: String,
        refText: String,
        coreType: String,
        testType: String,
        partNumber: Int,
        questionPrompt: String,
        questionNo: Int
    ) = viewModelScope.launch {
        // Build the params using the modified buildParam() function
        isLoading.postValue(true)
        val appKey = "16919766530001bc"
        val secretKey = "0f00c91fd4b12fc76a19190541fd2d2e"
        val userId = getRandomString(5)

        val params = buildParam(
            appKey, secretKey, userId,
            audioType, audioSampleRate, refText, coreType,
            testType, partNumber, questionPrompt
        )

        // Create a RequestBody from the params
        val comment =
            RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), params)

        // Create a File and a RequestBody from the audioPath
        val audioFile = File(audioPath)
        val audio = MultipartBody.Part.createFormData(
            "audio", audioPath, audioFile.asRequestBody(audioType.toMediaTypeOrNull())
        )
        val result = speechSuperApi.httpAPI(coreType, 0, comment, audio)
        map[questionNo] = result.mapTo()
        if (questionNo == 3) {
            speechResult.postValue(map.values.toList().mapToOverAll())
        }
    }

    fun buildParam(
        appkey: String,
        secretKey: String,
        userId: String,
        audioType: String,
        audioSampleRate: String,
        refText: String,
        coreType: String,
        testType: String,
        partNumber: Int,
        questionPrompt: String
    ): String {
        val digest = DigestUtils.getSha1Digest()
        val timeReqMillis = System.currentTimeMillis()
        val connectSigStr = "$appkey$timeReqMillis$secretKey"
        val connectSig = Hex.encodeHexString(digest.digest(connectSigStr.toByteArray()))

        val timeStartMillis = System.currentTimeMillis()
        val startSigStr = "$appkey$timeStartMillis$userId$secretKey"
        val startSig = Hex.encodeHexString(digest.digest(startSigStr.toByteArray()))

        // Create properties JSONObject
        val properties = JSONObject().apply {
            put("type", "unscripted")
            put("pro", true)
            put("test_type", testType)
            put("task_type", "ielts_$partNumber")
            put("question_prompt", questionPrompt)
        }

        // Create requestParams JSONObject
        val requestParams = JSONObject().apply {
            put("coreType", coreType)
            put("properties", properties.toString())
        }

        // Create the main request JSONObject
        val params = JSONObject()
        params.put("connect", JSONObject().apply {
            put("cmd", "connect")
            put("param", JSONObject().apply {
                put("sdk", JSONObject().apply {
                    put("protocol", 2)
                    put("version", 16777472)
                    put("source", 9)
                })
                put("app", JSONObject().apply {
                    put("applicationId", appkey)
                    put("sig", connectSig)
                    put("timestamp", timeReqMillis.toString())
                })
            })
        })

        params.put("start", JSONObject().apply {
            put("cmd", "start")
            put("param", JSONObject().apply {
                put("app", JSONObject().apply {
                    put("applicationId", appkey)
                    put("timestamp", timeStartMillis.toString())
                    put("sig", startSig)
                    put("userId", userId)
                })
                put("audio", JSONObject().apply {
                    put("sampleBytes", 2)
                    put("channel", 1)
                    put("sampleRate", audioSampleRate)
                    put("audioType", audioType)
                })
                put("request", JSONObject().apply {
                    put("tokenId", "tokenId")
                    put("refText", refText)
                    put("coreType", coreType)
                })
            })
        })

        // Include the requestParams in the main JSON object
        params.put("requestParams", requestParams)

        return params.toString()
    }

    fun getRandomString(length: Int): String {
        val allowedChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }


    fun List<Speech>.mapToOverAll() = Speech(
        speed = this.sumOf { it.speed } / 3,
        grammar = this.sumOf { it.grammar } / 3,
        pronunciation = this.sumOf { it.pronunciation } / 3,
        vocabulary = this.sumOf { it.vocabulary } / 3,
        topicDevelopment = this.sumOf { it.topicDevelopment } / 3,
        relevance = this.sumOf { it.relevance } / 3,
        fluency = this.sumOf { it.fluency } / 3,
        overAll = this.sumOf { it.overAll } / 3
    )

}