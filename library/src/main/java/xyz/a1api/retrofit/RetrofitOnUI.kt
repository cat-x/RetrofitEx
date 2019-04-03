package xyz.a1api.retrofit

import android.app.Activity
import android.app.Fragment
import android.content.Context
import kotlinx.coroutines.*
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.ref.WeakReference
import kotlin.reflect.KFunction0
import android.support.v4.app.Fragment as SupportFragment

/**
 * Created by Cat-x on 2019/4/3.
 * For RetrofitEx
 * Cat-x All Rights Reserved
 */

/**
 * 执行网络请求任务
 * @receiver Call<T>
 * @param success (data: T?, body: Response<T>) -> Unit 成功回调
 * @param fail (t: Throwable) -> Unit 失败回调
 * @param failNumberOfRetries Int 失败重试次数，默认为0
 */
fun <T> Call<T>.done(
    success: (data: T?, body: Response<T>) -> Unit,
    fail: (t: Throwable) -> Unit,
    failNumberOfRetries: Int = 0
) {

    enqueue(object : Callback<T> {
        override fun onResponse(call: Call<T>, response: Response<T>) {
            if (checkLife()) return
            if (response.body() == null || !response.isSuccessful) {
                onFailure(call, Throwable("response.body() is null", Throwable(response.code().toString())))
            } else {
//                checkLoginStatus(response)
                success(response.body(), response)
            }
        }


        override fun onFailure(call: Call<T>, t: Throwable) {
            if (checkLife()) return
            if (failNumberOfRetries > 0) {
                call.clone().done<T>(success, fail, failNumberOfRetries - 1)
            } else {
                fail(t)
            }
        }

//        private fun checkLoginStatus(response: Response<T>) {
//            val status = (response.body() as? Data<*>)
//            if (status != null) {
//                if (status.result.status.code == ApiCode.CODE_IS_LOGIN_INVALID) {
//                    App.app.toast(App.app.getString(R.string.user_information_is_invalid))
//                    Router.goRELogin()
//                }
//            } else {
//                val status2 = (response.body() as? ZQPageData<*>)
//                if (status2 != null) {
//                    if (status2.result.status.code == ApiCode.CODE_IS_LOGIN_INVALID) {
//                        App.app.toast(App.app.getString(R.string.user_information_is_invalid))
//                        Router.goRELogin()
//                    }
//                }
//            }
//        }

        private fun checkLife(): Boolean {
            if (this@done is LifeCall<*>) {
                job.get()?.cancel()
                val context = life.get()
                if (context is Activity) {
                    if (context.isDestroyed || context.isFinishing) {
                        LogEx.i("checkLife Activity", "context.isDestroyed || context.isFinishing")
                        return true
                    }
                } else if (context is SupportFragment) {
                    if (context.isDetached || !context.isAdded) {
                        LogEx.i("checkLife SupportFragment", "context.isDetached || !context.isAdded")
                        return true
                    }
                } else if (context is Fragment) {
                    if (context.isDetached || !context.isAdded) {
                        LogEx.i("checkLife Fragment", "context.isDetached || !context.isAdded")
                        return true
                    }
                }
                callClose?.invoke()
            }

            return false
        }

    })
}

fun <T> Call<T>.doneUI(
    success: (data: T?, body: Response<T>) -> Unit,
    fail: (t: Throwable) -> Unit = { App.app.toast(App.app.getString(R.string.failed_to_get_data_please_check_the_network)) },
    failNumberOfRetries: Int = 0
) {
    GlobalScope.launch(Dispatchers.Main, CoroutineStart.DEFAULT) {
        done(success, fail, failNumberOfRetries)
    }
}

fun <T> Call<T>.with(activity: BaseActivity, content: String = ""): LifeCall<T> {
    return LifeCall(activity, this, activity.delayShowWaitDialog(content), activity::closeWaitDialog)
}

fun <T> Call<T>.with(fragment: BaseFragment, content: String = ""): LifeCall<T> {
    return LifeCall(fragment, this, fragment.delayShowWaitDialog(content), fragment::closeWaitDialog)
}

fun <T> Call<T>.with(context: Context): LifeCall<T> {
    return if (context is BaseActivity) {
        with(context)
    } else {
        LifeCall(context, this)
    }
}

fun <T> Call<T>.withOnlyLife(activity: Activity): LifeCall<T> {
    return LifeCall(activity, this)
}

fun <T> Call<T>.withOnlyLife(fragment: SupportFragment): LifeCall<T> {
    return LifeCall(fragment, this)
}

fun <T> Call<T>.withOnlyLife(fragment: Fragment): LifeCall<T> {
    return LifeCall(fragment, this)
}

fun <T> Call<T>.withOnlyLife(context: Context): LifeCall<T> {
    return LifeCall(context, this)
}

class LifeCall<T>(
    any: Any,
    val call: Call<T>,
    job: Job? = null,
    val callClose: KFunction0<Unit>? = null
) : Call<T> by call {
    var life = WeakReference(any)
    val job = WeakReference(job)
}