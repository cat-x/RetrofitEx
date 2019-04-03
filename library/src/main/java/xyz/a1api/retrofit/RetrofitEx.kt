package xyz.a1api.retrofit

import com.google.gson.FieldAttributes
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit

/**
 * Created by Cat-x on 2019/4/3.
 * For RetrofitEx
 * Cat-x All Rights Reserved
 */
fun GsonBuilder.useExclusionStrategy(skipField: (f: FieldAttributes?) -> Boolean = { false }): GsonBuilder {
    return addDeserializationExclusionStrategy(MyExclusionStrategy(skipField))
        .addSerializationExclusionStrategy(MyExclusionStrategy(skipField))
}

fun Retrofit.Builder.useOkHttpClient(client: OkHttpClient = OkHttpManager.okHttpClient): Retrofit.Builder {
    return client(client)
}

fun Retrofit.Builder.useCustomOkHttpClient(handle: RequestHandle): Retrofit.Builder {
    return useOkHttpClient(OkHttpManager.okHttpClientBuilder.handleRequest(handle).build())
}

typealias  RequestHandle = okhttp3.Request.Builder.(original: okhttp3.Request) -> okhttp3.Request.Builder

fun OkHttpClient.Builder.handleRequest(handle: RequestHandle): OkHttpClient.Builder {
    addInterceptor { chain ->
        val original = chain.request()
        val request = original.newBuilder()
            .handle(original).build()
        /*.header("http-token", LStorage.SP.getString(DataEx.FLAG_Token))
        .header("language", BaseLanguageActivity.getSelectLanguageForWeb() ?: "en")
        .build()*/

        chain.proceed(request)
    }
    return this
}