package ir.axio.wlsagent.network

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

/**
 * فراخوانی API در scope اکتیویتی، با گرفتن استثناها.
 * نتیجه در [onResult] برگردانده می‌شود تا هر صفحه فقط منطق خودش را بنویسد.
 */
fun <T> AppCompatActivity.apiCall(
    block: suspend (ApiService) -> T,
    onResult: (Result<T>) -> Unit = {}
) {
    lifecycleScope.launch {
        onResult(runCatching { block(RetrofitClient.api(this@apiCall)) })
    }
}

/** نسخه کوتاه برای زمانی که فقط موفقیت اهمیت دارد. */
fun <T> AppCompatActivity.apiCallOnSuccess(
    block: suspend (ApiService) -> T,
    onSuccess: (T) -> Unit
) = apiCall(block) { it.onSuccess(onSuccess) }
