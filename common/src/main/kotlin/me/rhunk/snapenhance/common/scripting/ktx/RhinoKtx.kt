package me.rhunk.snapenhance.common.scripting.ktx

import com.faendir.rhino_android.RhinoAndroidHelper
import org.mozilla.javascript.Context
import org.mozilla.javascript.Function
import org.mozilla.javascript.Scriptable
import org.mozilla.javascript.ScriptableObject
import org.mozilla.javascript.Wrapper
import java.io.File

private val rhinoAndroidHelper = RhinoAndroidHelper(null as File?)

fun contextScope(f: Context.() -> Any?): Any? {
    val context = rhinoAndroidHelper.enterContext().apply {
        languageVersion = Context.VERSION_ES6
    }
    try {
        return context.f().let {
            if (it is Wrapper) {
                it.unwrap()
            } else it
        }
    } finally {
        Context.exit()
    }
}

fun Scriptable.scriptable(name: String): Scriptable? {
    return this.get(name, this) as? Scriptable
}

fun Scriptable.function(name: String): Function? {
    return this.get(name, this) as? Function
}

fun ScriptableObject.putFunction(name: String, proxy: Scriptable.(Array<out Any?>?) -> Any?) {
    this.putConst(name, this, object: org.mozilla.javascript.BaseFunction() {
        override fun call(
            cx: Context?,
            scope: Scriptable,
            thisObj: Scriptable,
            args: Array<out Any>?
        ): Any? {
            return thisObj.proxy(args)
        }
    })
}

fun scriptableObject(name: String? = "ScriptableObject", f: ScriptableObject.() -> Unit): ScriptableObject {
    return object: ScriptableObject() {
        override fun getClassName() = name
    }.apply(f)
}