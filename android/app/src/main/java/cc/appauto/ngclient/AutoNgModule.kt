package cc.appauto.ngclient

import cc.appauto.lib.ng.AppAutoContext
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

class AutoNgModule(ctx: ReactApplicationContext): ReactContextBaseJavaModule(ctx) {
    private val name = "autong_module"

    override fun getName(): String {
        return name
    }

    @ReactMethod()
    fun hierarchyString(promise: Promise) {
        promise.resolve(AppAutoContext.topAppHierarchyString)
    }
}