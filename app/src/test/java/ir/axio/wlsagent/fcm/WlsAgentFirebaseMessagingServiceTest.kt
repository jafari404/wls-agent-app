package ir.axio.wlsagent.fcm

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.messaging.RemoteMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class WlsAgentFirebaseMessagingServiceTest {

    private val notificationManager: NotificationManager
        get() = ApplicationProvider.getApplicationContext<Context>()
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private fun service() =
        Robolectric.buildService(WlsAgentFirebaseMessagingService::class.java).create().get()

    private fun remoteMessage(vararg data: Pair<String, String>): RemoteMessage =
        RemoteMessage.Builder("wls@fcm").apply { data.forEach { addData(it.first, it.second) } }.build()

    private fun postedNotifications(): List<Notification> =
        shadowOf(notificationManager).allNotifications

    @Test
    fun `a notification payload is shown as a notification`() {
        service().onMessageReceived(
            remoteMessage(
                "gcm.n.e" to "1",
                "gcm.notification.title" to "پیام جدید از Ali",
                "gcm.notification.body" to "سلام، سوالی داشتم"
            )
        )

        val notification = postedNotifications().single()
        assertEquals("پیام جدید از Ali", notification.extras.getString(Notification.EXTRA_TITLE))
        assertEquals("سلام، سوالی داشتم", notification.extras.getString(Notification.EXTRA_TEXT))
    }

    @Test
    fun `a data-only payload falls back to the default title and the data body`() {
        service().onMessageReceived(remoteMessage("body" to "سلام"))

        val notification = postedNotifications().single()
        assertEquals("پیام جدید", notification.extras.getString(Notification.EXTRA_TITLE))
        assertEquals("سلام", notification.extras.getString(Notification.EXTRA_TEXT))
    }

    @Test
    fun `an empty payload still shows a notification with an empty body`() {
        service().onMessageReceived(remoteMessage())

        val notification = postedNotifications().single()
        assertEquals("پیام جدید", notification.extras.getString(Notification.EXTRA_TITLE))
        assertEquals("", notification.extras.getString(Notification.EXTRA_TEXT))
    }

    @Test
    fun `the messages notification channel is created before notifying`() {
        service().onMessageReceived(remoteMessage("body" to "سلام"))

        val channel = notificationManager.getNotificationChannel("wls_agent_messages")
        assertEquals("پیام‌های مشتریان", channel.name)
        assertEquals(NotificationManager.IMPORTANCE_HIGH, channel.importance)
    }

    @Test
    fun `the notification is auto cancelled and opens the conversation list`() {
        service().onMessageReceived(remoteMessage("body" to "سلام"))

        val notification = postedNotifications().single()
        assertTrue(notification.flags and Notification.FLAG_AUTO_CANCEL != 0)
        val intent = shadowOf(notification.contentIntent).savedIntent
        assertEquals(
            "ir.axio.wlsagent.ConversationListActivity",
            intent.component?.className
        )
    }

    @Test
    fun `each message posts its own notification`() {
        val service = service()
        service.onMessageReceived(remoteMessage("body" to "اولی"))
        service.onMessageReceived(remoteMessage("body" to "دومی"))

        assertEquals(2, postedNotifications().size)
    }
}
