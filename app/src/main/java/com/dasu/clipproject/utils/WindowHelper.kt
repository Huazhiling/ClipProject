package com.dasu.clipproject.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.view.*
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.SPUtils
import com.dasu.clipproject.R
import com.dasu.clipproject.common.Constans.CLEAR_VIEW
import com.dasu.clipproject.common.Constans.CLIP_TEXT
import com.dasu.clipproject.common.Constans.TOAST_VIEW
import com.dasu.clipproject.common.Constans.SCREEN_VIEW
import com.dasu.clipproject.common.Constans.SIDE_VIEW
import com.dasu.clipproject.listener.IWindowHelperListener
import kotlinx.android.synthetic.main.layout_clear_all.view.*
import kotlinx.android.synthetic.main.layout_clip_list.view.*
import kotlinx.android.synthetic.main.layout_toast.view.*
import kotlinx.android.synthetic.main.layout_window.view.*
import java.util.HashMap

@SuppressLint("StaticFieldLeak")
object WindowHelper {
    private lateinit var windowManager: WindowManager
    private var windowHelper: IWindowHelperListener? = null
    private lateinit var windowManagerLayoutParams: WindowManager.LayoutParams
    private lateinit var clipWindowManagerLayoutParams: WindowManager.LayoutParams
    private lateinit var toastLayoutParams: WindowManager.LayoutParams
    private lateinit var dialogLayoutParams: WindowManager.LayoutParams
    private lateinit var sideView: View
    private lateinit var fullScreenView: View
    private lateinit var toastView: View
    private lateinit var dialogView: View
    private lateinit var applicationContext: Context
    private var windowViewMap = HashMap<String, View>()

    fun init(windowManager: WindowManager, applicationContext: Context): WindowHelper {
        WindowHelper.windowManager = windowManager
        WindowHelper.applicationContext = applicationContext
        return this
    }

    fun setWindowHelper(windowHelper: IWindowHelperListener) {
        WindowHelper.windowHelper = windowHelper
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
            fullScreenView.clip_layout.setOnClickListener {
                removeUpdateView()
                createSideWindowManager()
            }
            fullScreenView.clip_search_list.setOnClickListener {
                removeUpdateView()
                createSideWindowManager()
            }

            windowHelper!!.scrollToPosition(fullScreenView)
            windowHelper!!.setAdapter(fullScreenView)
            windowViewMap[SCREEN_VIEW] = fullScreenView
        }
        windowManager.addView(fullScreenView, clipWindowManagerLayoutParams)
    }

    fun showDialog(msg: String) {
        if (windowViewMap[TOAST_VIEW] == null) {
            toastView = LayoutInflater.from(applicationContext).inflate(R.layout.layout_toast, null)
            toastView.dialog_content.text = msg
            toastLayoutParams = WindowManager.LayoutParams()
            toastLayoutParams.gravity = Gravity.CENTER
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                toastLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                toastLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST
            }
            toastLayoutParams.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN
            toastLayoutParams.format = PixelFormat.RGBA_8888
            toastLayoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            toastLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            toastLayoutParams.windowAnimations = android.R.style.Animation_Toast
            windowViewMap[TOAST_VIEW] = toastView
        }
        windowManager.addView(toastView, toastLayoutParams)
        Handler().postDelayed({
            removeToastView()
        }, 1000)
    }

    fun createClearAllView() {
        if (windowViewMap[CLEAR_VIEW] == null) {
            dialogView = LayoutInflater.from(applicationContext).inflate(R.layout.layout_clear_all, null)
            dialogLayoutParams = WindowManager.LayoutParams()
            dialogLayoutParams.gravity = Gravity.CENTER
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                dialogLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                dialogLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST
            }
            dialogLayoutParams.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN
            dialogLayoutParams.format = PixelFormat.RGBA_8888
            dialogLayoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            dialogLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialogLayoutParams.windowAnimations = android.R.style.Animation_Toast
            windowViewMap[CLEAR_VIEW] = dialogView
            dialogView.cancle.setOnClickListener {
                removeDialogView()
            }
            dialogView.enter.setOnClickListener {
                removeDialogView()
                SPUtils.getInstance().put(CLIP_TEXT, "")
                windowHelper?.clearAllItem()
            }
        }
        windowManager.addView(dialogView, dialogLayoutParams)
    }

    fun removeToastView() {
        if (windowViewMap[TOAST_VIEW] !== null) {
            windowManager.removeView(toastView)
        }
    }

    fun removeDialogView() {
        if (windowViewMap[CLEAR_VIEW] !== null) {
            windowManager.removeView(dialogView)
        }
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

    fun scrollToPosition() {
        if (windowViewMap[SCREEN_VIEW] !== null) {
            windowHelper!!.scrollToPosition(fullScreenView)
        }
    }
}