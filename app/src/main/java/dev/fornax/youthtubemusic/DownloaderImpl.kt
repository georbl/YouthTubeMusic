package dev.fornax.youthtubemusic

import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

class DownloaderImpl private constructor(builder: OkHttpClient.Builder) : Downloader() {
    private val mCookies: MutableMap<String?, String?> = HashMap()
    val client: OkHttpClient = builder
        .readTimeout(
            30,
            TimeUnit.SECONDS
        ) //                .cache(new Cache(new File(context.getExternalCacheDir(), "okhttp"),
        //                        16 * 1024 * 1024))
        .build()

    fun getCookies(url: String): String? {
        val youtubeCookie = if (url.contains(YOUTUBE_DOMAIN))
            getCookie(YOUTUBE_RESTRICTED_MODE_COOKIE_KEY)
        else
            null
        return youtubeCookie
    }

    fun getCookie(key: String?): String? {
        return mCookies[key]
    }

    @Throws(IOException::class, ReCaptchaException::class)
    override fun execute(request: Request): Response {
        val httpMethod = request.httpMethod()
        val url = request.url()
        val headers = request.headers()
        val dataToSend = request.dataToSend()

        var requestBody: RequestBody? = null
        if (dataToSend != null) {
            requestBody = dataToSend.toRequestBody(null, 0, dataToSend.size)
        }

        val requestBuilder = okhttp3.Request.Builder()
            .method(httpMethod, requestBody)
            .url(url)
            .addHeader("User-Agent", USER_AGENT)

        val cookies = getCookies(url)
        if (!cookies.isNullOrEmpty()) {
            requestBuilder.addHeader("Cookie", cookies)
        }

        headers.forEach { (headerName: String?, headerValueList: MutableList<String?>?) ->
            requestBuilder.removeHeader(headerName!!)
            headerValueList!!.forEach(Consumer { headerValue: String? ->
                requestBuilder.addHeader(
                    headerName,
                    headerValue!!
                )
            })
        }

        client.newCall(requestBuilder.build()).execute().use { response ->
            if (response.code == 429) {
                throw ReCaptchaException("reCaptcha Challenge requested", url)
            }
            var responseBodyToReturn: String? = null
            response.body.use { body ->
                responseBodyToReturn = body.string()
            }
            val latestUrl = response.request.url.toString()
            return Response(
                response.code,
                response.message,
                response.headers.toMultimap(),
                responseBodyToReturn,
                latestUrl
            )
        }
    }

    companion object {
        const val USER_AGENT: String =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:140.0) Gecko/20100101 Firefox/140.0"
        const val YOUTUBE_RESTRICTED_MODE_COOKIE_KEY: String = "youtube_restricted_mode_key"
        const val YOUTUBE_DOMAIN: String = "youtube.com"

        private var instance: DownloaderImpl? = null

        /**
         * It's recommended to call exactly once in the entire lifetime of the application.
         *
         * @param builder if null, default builder will be used
         * @return a new instance of [DownloaderImpl]
         */
        fun init(builder: OkHttpClient.Builder?): DownloaderImpl? {
            instance = DownloaderImpl(
                builder ?: OkHttpClient.Builder()
            )
            return instance
        }

    }
}