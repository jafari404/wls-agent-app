package ir.axio.wlsagent.network

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RetrofitClientTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() = RetrofitClient.reset()

    @After
    fun tearDown() = RetrofitClient.reset()

    @Test
    fun `the api client is created once and reused`() {
        assertSame(RetrofitClient.api(context), RetrofitClient.api(context))
    }

    @Test
    fun `reset rebuilds the api client so new credentials are picked up`() {
        val before = RetrofitClient.api(context)
        RetrofitClient.reset()

        assertNotSame(before, RetrofitClient.api(context))
    }
}
