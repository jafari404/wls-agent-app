package ir.axio.wlsagent.network

import android.util.Log
import androidx.annotation.StringRes
import ir.axio.wlsagent.R
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

private const val TAG = "WlsApi"

/** A request failure that carries a message which can be shown to the user. */
sealed class ApiException(
    @StringRes val userMessageRes: Int,
    message: String,
    cause: Throwable?
) : Exception(message, cause) {

    class Network(cause: IOException) :
        ApiException(R.string.error_network, "Network failure", cause)

    /** Missing or rejected credentials. [code] is null when no credentials are stored at all. */
    class Unauthorized(val code: Int?, cause: Throwable?) :
        ApiException(R.string.error_unauthorized, "Unauthorized (code=$code)", cause)

    class Server(val code: Int, cause: HttpException) :
        ApiException(R.string.error_server, "Server returned $code", cause)

    /** The server answered successfully but reported that the operation was not performed. */
    class Rejected(operation: String) :
        ApiException(R.string.error_rejected, "Server rejected $operation", null)

    class Unexpected(cause: Throwable) :
        ApiException(R.string.error_unexpected, "Unexpected failure", cause)
}

fun Throwable.toApiException(): ApiException = when (this) {
    is ApiException -> this
    is MissingCredentialsException -> ApiException.Unauthorized(null, this)
    is IOException -> ApiException.Network(this)
    is HttpException ->
        if (code() == 401 || code() == 403) ApiException.Unauthorized(code(), this)
        else ApiException.Server(code(), this)
    else -> ApiException.Unexpected(this)
}

/**
 * Runs an API call and maps any failure to an [ApiException], logging it. Coroutine cancellation is
 * rethrown so that it is never reported as a request failure.
 */
suspend fun <T> apiCall(operation: String, block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        val mapped = e.toApiException()
        Log.e(TAG, "$operation failed", mapped)
        Result.failure(mapped)
    }

/** Turns an `{ "ok": false }` response into a failure instead of treating it as success. */
fun OkResponse.requireOk(operation: String): OkResponse {
    if (!ok) throw ApiException.Rejected(operation)
    return this
}
