package xyz.a1api.retrofit

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import kotlinx.coroutines.Job
import org.jetbrains.anko.support.v4.findOptional


/**
 * Created by Cat-x on 2018/12/29.
 * For Text4
 * Cat-x All Rights Reserved
 */
abstract class BaseFragment : Fragment() {

    open val intentData: Intent?
        get() {
            val bActivity = activity
            return if (bActivity is BaseActivity) {
                bActivity.intentData
            } else
                null
        }

    abstract val layoutResId: Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(layoutResId, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findOptional<ImageView>(R.id.backButton)?.setOnClickListener { activity?.onBackPressed() }
        initViews()
        initData()
    }

    abstract fun initViews()

    open fun initData() {}

    fun delayShowWaitDialog(string: String = ""): Job? {
        val bActivity = activity
        return if (bActivity is BaseActivity) {
            bActivity.delayShowWaitDialog()
        } else
            null
    }

    fun showWaitDialog(string: String = "") {
        val bActivity = activity
        if (bActivity is BaseActivity) {
            bActivity.showWaitDialog(string)
        }
    }

    fun closeWaitDialog() {
        val bActivity = activity
        if (bActivity is BaseActivity) {
            bActivity.closeWaitDialog()
        }
    }


    companion object {

//        inline fun <reified T : BaseFragment> initLayout(@LayoutRes layoutResId: Int): T {
//            val fragment = T::class.java.getConstructor().newInstance()
//            val bundle = fragment.arguments ?: Bundle()
//            bundle.putInt("layoutResId", layoutResId)
//            fragment.arguments = bundle
//            return fragment
//        }

        inline fun <reified T : BaseFragment> T.addData(data: Bundle.() -> Unit): T {
            val bundle = arguments ?: Bundle()
            bundle.data()
            arguments = bundle
            return this
        }

    }
}