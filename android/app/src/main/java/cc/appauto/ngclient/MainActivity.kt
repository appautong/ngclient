package cc.appauto.ngclient

import android.app.AlertDialog
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
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
    lateinit private var mediaProjectionManager: MediaProjectionManager
    private var mediaProjection: MediaProjection? = null

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

        mediaProjectionManager = getSystemService(MediaProjectionManager::class.java)
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
        val builder = AlertDialog.Builder(this)

        val diag = builder.setTitle(cc.appauto.lib.R.string.appauto_check_permission)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false)
                .create()
        diag.show()
        AppAutoContext.workHandler.postDelayed({
            diag.dismiss()
            AppAutoContext.checkPermissions(this)
        }, 2000)
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
            if (mediaProjection == null) {
                val intent = mediaProjectionManager.createScreenCaptureIntent()
                startActivityForResult(intent, 12345)
            } else {
                AppAutoContext.automedia.takeScreenShot()
            }
            return true
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 12345) {
            if (data == null) return
            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)
            if (mediaProjection == null) {
                Log.w(TAG, "onActivityResult: media projection is null, $resultCode $data")
                return
            }
            AppAutoContext.automedia.mediaProjection = mediaProjection
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
