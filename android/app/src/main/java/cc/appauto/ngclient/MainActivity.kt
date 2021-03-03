package cc.appauto.ngclient

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import cc.appauto.lib.ng.AppAutoContext
import cc.appauto.lib.ng.checkPermissions
import com.facebook.react.PackageList
import com.facebook.react.ReactInstanceManager
import com.facebook.react.ReactPackage
import com.facebook.react.ReactRootView
import com.facebook.react.common.LifecycleState
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler

private const val TAG = "ngclient"

class MainActivity : AppCompatActivity(), DefaultHardwareBackBtnHandler {
    private var mReactRootView: ReactRootView? = null
    private var mReactInstanceManager: ReactInstanceManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mReactRootView = ReactRootView(this)
        val packages: MutableList<ReactPackage> = PackageList(application).packages
        packages.add(AutoNgPackage())
        mReactInstanceManager = ReactInstanceManager.builder()
            .setApplication(application)
            .setCurrentActivity(this)
            .setBundleAssetName("index.android.bundle")
            .setJSMainModulePath("index")
            .addPackages(packages)
            .setUseDeveloperSupport(BuildConfig.DEBUG)
            .setInitialLifecycleState(LifecycleState.RESUMED)
            .build()
        mReactRootView?.startReactApplication(mReactInstanceManager, "ngclient", null)
        setContentView(mReactRootView)
        AppAutoContext.setupRuntime(this)
    }

    override fun invokeDefaultOnBackPressed() {
        Log.i(TAG, "invokeDefaultOnBackPressed")
        moveTaskToBack(true)
    }

    override fun onPause() {
        super.onPause()
        mReactInstanceManager?.onHostPause(this)
    }

    override fun onResume() {
        super.onResume()
        mReactInstanceManager?.onHostResume(this, this)
        AppAutoContext.checkPermissions(this)

    }

    override fun onDestroy() {
        super.onDestroy()
        mReactInstanceManager?.onHostDestroy(this)
        mReactRootView?.unmountReactApplication()

        mReactInstanceManager = null
        mReactRootView = null
    }

    override fun onBackPressed(){
        if (mReactInstanceManager != null)
            mReactInstanceManager?.onBackPressed()
        else
            moveTaskToBack(true)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_MENU && mReactInstanceManager != null) {
            mReactInstanceManager?.showDevOptionsDialog()
            val requested = AppAutoContext.mediaRuntime.requestMediaProjection()
            Log.i(TAG, "reqeust media projection: $requested")
            return true
        }
        return super.onKeyUp(keyCode, event)
    }
}
