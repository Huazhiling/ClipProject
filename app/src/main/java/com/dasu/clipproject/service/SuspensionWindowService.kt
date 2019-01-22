package com.dasu.clipproject.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SPUtils
import com.dasu.clipproject.R
import com.dasu.clipproject.common.Constans.IS_WINDOW
import com.dasu.clipproject.listener.IWindowOnClickListener
import kotlinx.android.synthetic.main.layout_window.view.*

class SuspensionWindowService : Service() {
    private lateinit var touchManager: WindowManager
    private lateinit var windowManagerLayoutParams: WindowManager.LayoutParams
    private lateinit var windowContentView: View
//    private lateinit var windowBinder: SuspensionWindowService.WindowBinder
    private var windowClick: IWindowOnClickListener? = null
    override fun onCreate() {
        super.onCreate()
        createOnWindowManager()
    }

    fun setWindowOnClickListener(windowClick: IWindowOnClickListener) {
        this.windowClick = windowClick
        LogUtils.e("WindowToast", windowClick)
    }

    fun removeView() {
        touchManager.removeView(windowContentView)
    }


    private fun createOnWindowManager() {
        touchManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManagerLayoutParams = WindowManager.LayoutParams()
        windowManagerLayoutParams.gravity = Gravity.RIGHT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            windowManagerLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            windowManagerLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST
        }
        windowManagerLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        windowManagerLayoutParams.format = PixelFormat.RGBA_8888
        windowManagerLayoutParams.y = -200
        windowManagerLayoutParams.width = 30
        windowManagerLayoutParams.height = 230
        windowContentView = LayoutInflater.from(applicationContext).inflate(R.layout.layout_window, null)
        windowContentView.window_layout.setOnClickListener {
            //            if (windowClick != null) {
            LogUtils.e("WindowToast",this@SuspensionWindowService)
            LogUtils.e("WindowToast",windowClick)
            windowClick?.openView()
//            }
        }
        touchManager.addView(windowContentView, windowManagerLayoutParams)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return SuspensionWindowService().WindowBinder()
    }

    override fun onDestroy() {
        removeView()
        SPUtils.getInstance().put(IS_WINDOW, false)
        super.onDestroy()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    internal inner class WindowBinder : Binder() {
        val getService: SuspensionWindowService
            get() = this@SuspensionWindowService
    }
}