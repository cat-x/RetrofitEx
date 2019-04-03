package xyz.a1api.retrofit

import com.google.gson.annotations.SerializedName

/**
 * Created by Cat-x on 2019/4/3.
 * For RetrofitEx
 * Cat-x All Rights Reserved
 */
data class Data<T>(
    @SerializedName("result")
    val result: Result<T> = Result()
) {
    data class Result<T>(
        @SerializedName("data")
        var `data`: T? = null,
        @SerializedName("status")
        val status: Status = Status()
    ) {

        data class Status(
            @SerializedName("code")
            val code: Int = -1,
            @SerializedName("msg")
            val msg: String = ""
        )
    }
}