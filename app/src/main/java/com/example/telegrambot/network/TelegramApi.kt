package com.example.telegrambot.network

import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface TelegramApi {
    
    @GET("bot{token}/getUpdates")
    suspend fun getUpdates(
        @Path("token") token: String,
        @Query("offset") offset: Long? = null,
        @Query("limit") limit: Int = 100,
        @Query("timeout") timeout: Int = 30
    ): UpdatesResponse

    @POST("bot{token}/sendMessage")
    @FormUrlEncoded
    suspend fun sendMessage(
        @Path("token") token: String,
        @Field("chat_id") chatId: String,
        @Field("text") text: String,
        @Field("parse_mode") parseMode: String? = null
    ): Response

    @Multipart
    @POST("bot{token}/sendPhoto")
    suspend fun sendPhoto(
        @Path("token") token: String,
        @Part("chat_id") chatId: okhttp3.RequestBody,
        @Part photo: okhttp3.MultipartBody.Part,
        @Part("caption") caption: okhttp3.RequestBody? = null
    ): Response

    @Multipart
    @POST("bot{token}/sendDocument")
    suspend fun sendDocument(
        @Path("token") token: String,
        @Part("chat_id") chatId: okhttp3.RequestBody,
        @Part document: okhttp3.MultipartBody.Part
    ): Response

    @GET("bot{token}/getMe")
    suspend fun getMe(
        @Path("token") token: String
    ): BotInfoResponse

    @POST("bot{token}/setWebhook")
    @FormUrlEncoded
    suspend fun setWebhook(
        @Path("token") token: String,
        @Field("url") url: String,
        @Field("max_connections") maxConnections: Int = 100
    ): Response

    data class UpdatesResponse(
        val ok: Boolean,
        val result: List<Update>? = null,
        val description: String? = null
    )

    data class Update(
        @SerializedName("update_id")
        val updateId: Long,
        val message: Message? = null,
        val callback_query: CallbackQuery? = null,
        val inline_query: InlineQuery? = null
    )

    data class Message(
        val message_id: Int,
        val from: User? = null,
        val date: Long,
        val chat: Chat,
        val text: String? = null
    )

    data class User(
        val id: Long,
        val is_bot: Boolean,
        val first_name: String,
        val last_name: String? = null,
        val username: String? = null
    )

    data class Chat(
        val id: Long,
        val type: String,
        val title: String? = null,
        val username: String? = null,
        val first_name: String? = null,
        val last_name: String? = null
    )

    data class CallbackQuery(
        val id: String,
        val from: User,
        val message: Message? = null,
        val data: String? = null
    )

    data class InlineQuery(
        val id: String,
        val from: User,
        val query: String,
        val offset: String
    )

    data class Response(
        val ok: Boolean,
        val result: Message? = null,
        val description: String? = null,
        val error_code: Int? = null
    )

    data class BotInfoResponse(
        val ok: Boolean,
        val result: User? = null,
        val description: String? = null
    )

    companion object {
        private const val BASE_URL = "https://api.telegram.org/"
        
        private val gson = GsonBuilder().create()

        private val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        private val retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }

        fun create(): TelegramApi = retrofit.create(TelegramApi::class.java)

        suspend fun getMe(token: String): BotInfoResponse {
            return try {
                val api = create()
                api.getMe(token)
            } catch (e: Exception) {
                BotInfoResponse(false, null, null)
            }
        }

        suspend fun validateBot(token: String): Boolean {
            return try {
                val api = create()
                val response = api.getMe(token)
                response.ok && response.result != null
            } catch (e: Exception) {
                false
            }
        }

        suspend fun setWebhook(token: String, url: String): Boolean {
            return try {
                val api = create()
                val response = api.setWebhook(token, url)
                response.ok
            } catch (e: Exception) {
                false
            }
        }

        suspend fun getUpdates(
            token: String,
            offset: Long? = null,
            limit: Int = 100,
            timeout: Int = 30
        ): List<Update> {
            return try {
                val api = create()
                val response = api.getUpdates(token, offset, limit, timeout)
                if (response.ok) response.result ?: emptyList() else emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }

        suspend fun sendMessage(
            token: String,
            chatId: String,
            text: String,
            parseMode: String? = "HTML"
        ): Boolean {
            return try {
                val api = create()
                val response = api.sendMessage(token, chatId, text, parseMode)
                response.ok
            } catch (e: Exception) {
                false
            }
        }

        suspend fun sendPhoto(
            token: String,
            chatId: String,
            file: java.io.File,
            caption: String? = null
        ): Boolean {
            return try {
                val mediaType = "image/*".toMediaTypeOrNull()
                val requestFile = okhttp3.RequestBody.create(mediaType, file)
                val body = okhttp3.MultipartBody.Part.createFormData("photo", file.name, requestFile)
                val textMediaType = "text/plain".toMediaTypeOrNull()
                val chatPart = okhttp3.RequestBody.create(textMediaType, chatId)
                val captionPart = caption?.let { okhttp3.RequestBody.create(textMediaType, it) }
                
                val response = create().sendPhoto(token, chatPart, body, captionPart)
                response.ok
            } catch (e: Exception) {
                false
            }
        }

        suspend fun sendDocument(
            token: String,
            chatId: String,
            file: java.io.File
        ): Boolean {
            return try {
                val mediaType = "application/octet-stream".toMediaTypeOrNull()
                val requestFile = okhttp3.RequestBody.create(mediaType, file)
                val body = okhttp3.MultipartBody.Part.createFormData("document", file.name, requestFile)
                val textMediaType = "text/plain".toMediaTypeOrNull()
                val chatPart = okhttp3.RequestBody.create(textMediaType, chatId)
                
                val response = create().sendDocument(token, chatPart, body)
                response.ok
            } catch (e: Exception) {
                false
            }
        }
    }
}
