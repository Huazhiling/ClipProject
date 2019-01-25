package com.dasu.clipproject.view

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.*
import com.blankj.utilcode.util.ConvertUtils
import com.dasu.clipproject.R
import com.dasu.clipproject.common.Constans.SCREEN_VIEW
import com.dasu.clipproject.common.Constans.SIDE_VIEW
import com.dasu.clipproject.listener.IWindowHelperListener
import kotlinx.android.synthetic.main.layout_clip_list.view.*
import kotlinx.android.synthetic.main.layout_window.view.*
import java.util.HashMap

@SuppressLint("StaticFieldLeak")
object WindowHelper {
    private lateinit var windowManager: WindowManager
    private var windowHelper: IWindowHelperListener? = null
    private lateinit var windowManagerLayoutParams: WindowManager.LayoutParams
    private lateinit var clipWindowManagerLayoutParams: WindowManager.LayoutParams
    private lateinit var sideView: View
    private lateinit var fullScreenView: View
    private lateinit var applicationContext: Context
    private var windowViewMap = HashMap<String, View>()

    fun init(windowManager: WindowManager, applicationContext: Context): WindowHelper {
        this.windowManager = windowManager
        this.applicationContext = applicationContext
        return this
    }

    fun setWindowHelper(windowHelper: IWindowHelperListener) {
        this.windowHelper = windowHelper
    }

    /**
     * 创建侧边的windowManager
     */
    fun createSideWindowManager() {
        if (windowViewMap[SIDE_VIEW] == null) {
            windowManagerLayoutParams = WindowManager.LayoutParams()
            windowManagerLayoutParams.gravity = Gravity.RIGHT or Gravity.TOP
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                windowManagerLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                windowManagerLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST
            }
            windowManagerLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            windowManagerLayoutParams.format = PixelFormat.RGBA_8888
            windowManagerLayoutParams.y = 0
            windowManagerLayoutParams.width = ConvertUtils.dp2px(15f)
            windowManagerLayoutParams.height = ConvertUtils.dp2px(150f)
            windowManagerLayoutParams.windowAnimations = android.R.style.Animation_Translucent
            sideView = LayoutInflater.from(applicationContext).inflate(R.layout.layout_window, null)
            sideView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            sideView.window_layout.setOnClickListener {
                removeWindowView()
                createFullScreenWindowManager()
            }
            windowViewMap[SIDE_VIEW] = sideView
        }
        windowManager.addView(sideView, windowManagerLayoutParams)
    }

    /**
     * 创建全屏的windowManager记录
     */
    fun createFullScreenWindowManager() {
        if (windowViewMap[SCREEN_VIEW] == null) {
            fullScreenView = LayoutInflater.from(applicationContext).inflate(R.layout.layout_clip_list, null)
            clipWindowManagerLayoutParams = WindowManager.LayoutParams()
            clipWindowManagerLayoutParams.gravity = Gravity.RIGHT or Gravity.TOP
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                clipWindowManagerLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                clipWindowManagerLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST
            }
            clipWindowManagerLayoutParams.flags = WindowManager.LayoutParams.FLAG_BLUR_BEHIND or WindowManager.LayoutParams.FLAG_FULLSCREEN
            clipWindowManagerLayoutParams.format = PixelFormat.RGBA_8888
            clipWindowManagerLayoutParams.y = 0
            clipWindowManagerLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            clipWindowManagerLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            clipWindowManagerLayoutParams.windowAnimations = android.R.style.Animation_Translucent
            var windowContentView = LayoutInflater.from(applicationContext).inflate(R.layout.layout_window, null)
            windowContentView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            fullScreenView.clip_setting.setOnClickListener {
                removeUpdateView()
                createSideWindowManager()
                windowHelper!!.startSettingActivity()
            }
            windowHelper!!.scrollToPosition(fullScreenView)
            windowHelper!!.setAdapter(fullScreenView)
            windowViewMap[SCREEN_VIEW] = fullScreenView
        }
        windowManager.addView(fullScreenView, clipWindowManagerLayoutParams)
    }

    fun removeUpdateView() {
        if (windowViewMap[SCREEN_VIEW] !== null) {
            windowManager.removeView(fullScreenView)
        }
    }

    fun removeWindowView() {
        if (windowViewMap[SIDE_VIEW] !== null) {
            windowManager.removeView(sideView)
        }
    }
    fun scrollToPosition(){
        if (windowViewMap[SCREEN_VIEW] !== null) {
            windowHelper!!.scrollToPosition(fullScreenView)
        }
    }
}