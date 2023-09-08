package com.bekhruz.examai

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bekhruz.examai.api.WritingApi
import com.bekhruz.examai.api.WritingResultResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import javax.inject.Inject


@HiltViewModel
class WritingViewModel @Inject constructor(
    private val writingApi: WritingApi
) : ViewModel() {

    val responseLiveData = MutableLiveData<WritingResultResponse>()
    val isLoading = MutableLiveData<Boolean>()

    fun askQuestion(question: String) = viewModelScope.launch {
        isLoading.postValue(true)
        val mediaType = "application/json".toMediaTypeOrNull()
        val requestBody = RequestBody.create(mediaType, "{\"question\": \"$question\"}")

        try {
            val response = withContext(Dispatchers.IO) {
                writingApi.askQuestion(requestBody)
            }
            responseLiveData.postValue(response)
        } catch (e: Exception) {
            // Handle error
        }
    }

}