package xyz.a1api.retrofit

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDialog
import android.widget.ImageView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.anko.findOptional


/**
 * Created by Cat-x on 2018/12/29.
 * For Text4
 * Cat-x All Rights Reserved
 */
@SuppressLint("Registered")
abstract class BaseActivity(@LayoutRes private val layoutResId: Int) : AppCompatActivity() {

    open var intentData: Intent? = null
    private var mWaitDialog: AppCompatDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*  //当FitsSystemWindows设置 true 时，会在屏幕最上方预留出状态栏高度的 padding
          StatusBarUtil.setRootViewFitsSystemWindows(this,true);
          //设置状态栏透明
          StatusBarUtil.setTranslucentStatus(this);
          //一般的手机的状态栏文字和图标都是白色的, 可如果你的应用也是纯白色的, 或导致状态栏文字看不清
          //所以如果你是这种情况,请使用以下代码, 设置状态使用深色文字图标风格, 否则你可以选择性注释掉这个if内容
          if (!StatusBarUtil.setStatusBarDarkTheme(this, true)) {
              //如果不支持设置深色风格 为了兼容总不能让状态栏白白的看不清, 于是设置一个状态栏颜色为半透明,
              //这样半透明+白=灰, 状态栏的文字能看得清
              StatusBarUtil.setStatusBarColor(this,0x55000000);
          }*/

        parseIntentData()
        setContentView(layoutResId)
        findOptional<ImageView>(R.id.backButton)?.setOnClickListener { onBackPressed() }
        initViews()
        initData()
    }

    open fun parseIntentData() {
        intentData = intent
    }

    abstract fun initViews()

    open fun initData() {

    }

    fun delayShowWaitDialog(string: String = ""): Job {
        return GlobalScope.launch {
            delay(1000)
            showWaitDialog(string)
            delay(9 * 1000)
            if (!isDestroyed && !isFinishing) {
                if (mWaitDialog != null && mWaitDialog?.isShowing == true) {
                    closeWaitDialog()
                }
            }
        }
    }

    fun showWaitDialog(string: String = "") {
        runOnUiThread {
            if (!this.isFinishing) {
                if (mWaitDialog == null) {
                    val dialog = AppCompatDialog(this/*,R.style.MyDialog*/)
                    dialog.setContentView(R.layout.widget_dialog_waiting)
                    dialog.setCancelable(true)
                    mWaitDialog = dialog
                }
                if (!isDestroyed && !isFinishing) {
                    if (mWaitDialog?.isShowing == false) {
//                        if (string.isNotBlank()) {
//                            mWaitDialog?.findViewById<AppCompatTextView>(R.id.messageTextView)?.text = string
//                        }
//                        mWaitDialog?.show()
//                        mWaitDialog?.findViewById<ImageView>(R.id.waitingImageView)?.let {
//                            Glide.with(this).load(R.drawable.loading).into(it)
//                        }
                    }
                }
            }
        }
    }

    fun closeWaitDialog() {
        if (mWaitDialog != null && mWaitDialog!!.isShowing) {
            mWaitDialog?.dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        closeWaitDialog()
    }
}