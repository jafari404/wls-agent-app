package ir.axio.wlsagent.util

import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

/**
 * اکتیویتی پایه برای صفحاتی که به‌صورت دوره‌ای داده را تازه می‌کنند.
 * پولینگ در onResume شروع و در onPause متوقف می‌شود.
 */
abstract class PollingActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())

    private val pollRunnable = object : Runnable {
        override fun run() {
            onPoll()
            handler.postDelayed(this, pollIntervalMs)
        }
    }

    protected abstract val pollIntervalMs: Long

    protected abstract fun onPoll()

    override fun onResume() {
        super.onResume()
        handler.post(pollRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(pollRunnable)
    }
}
