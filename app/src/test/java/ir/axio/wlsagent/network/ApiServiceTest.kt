package ir.axio.wlsagent.network

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.HttpException

class ApiServiceTest {

    private lateinit var server: MockWebServer
    private lateinit var api: ApiService

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        api = Retrofit.Builder()
            .baseUrl(server.url("/wp-json/wls/v1/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun enqueue(body: String, code: Int = 200) {
        server.enqueue(MockResponse().setResponseCode(code).setBody(body))
    }

    @Test
    fun `listConversations defaults to the open status and parses the payload`() = runTest {
        enqueue(
            """
            [{"id":5,"guest_name":"Ali","status":"open",
              "last_message_at":"2026-01-01 10:00:00","assigned_agent_id":3}]
            """.trimIndent()
        )

        val conversations = api.listConversations()

        assertEquals(
            "/wp-json/wls/v1/agent/conversations?status=open",
            server.takeRequest().path
        )
        assertEquals(1, conversations.size)
        assertEquals(5L, conversations[0].id)
        assertEquals("Ali", conversations[0].guest_name)
        assertEquals("open", conversations[0].status)
        assertEquals(3L, conversations[0].assigned_agent_id)
    }

    @Test
    fun `listConversations passes an explicit status and tolerates null fields`() = runTest {
        enqueue(
            """
            [{"id":6,"guest_name":null,"status":"closed",
              "last_message_at":null,"assigned_agent_id":null}]
            """.trimIndent()
        )

        val conversations = api.listConversations(status = "closed")

        assertEquals(
            "/wp-json/wls/v1/agent/conversations?status=closed",
            server.takeRequest().path
        )
        assertNull(conversations[0].guest_name)
        assertNull(conversations[0].last_message_at)
        assertNull(conversations[0].assigned_agent_id)
    }

    @Test
    fun `claimConversation posts to the claim endpoint of the conversation`() = runTest {
        enqueue("""{"ok":true}""")

        val response = api.claimConversation(42L)

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/wp-json/wls/v1/agent/conversations/42/claim", request.path)
        assertTrue(response.ok)
    }

    @Test
    fun `closeConversation posts to the close endpoint of the conversation`() = runTest {
        enqueue("""{"ok":false}""")

        val response = api.closeConversation(42L)

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/wp-json/wls/v1/agent/conversations/42/close", request.path)
        assertEquals(false, response.ok)
    }

    @Test
    fun `getMessages defaults to fetching the whole history`() = runTest {
        enqueue(
            """
            {"conversation":{"id":5,"guest_name":"Ali","status":"open",
                             "last_message_at":null,"assigned_agent_id":null},
             "messages":[{"id":1,"conversation_id":5,"sender_type":"guest",
                          "body":"سلام","created_at":"2026-01-01 10:00:00"}]}
            """.trimIndent()
        )

        val response = api.getMessages(5L)

        assertEquals(
            "/wp-json/wls/v1/conversations/5/messages?after_id=0",
            server.takeRequest().path
        )
        assertEquals(5L, response.conversation.id)
        assertEquals(1, response.messages.size)
        assertEquals("سلام", response.messages[0].body)
        assertEquals("guest", response.messages[0].sender_type)
    }

    @Test
    fun `getMessages forwards the after_id cursor`() = runTest {
        enqueue(
            """
            {"conversation":{"id":5,"guest_name":null,"status":"open",
                             "last_message_at":null,"assigned_agent_id":null},
             "messages":[]}
            """.trimIndent()
        )

        val response = api.getMessages(5L, afterId = 17L)

        assertEquals(
            "/wp-json/wls/v1/conversations/5/messages?after_id=17",
            server.takeRequest().path
        )
        assertTrue(response.messages.isEmpty())
    }

    @Test
    fun `postMessage sends the body as json`() = runTest {
        enqueue("""{"ok":true}""")

        val response = api.postMessage(5L, SendMessageRequest("سلام"))

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/wp-json/wls/v1/conversations/5/messages", request.path)
        assertEquals("""{"body":"سلام"}""", request.body.readUtf8())
        assertTrue(response.ok)
    }

    @Test
    fun `registerDevice sends the fcm token`() = runTest {
        enqueue("""{"ok":true}""")

        api.registerDevice(RegisterDeviceRequest("token-123"))

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/wp-json/wls/v1/device/register", request.path)
        assertEquals("""{"fcm_token":"token-123"}""", request.body.readUtf8())
    }

    @Test
    fun `an unauthorized response is surfaced as an http exception`() = runTest {
        enqueue("""{"code":"rest_forbidden"}""", code = 401)

        val error = runCatching { api.listConversations() }.exceptionOrNull()

        assertEquals(401, (error as HttpException).code())
    }
}
