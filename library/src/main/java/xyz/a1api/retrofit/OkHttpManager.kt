package xyz.a1api.retrofit

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.parkingwang.okhttp3.LogInterceptor.LogInterceptor
import okhttp3.*
import java.io.IOException
import java.net.URLEncoder
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


/**
 * Created by Cat-x on 2019/4/3.
 * For RetrofitEx
 * Cat-x All Rights Reserved
 */
object OkHttpManager {
    var isDebug: Boolean = true

    private lateinit var handler: Handler
    fun initHandle() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            handler = Handler(Looper.getMainLooper())
        } else {
            throw IllegalStateException("Please call RestApi.initHandle in main thread.")
        }
    }

    fun run(call: () -> Unit) {
        handler.post { call() }
    }


    private val JSONType = MediaType.parse("application/json; charset=utf-8")
    var timeOut = 30000L
    private val DO_NOT_VERIFY = HostnameVerifier { _, _ -> true }
    private val x509TrustManager = object : X509TrustManager {
        @SuppressLint("TrustAllX509TrustManager")
        override fun checkClientTrusted(chain: Array<X509Certificate>?, authType: String?) {
        }

        @SuppressLint("TrustAllX509TrustManager")
        override fun checkServerTrusted(chain: Array<X509Certificate>?, authType: String?) {
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return arrayOf()
        }
    }

    val cookieStore: HashMap<String, MutableList<Cookie>> = hashMapOf()

    @Suppress("MemberVisibilityCanBePrivate")
    var okHttpClientBuilder = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .build()

            chain.proceed(request)
        }.apply {
            if (isDebug) {
                this.addInterceptor(LogInterceptor())// 仅供测试时使用
            }
        }
        .cookieJar(object : CookieJar {
            override fun saveFromResponse(url: HttpUrl, cookies: MutableList<Cookie>) {
                cookieStore[url.host()] = cookies
            }

            override fun loadForRequest(url: HttpUrl): MutableList<Cookie> {
                return cookieStore[url.host()] ?: mutableListOf()
            }

        })
        .connectTimeout(timeOut, TimeUnit.MILLISECONDS)
        .readTimeout(timeOut, TimeUnit.MILLISECONDS)
        .writeTimeout(timeOut, TimeUnit.MILLISECONDS)
        //其他配置
        .sslSocketFactory(getSSLContext().socketFactory, x509TrustManager)
        .hostnameVerifier(DO_NOT_VERIFY)

    @Suppress("MemberVisibilityCanBePrivate")
    var okHttpClient = okHttpClientBuilder.build()


    private fun getSSLContext(): SSLContext {
        val sslContext: SSLContext = SSLContext.getInstance("SSL")
        try {
            sslContext.init(null, arrayOf<TrustManager>(x509TrustManager), SecureRandom())
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: KeyManagementException) {
            e.printStackTrace()
        }
        return sslContext
    }


    fun getJSONBody(jsonObject: JsonObject): RequestBody {
        return RequestBody.create(JSONType, jsonObject.toString())
    }

    fun getFromJSONBody(dataForm: HashMap<String, Any?>): RequestBody {
        return RequestBody.create(JSONType, GsonBuilder().enableComplexMapKeySerialization().create().toJson(dataForm))
    }

    fun getFormBody(dataForm: HashMap<String, Any?>): RequestBody {
        val formBody = FormBody.Builder()
        for (entry in dataForm.entries) {
            formBody.add(entry.key, entry.value?.toString() ?: "")
        }
        return formBody.build()
    }

    fun getMultipartBody(
        partList: ArrayList<MultipartBody.Part>,
        mediaType: MediaType = MultipartBody.FORM
    ): RequestBody {
        val multipartBody = MultipartBody.Builder().setType(mediaType)
        for (part in partList) {
            multipartBody.addPart(part)
        }
        return multipartBody.build()
    }

    private fun makeRequest(url: String, headerMap: HashMap<String, String>): Request.Builder {
        val request = Request.Builder().url(url)
        for (entry in headerMap.entries) {
            request.addHeader(entry.key, entry.value)
        }
        return request
    }

    fun post(
        url: String,
        requestBody: RequestBody,
        headerMap: LinkedHashMap<String, String> = linkedMapOf(),
        callBack: (json: JsonObject) -> Unit = {}
    ) {
        val request = makeRequest(url, headerMap).post(requestBody).build()
        newCall(request, callBack)
    }

    fun get(
        url: String,
        headerMap: HashMap<String, String> = hashMapOf(),
        requestBody: LinkedHashMap<String, Any> = linkedMapOf(),
        callBack: (json: JsonObject) -> Unit = {}
    ) {
        val stringBuffer = StringBuffer()
        if (requestBody.isNotEmpty()) {
            stringBuffer.append("?")
            for (entry in requestBody.entries) {
                stringBuffer.append(entry.key + "=" + URLEncoder.encode(entry.value.toString(), "utf-8"))
                stringBuffer.append("&")
            }
            stringBuffer.dropLast(1)
        }
        val request = makeRequest(url + stringBuffer.toString(), headerMap).get().build()

        newCall(request, callBack)
    }

    private fun newCall(request: Request, call: (json: JsonObject) -> Unit) {
        var responseJSON: JsonObject
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call?, response: Response?) {
                if (response != null) {
                    val body = response.body()
                    if (body != null) {
                        val s = body.string()
                        try {
                            Log.i("newCall", "json: $s")
                            responseJSON = JsonParser().parse(s).asJsonObject
                        } catch (e: Exception) {
                            e.printStackTrace()
                            responseJSON = JsonObject().put("code", ApiCode.RESPONSE_CODE_FAIL_UNKNOWN)
                                .put(ApiCode.RESPONSE_MESSAGE, e.message)
                        }
                    } else {
                        responseJSON = JsonObject().put("code", ApiCode.RESPONSE_CODE_FAIL_BODY_IS_NULL)
                            .put(ApiCode.RESPONSE_MESSAGE, "body is null")
                    }
                } else {
                    responseJSON = JsonObject().put("code", ApiCode.RESPONSE_CODE_FAIL_RESPONSE_IS_NULL)
                        .put(ApiCode.RESPONSE_MESSAGE, "response is null")
                }

                OkHttpManager.run { call(responseJSON) }
            }

            override fun onFailure(call: Call?, e: IOException?) {
                var message = e?.message
                if (message != null) {
                    if (message.contains("java.net.SocketTimeoutException", true)
                        || message.contains("Failed to connect", true)
                        || message.contains("timeout", true)
                        || message.contains("time out", true)
                    ) {
                        message = ""/*App.app.getString(R.string.network_anomalies)*/
                    }
                }
                responseJSON = JsonObject().put("code", ApiCode.RESPONSE_CODE_FAIL_UNKNOWN)
                    .put(ApiCode.RESPONSE_MESSAGE, message)
                call(responseJSON)
            }
        })
    }

}

fun JsonObject.put(property: String, value: Char?): JsonObject {
    addProperty(property, value)
    return this
}

fun JsonObject.put(property: String, value: Boolean?): JsonObject {
    addProperty(property, value)
    return this
}

fun JsonObject.put(property: String, value: Number?): JsonObject {
    addProperty(property, value)
    return this
}

fun JsonObject.put(property: String, value: String?): JsonObject {
    addProperty(property, value)
    return this
}

fun JsonObject.put(property: String, value: JsonElement?): JsonObject {
    add(property, value)
    return this
}