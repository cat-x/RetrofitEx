package xyz.a1api.retrofit

import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties

/**
 * Created by Cat-x on 2019/4/3.
 * For RetrofitEx
 * Cat-x All Rights Reserved
 */
interface BodyMap : Map<String, Any> {

    /*** 下面方式必须实现，因为Retrofit会调用*/
    override val entries: Set<Map.Entry<String, Any>>
        get() = getMapEntries()

    private fun getMapEntries(): Set<Map.Entry<String, Any>> {
        val data = mutableSetOf<MutableMap.MutableEntry<String, Any>>()
        for (member in this::class.declaredMemberProperties) {
            if (member.name != "entries") {
                val value = member.getter.call(this) ?: ""
                data.add(MapData(getName(member), value))
            }
        }
        return data
    }


    private fun <T> getName(member: KProperty1<T, *>): String {
        var name = member.name
        for (annotation in member.annotations) {
            if (annotation is RequestName) {
                name = annotation.value
                break
            }
        }
        return name
    }
    /*** 上面方式必须实现，因为Retrofit 会调用*/

    /*** 下面方式均可不实现，因为Retrofit 不会调用*/
    /*** 下面方式均可不实现，因为Retrofit 不会调用*/
    /*** 下面方式均可不实现，因为Retrofit 不会调用*/

    override val keys: Set<String>
        get() = getMapKeys()

    private fun getMapKeys(): Set<String> {
        val data = mutableSetOf<String>()
        for (member in this::class.declaredMemberProperties) {
            if (member.name != "entries") {
                data.add(member.name)
            }
        }
        return data
    }


    override val size: Int
        get() = this::class.declaredMemberProperties.size - 1


    override val values: Collection<Any>
        get() = getMapValues()

    private fun getMapValues(): List<Any> {
        val data = mutableListOf<Any>()
        for (member in this::class.declaredMemberProperties) {
            if (member.name != "entries") {
                data.add(member.getter.call(this) ?: "")
            }
        }
        return data
    }

    override fun containsKey(key: String): Boolean {
        for (member in this::class.declaredMemberProperties) {
            val find = member.name == key
            if (find) return true
        }
        return false
    }

    override fun containsValue(value: Any): Boolean {
        for (member in this::class.declaredMemberProperties) {
            val find = member.getter.call(this) == value
            if (find) return true
        }
        return false
    }

    override fun get(key: String): Any? {
        for (member in this::class.declaredMemberProperties) {
            val find = member.name == key
            if (find) return member.getter.call(this)
        }
        return null
    }

    override fun isEmpty(): Boolean {
        return size <= 0
    }
    /*** 上面方式均可不实现，因为Retrofit 不会调用*/
    /*** 上面方式均可不实现，因为Retrofit 不会调用*/
    /*** 上面方式均可不实现，因为Retrofit 不会调用*/


    class MapData(override val key: String, override var value: Any) : MutableMap.MutableEntry<String, Any> {
        override fun setValue(newValue: Any): Any {
            val oldValue = value
            value = newValue
            return oldValue
        }
    }
}

/**
 * An annotation that indicates this member should be serialized to JSON with
 * the provided name value as its field name.
 *
 * <p>This annotation will override any {@link com.google.gson.FieldNamingPolicy}, including
 * the default field naming policy, that may have been set on the {@link com.google.gson.Gson}
 * app.  A different naming policy can set using the {@code GsonBuilder} class.  See
 * {@link com.google.gson.GsonBuilder#setFieldNamingPolicy(com.google.gson.FieldNamingPolicy)}
 * for more information.</p>
 *
 * <p>Here is an example of how this annotation is meant to be used:</p>
 * <pre>
 * public class MyClass {
 *   &#64SerializedName("name") String a;
 *   &#64SerializedName(value="name1", alternate={"name2", "name3"}) String b;
 *   String c;
 *
 *   public MyClass(String a, String b, String c) {
 *     this.a = a;
 *     this.b = b;
 *     this.c = c;
 *   }
 * }
 * </pre>
 *
 * <p>The following shows the output that is generated when serializing an app of the
 * above example class:</p>
 * <pre>
 * MyClass target = new MyClass("v1", "v2", "v3");
 * Gson gson = new Gson();
 * String json = gson.toJson(target);
 * System.out.println(json);
 *
 * ===== OUTPUT =====
 * {"name":"v1","name1":"v2","c":"v3"}
 * </pre>
 *
 * <p>NOTE: The value you specify in this annotation must be a valid JSON field name.</p>
 * While deserializing, all values specified in the annotation will be deserialized into the field.
 * For example:
 * <pre>
 *   MyClass target = gson.fromJson("{'name1':'v1'}", MyClass.class);
 *   assertEquals("v1", target.b);
 *   target = gson.fromJson("{'name2':'v2'}", MyClass.class);
 *   assertEquals("v2", target.b);
 *   target = gson.fromJson("{'name3':'v3'}", MyClass.class);
 *   assertEquals("v3", target.b);
 * </pre>
 * Note that MyClass.b is now deserialized from either name1, name2 or name3.
 *
 * @see com.google.gson.FieldNamingPolicy
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
@MustBeDocumented
@Retention
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.FUNCTION)
annotation class RequestName(
    /**
     * @return the desired name of the field when it is serialized or deserialized
     */
    val value: String
)