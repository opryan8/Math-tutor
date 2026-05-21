package com.example.data

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Gemini API Classes ---

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null,
    val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val role: String? = null, // e.g. "user" or "model" or null
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    val temperature: Float? = null,
    val maxOutputTokens: Int? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent? = null
)

// --- Retrofit Service ---

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiRetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

// --- Helper Functions to construct queries ---

class GeminiRepository {
    private val systemPrompt = """
        You are a highly helpful, friendly, and motivational AI Math Tutor. Your goal is to help middle school and high school students master Algebra and math concepts.
        Follow these pedagogy guidelines:
        1. Keep explanations highly interactive, clear, and broken down into step-by-step numbered lists.
        2. Give examples and explain 'why' a rule works (e.g. why we flip inequality symbols, or why x^0 = 1).
        3. Never just give the final answer flat out unless requested. Encourage the student and ask questions to help them think!
        4. Use bold text and clean formatting to make algebraic variables and steps easy to read.
        5. Keep tone cheerful, friendly, and supportive, using words of encouragement.
    """.trimIndent()

    suspend fun getTutorResponse(
        prompt: String,
        history: List<ChatMessage>
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Please configure your GEMINI_API_KEY in the Secrets panel of AI Studio to experience interactive AI hints and tutoring! Let me act as your Offline tutor for now: Keep up the algebra practice!"
        }

        // Map database chat history to Gemini payload formats
        val contentList = mutableListOf<GeminiContent>()
        
        // Add historical context (up to last 10 messages)
        history.forEach { msg ->
            contentList.add(
                GeminiContent(
                    role = if (msg.isUser) "user" else "model",
                    parts = listOf(GeminiPart(text = msg.text))
                )
            )
        }

        // Add the current prompt
        contentList.add(
            GeminiContent(
                role = "user",
                parts = listOf(GeminiPart(text = prompt))
            )
        )

        val request = GeminiRequest(
            contents = contentList,
            generationConfig = GeminiGenerationConfig(
                temperature = 0.7f,
                maxOutputTokens = 1500
            ),
            systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(text = systemPrompt))
            )
        )

        return try {
            val response = GeminiRetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "I apologize, I didn't receive a clear tutor explanation. Please try asking again!"
        } catch (e: Exception) {
            "An error occurred while connecting to the AI Tutor server: ${e.localizedMessage ?: "Connection error"}. Please make sure you are connected to the Internet and have configured a valid GEMINI_API_KEY."
        }
    }
}
