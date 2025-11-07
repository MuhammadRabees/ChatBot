package com.example.chatbot.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit // <-- YEH IMPORT ADD KAREIN

object RetrofitInstance {
    private const val BASE_URL = "https://openrouter.ai/api/v1/"

    fun create(apiKey: String): OpenAiApiService {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

        val authInterceptor = Interceptor { chain ->
            val newReq = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("HTTP-Referer", "http://com.example.chatbot")
                .addHeader("X-Title", "Chatbot")
                .build()
            chain.proceed(newReq)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(60, TimeUnit.SECONDS) // For Connecting to API 1 min
            .readTimeout(120, TimeUnit.SECONDS)   // To get respnse from AI 2 min
            .writeTimeout(60, TimeUnit.SECONDS)  // For Image upload 1 min
            // === CHANGES END ===
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(OpenAiApiService::class.java)
    }
}