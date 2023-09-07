package com.bekhruz.examai.api

import com.google.gson.annotations.SerializedName

data class SpeechResponse(
    @SerializedName("recordId")
    val recordId: String,
    @SerializedName("eof")
    val eof: Int,
    @SerializedName("dtLastResponse")
    val dtLastResponse: String,
    @SerializedName("applicationId")
    val applicationId: String,
    @SerializedName("refText")
    val refText: String,
    @SerializedName("tokenId")
    val tokenId: String,
    @SerializedName("params")
    val params: Params,
    @SerializedName("result")
    val result: Result?
) {
    fun mapTo() = Speech(
        speed = result?.speed?:0,
        grammar = result?.grammar?:0,
        pronunciation = result?.pronunciation?:0,
        vocabulary = result?.vocabulary?:0,
        topicDevelopment = result?.topicDevelopment?:0,
        relevance = result?.relevance?:0,
        fluency = result?.fluency?:0,
        overAll = result?.overall?:0
    )
}

data class Speech(
    //speed, grammar, pronunciation, vocabulary, topicDevelopment, relevance, fluency
    val speed: Int = 0,
    val grammar: Int = 0,
    val pronunciation: Int = 0,
    val vocabulary: Int = 0,
    val topicDevelopment: Int = 0,
    val relevance: Int = 0,
    val fluency: Int = 0,
    val overAll: Int = 0
)

data class Params(
    @SerializedName("audio")
    val audio: Audio,
    @SerializedName("request")
    val request: Request,
    @SerializedName("app")
    val app: App
)

data class Audio(
    @SerializedName("channel")
    val channel: Int,
    @SerializedName("sampleBytes")
    val sampleBytes: Int,
    @SerializedName("sampleRate")
    val sampleRate: String,
    @SerializedName("audioType")
    val audioType: String
)

data class Request(
    @SerializedName("refText")
    val refText: String,
    @SerializedName("coreType")
    val coreType: String,
    @SerializedName("tokenId")
    val tokenId: String
)

data class App(
    @SerializedName("userId")
    val userId: String,
    @SerializedName("sig")
    val sig: String,
    @SerializedName("timestamp")
    val timestamp: String,
    @SerializedName("applicationId")
    val applicationId: String
)

data class Result(
    @SerializedName("speed")
    val speed: Int?,
    @SerializedName("kernel_version")
    val kernelVersion: String,
    @SerializedName("resource_version")
    val resourceVersion: String,
    @SerializedName("overall")
    val overall: Int,
    @SerializedName("sentences")
    val sentences: List<Sentence>,
    @SerializedName("grammar")
    val grammar: Int?,
    @SerializedName("transcription")
    val transcription: String,
    @SerializedName("pronunciation")
    val pronunciation: Int?,
    @SerializedName("pause_filler")
    val pauseFiller: Map<String, Any>, // You can specify the correct type here
    @SerializedName("vocabulary")
    val vocabulary: Int?,
    @SerializedName("topic_development")
    val topicDevelopment: Int?,
    @SerializedName("relevance")
    val relevance: Int?,
    @SerializedName("effective_speech_length")
    val effectiveSpeechLength: Double,
    @SerializedName("fluency")
    val fluency: Int?
)

data class Sentence(
    @SerializedName("end")
    val end: Int,
    @SerializedName("sentence")
    val sentence: String,
    @SerializedName("grammar")
    val grammar: Map<String, Any>, // You can specify the correct type here
    @SerializedName("details")
    val details: List<Detail>,
    @SerializedName("start")
    val start: Int
)

data class Detail(
    @SerializedName("pronunciation")
    val pronunciation: Int,
    @SerializedName("pause")
    val pause: Pause,
    @SerializedName("end")
    val end: Int,
    @SerializedName("word")
    val word: String,
    @SerializedName("level")
    val level: String,
    @SerializedName("start")
    val start: Int
)

data class Pause(
    @SerializedName("type")
    val type: Int,
    @SerializedName("duration")
    val duration: Int
)
