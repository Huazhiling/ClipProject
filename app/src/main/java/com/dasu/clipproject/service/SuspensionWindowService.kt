package com.dasu.clipproject.service

import android.app.Service
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.view.*
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SPUtils
import com.dasu.clipproject.R
import com.dasu.clipproject.adapter.ClipAdapter
import com.dasu.clipproject.bean.ClipBean
import com.dasu.clipproject.common.Constans.CLIP_TEXT
import com.dasu.clipproject.common.Constans.IS_WINDOW
import com.dasu.clipproject.listener.IWindowOnClickListener
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.layout_clip_list.view.*
import kotlinx.android.synthetic.main.layout_window.view.*

class SuspensionWindowService : Service() {
    private lateinit var clipView: View
    private var windowBinder = WindowBinder()
    private lateinit var windowContentView: View
    private lateinit var clipAdapter: ClipAdapter
    private lateinit var touchManager: WindowManager
    private lateinit var clipManager: ClipboardManager
    private var clipJson = JsonObject()
    private var clipArray = JsonArray()
    private var windowClick: IWindowOnClickListener? = null
    private lateinit var data: ArrayList<ClipBean.ClipItemData>
    private var clip = SPUtils.getInstance().getString(CLIP_TEXT)
    private lateinit var windowManagerLayoutParams: WindowManager.LayoutParams
    private lateinit var clipWindowManagerLayoutParams: WindowManager.LayoutParams
    private var isClipManager = false
    private var fristClip = ""
    override fun onCreate() {
        super.onCreate()
        createOnWindowManager()
        createClipListener()
        createClipCollection()
    }



    private fun createClipCollection() {
        data = java.util.ArrayList()
        initClipData()
        clipAdapter = ClipAdapter(R.layout.item_clip_layout, data)
    }

    private fun initClipData() {
        if (clip == "" || clip == "{}") {
            SPUtils.getInstance().put(CLIP_TEXT, clipJson.toString())
        } else {
            var gson = Gson()
            var clipClass = gson.fromJson(clip, ClipBean::class.java)
            for (clipItemData in clipClass.clipList) {
                var element = JsonObject()
                element.addProperty("content", clipItemData.content)
                element.addProperty("isWhetherToCollect", false)
                clipArray.add(element)
                data.add(clipItemData)
            }
            clipJson.add("clipList", clipArray)
        }
    }

    /**
     * 创建监听剪贴板的代码
     */
    private fun createClipListener() {
        clipManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipManager.addPrimaryClipChangedListener {
            var primary = clipManager.primaryClip.getItemAt(0).text
            if (primary != null && primary != "") {
                var clipBean = ClipBean.ClipItemData(primary.toString(), false)
                if(primary.toString() != fristClip){
                    var element = JsonObject()
                    element.addProperty("content", clipBean.content)
                    element.addProperty("isWhetherToCollect", false)
                    clipArray.add(element)
                    clipJson.add("clipList", clipArray)
                    SPUtils.getInstance().put(CLIP_TEXT, clipJson.toString())
                    data.add(clipBean)
                    fristClip = clipBean.content
                }
                clipAdapter.notifyDataSetChanged()
                windowClick?.getDesrc(primary.toString())
            }
        }
    }

    fun setWindowOnClickListener(windowClick: IWindowOnClickListener) {
        this.windowClick = windowClick
    }

    fun removeUpdateView() {
        if (clipView !== null) {
            touchManager.removeView(clipView)
        }
    }

    fun removeWindowView() {
        if (windowContentView !== null) {
            touchManager.removeView(windowContentView)
        }
    }


    private fun createOnWindowManager() {
        touchManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createClipWindowView()
    }

    /**
     * 创建初始的闪贴popwin
     */
    private fun createClipWindowView() {
        isClipManager = false
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
        windowManagerLayoutParams.width = ConvertUtils.dp2px(10f)
        windowManagerLayoutParams.height = ConvertUtils.dp2px(150f)
        windowManagerLayoutParams.windowAnimations = android.R.style.Animation_Translucent
        windowContentView = LayoutInflater.from(applicationContext).inflate(R.layout.layout_window, null)
        windowContentView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        windowContentView.window_layout.setOnClickListener {
            windowClick?.openClipManagerView()
        }
        touchManager.addView(windowContentView, windowManagerLayoutParams)
    }

    fun createClipManagerView() {
        isClipManager = true
        clipView = LayoutInflater.from(applicationContext).inflate(R.layout.layout_clip_list, null)
        clipWindowManagerLayoutParams = WindowManager.LayoutParams()
        clipWindowManagerLayoutParams.gravity = Gravity.RIGHT or Gravity.TOP
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            clipWindowManagerLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            clipWindowManagerLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST
        }
        clipWindowManagerLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_BLUR_BEHIND or WindowManager.LayoutParams.FLAG_FULLSCREEN
        clipWindowManagerLayoutParams.format = PixelFormat.RGBA_8888
        clipWindowManagerLayoutParams.y = 0
        clipWindowManagerLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        clipWindowManagerLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        clipWindowManagerLayoutParams.windowAnimations = android.R.style.Animation_Translucent
        windowContentView = LayoutInflater.from(applicationContext).inflate(R.layout.layout_window, null)
        windowContentView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        clipView.clip_setting.setOnClickListener {
            removeUpdateView()
            createClipWindowView()
        }
        clipView.clip_list.adapter = clipAdapter
        clipView.clip_cleanAll.setOnClickListener { }
        clipView.clip_search.setOnClickListener { }
        clipView.clip_collection.setOnClickListener { }
        touchManager.addView(clipView, clipWindowManagerLayoutParams)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return windowBinder
    }
    fun getIsClipManager() :Boolean{
        return isClipManager
    }
    override fun onDestroy() {
        isClipManager = false
        removeWindowView()
        SPUtils.getInstance().put(IS_WINDOW, false)
        super.onDestroy()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    internal inner class WindowBinder : Binder() {
        val getService: SuspensionWindowService
            get() = this@SuspensionWindowService

        fun updateClipWindowView() {
            removeUpdateView()
            createClipWindowView()
        }

        fun updateClipManagerView() {
            removeWindowView()
            createClipManagerView()
        }
    }
}