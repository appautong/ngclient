package cc.appauto.ngclient

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.Settings
import android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.loader.content.AsyncTaskLoader
import com.facebook.react.PackageList
import com.facebook.react.ReactInstanceManager
import com.facebook.react.ReactPackage
import com.facebook.react.ReactRootView
import com.facebook.react.common.LifecycleState
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler
import cc.appauto.lib.ng.AppAutoContext
import cc.appauto.lib.ng.UIAnnotation
import cc.appauto.lib.ng.getAppBound
import cc.appauto.lib.ng.randomInt

private const val TAG = "ngclient"

class MainActivity : AppCompatActivity(), DefaultHardwareBackBtnHandler {
    private var mReactRootView: ReactRootView? = null
    private var mReactInstanceManager: ReactInstanceManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mReactRootView = ReactRootView(this)
        val packages: List<ReactPackage> = PackageList(application).packages
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

    fun showOverylay() {
        val overlay = Settings.canDrawOverlays(this.applicationContext)
        Log.i(TAG, "can draw overlay: $overlay")
        if (!overlay) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            this.startActivity(intent)
            return
        }
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val params = WindowManager.LayoutParams()
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        params.gravity = Gravity.LEFT or Gravity.TOP
        params.format = PixelFormat.RGBA_8888
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        params.width = randomInt(100, 600)
        params.height = randomInt(100, 1200)
        params.x = randomInt(0, 200)
        params.y = randomInt(0, 200)
        val anno = UIAnnotation(this.applicationContext, Rect(params.x, params.y, params.width+params.x, params.height+params.y))
        wm.addView(anno, params)
        anno.invalidate()
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_MENU && mReactInstanceManager != null) {
            showOverylay()
            mReactInstanceManager?.showDevOptionsDialog()
            return true
        }
        return super.onKeyUp(keyCode, event)
    }
}
