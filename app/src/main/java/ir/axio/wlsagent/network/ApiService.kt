package ir.axio.wlsagent.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

data class ConversationDto(
    val id: Long,
    val guest_name: String?,
    val status: String,
    val last_message_at: String?,
    val assigned_agent_id: Long?
)

data class MessageDto(
    val id: Long,
    val conversation_id: Long,
    val sender_type: String,
    val body: String,
    val created_at: String
)

data class MessagesResponse(
    val conversation: ConversationDto,
    val messages: List<MessageDto>
)

data class SendMessageRequest(val body: String)
data class OkResponse(val ok: Boolean)
data class RegisterDeviceRequest(val fcm_token: String)

interface ApiService {

    @GET("agent/conversations")
    suspend fun listConversations(@Query("status") status: String = "open"): List<ConversationDto>

    @POST("agent/conversations/{id}/claim")
    suspend fun claimConversation(@Path("id") id: Long): OkResponse

    @POST("agent/conversations/{id}/close")
    suspend fun closeConversation(@Path("id") id: Long): OkResponse

    @GET("conversations/{id}/messages")
    suspend fun getMessages(@Path("id") id: Long, @Query("after_id") afterId: Long = 0): MessagesResponse

    @POST("conversations/{id}/messages")
    suspend fun postMessage(@Path("id") id: Long, @Body body: SendMessageRequest): OkResponse

    @POST("device/register")
    suspend fun registerDevice(@Body body: RegisterDeviceRequest): OkResponse
}
