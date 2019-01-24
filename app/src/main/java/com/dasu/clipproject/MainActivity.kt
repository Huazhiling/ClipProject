package com.dasu.clipproject

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ToastUtils
import com.dasu.clipproject.activity.SettingActivity
import com.dasu.clipproject.common.Constans.IS_WINDOW
import com.dasu.clipproject.listener.IWindowOnClickListener
import com.dasu.clipproject.service.SuspensionWindowService
import com.gyf.barlibrary.ImmersionBar
import com.per.rslibrary.IPermissionRequest
import com.per.rslibrary.RsPermission
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var isWindow = SPUtils.getInstance().getBoolean(IS_WINDOW)
    private var windowService: SuspensionWindowService.WindowBinder? = null
    private val OVERLAY_PERMISSION_REQ_CODE = 200


    private fun initView() {
        ImmersionBar.with(this).statusBarView(R.id.status_bar).statusBarDarkFont(true).init()
        control_window.isChecked = isWindow
        if (isWindow) {
            control_window.text = "关闭"
            checkWindowStatus()
        } else {
            control_window.text = "启动"
        }
    }

    private fun initData() {
        control_window.setOnCheckedChangeListener { buttonView, isChecked ->
            //            //如果是false  就是没有创建过
            if (isChecked) {
                checkWindowStatus()
            } else {
                control_window.text = "启动"
                if (windowService != null) {
                    windowService = null
                    unbindService(windowConnection)
                }
            }
            SPUtils.getInstance().put(IS_WINDOW, isChecked)
        }
        toolbar.setNavigationOnClickListener { moveTaskToBack(true) }
    }

    private fun checkWindowStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(this)) {
                createWindowManneger()
            } else {
                var intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE)
            }
        } else {
            createWindowManneger()
        }
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        RsPermission.getInstance().onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun createWindowManneger() {
        isWindow = true
        control_window.text = "关闭"
        var serviceIntent = intent
        serviceIntent.setClass(this, SuspensionWindowService::class.java)
        bindService(serviceIntent, windowConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        initData()
    }

    private var windowConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            windowService = null
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service is SuspensionWindowService.WindowBinder) {
                windowService = service
                LogUtils.e("WindowToast", windowService)
                windowService!!.getService.setWindowOnClickListener(object : IWindowOnClickListener {
                    override fun startSettingActivity() {
                        var intent = Intent(this@MainActivity,SettingActivity::class.java)
                        startActivity(intent)
                    }

                    override fun openClipWindow() {
                        windowService!!.updateClipWindowView()
                    }

                    override fun getDesrc(msg: String) {

//                        RsPermission.getInstance()
//                                .setRequestCode(200)
//                                .setiPermissionRequest(object : IPermissionRequest {
//                                    override fun toSetting() {
//
//                                    }
//
//                                    override fun cancle(p0: Int) {
//
//                                    }
//
//                                    override fun success(p0: Int) {
//
//                                    }
//
//                                }).requestPermission(this@MainActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }

                    override fun openClipManagerView() {
                        windowService!!.updateClipManagerView()
                    }
                })
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (windowService != null) {
            if (windowService!!.getService.getIsClipManager()) {
                windowService!!.updateClipWindowView()
            }
        }
    }

    override fun onDestroy() {
        SPUtils.getInstance().put(IS_WINDOW, false)
        control_window.isChecked = true
        if (windowService != null) {
            unbindService(windowConnection)
        }
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (!Settings.canDrawOverlays(this)) {
                    ToastUtils.showShort("未给予权限，无法使用该功能")
                    control_window.isChecked = false
                    control_window.text = "启动"
                    SPUtils.getInstance().put(IS_WINDOW, false)
                } else {
                    createWindowManneger()
                }
            }
        }
    }
}
