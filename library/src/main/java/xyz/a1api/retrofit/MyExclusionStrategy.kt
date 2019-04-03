package xyz.a1api.retrofit

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes


/**
 * Created by Cat-x on 2019/4/3.
 * For RetrofitEx
 * Cat-x All Rights Reserved
 */
class MyExclusionStrategy(val skipField: (f: FieldAttributes?) -> Boolean = { false }) : ExclusionStrategy {

    override fun shouldSkipClass(clazz: Class<*>?): Boolean {
        return false
    }

    override fun shouldSkipField(f: FieldAttributes?): Boolean {
//        不能够使用kotlin注解判断，因为getAnnotation获取不到
        return (skipField(f) || f?.getAnnotation(NotExpose::class.java) != null)
    }
}
