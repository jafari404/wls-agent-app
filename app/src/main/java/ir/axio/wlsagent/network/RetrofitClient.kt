package ir.axio.wlsagent.network

import android.content.Context
import android.util.Base64
import ir.axio.wlsagent.BuildConfig
import ir.axio.wlsagent.SecureStore
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private val baseUrl = "${BuildConfig.SITE_BASE_URL}/wp-json/wls/v1/"

    private var apiInstance: ApiService? = null

    /**
     * Basic Auth با نام‌کاربری وردپرس + Application Password.
     * توجه: WP نیازمند HTTPS برای این نوع احراز هویت است.
     */
    private class BasicAuthInterceptor(private val ctx: Context) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val username = SecureStore.username(ctx) ?: throw MissingCredentialsException()
            val password = SecureStore.appPassword(ctx) ?: throw MissingCredentialsException()
            val credential = Base64.encodeToString("$username:$password".toByteArray(), Base64.NO_WRAP)
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Basic $credential")
                .build()
            return chain.proceed(request)
        }
    }

    fun api(context: Context): ApiService {
        if (apiInstance != null) return apiInstance!!

        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        val client = OkHttpClient.Builder()
            .addInterceptor(BasicAuthInterceptor(context.applicationContext))
            .addInterceptor(logging)
            .build()

        apiInstance = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)

        return apiInstance!!
    }

    /** Call after login/logout to rebuild the client with fresh credentials. */
    fun reset() {
        apiInstance = null
    }
}
