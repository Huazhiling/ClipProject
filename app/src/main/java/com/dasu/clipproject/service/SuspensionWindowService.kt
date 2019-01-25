package com.dasu.clipproject.service

import android.app.Service
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.view.*
import com.blankj.utilcode.util.SPUtils
import com.dasu.clipproject.R
import com.dasu.clipproject.adapter.ClipAdapter
import com.dasu.clipproject.bean.ClipBean
import com.dasu.clipproject.common.Constans.CLIP_TEXT
import com.dasu.clipproject.common.Constans.IS_WINDOW
import com.dasu.clipproject.listener.IClipManagerListener
import com.dasu.clipproject.listener.IWindowHelperListener
import com.dasu.clipproject.listener.IWindowOnClickListener
import com.dasu.clipproject.utils.ClipHelper
import com.dasu.clipproject.view.WindowHelper
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.layout_clip_list.view.*

class SuspensionWindowService : Service() {
    private lateinit var clipView: View
    private var windowBinder = WindowBinder()
    private lateinit var windowContentView: View
    private lateinit var clipAdapter: ClipAdapter
    private lateinit var touchManager: WindowManager
    private lateinit var clipManager: ClipboardManager
    private var windowClick: IWindowOnClickListener? = null
    private var data = ArrayList<ClipBean.ClipItemData>()
    private lateinit var windowHelper: WindowHelper
    private lateinit var clipHelper: ClipHelper
    private var isClipManager = false
    private var previousClip = ""
    private var isAppClip = false
    override fun onCreate() {
        super.onCreate()
        createOnWindowManager()
        createClipListener()
    }

    /**
     * 创建监听剪贴板的代码
     */
    private fun createClipListener() {
        clipAdapter = ClipAdapter(R.layout.item_clip_layout, data)
        clipManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipHelper = ClipHelper.init(
                object : IClipManagerListener {
                    override fun scrollToPosition() {
                        windowHelper.scrollToPosition()
                    }

                    override fun addClipItem(clipBean: ClipBean.ClipItemData) {
                        data.add(clipBean)
                        clipAdapter.notifyDataSetChanged()
                    }
                }, clipManager, applicationContext)
    }

    fun setWindowOnClickListener(windowClick: IWindowOnClickListener) {
        this.windowClick = windowClick
    }

    private fun createOnWindowManager() {
        touchManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowHelper = WindowHelper.init(touchManager, applicationContext)
        windowHelper.setWindowHelper(object : IWindowHelperListener {
            override fun setAdapter(fullScreenView: View) {
                clipAdapter.setOnItemChildClickListener { adapter, view, position ->
                    when (view.id) {
                        R.id.item_layout -> {
                            windowHelper.removeUpdateView()
                            windowHelper.createSideWindowManager()
                        }
                        R.id.clip_content -> {
                            clipHelper.setIsAppClip(true)
                            var clipData = ClipData.newPlainText("", data[position].content)
                            clipManager.primaryClip = clipData
                        }
                    }

                }
                fullScreenView.setOnClickListener {
                    WindowHelper.removeUpdateView()
                    createClipWindowView()
                }
                fullScreenView.setOnKeyListener { v, keyCode, event ->
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        WindowHelper.removeUpdateView()
                        createClipWindowView()
                        true
                    } else {
                        false
                    }
                }
                fullScreenView.clip_list.adapter = clipAdapter
                fullScreenView.clip_cleanAll.setOnClickListener { }
                fullScreenView.clip_search.setOnClickListener { }
                fullScreenView.clip_collection.setOnClickListener { }
            }

            override fun startSettingActivity() {
                windowClick?.startSettingActivity()
            }

            override fun scrollToPosition(view: View) {
                view.clip_list.scrollToPosition(data.size - 1)
            }
        })
        createClipWindowView()
    }

    /**
     * 创建初始的闪贴window
     */
    private fun createClipWindowView() {
        isClipManager = false
        windowHelper.createSideWindowManager()
    }

    /**
     * 创建侧边栏滑动后的window
     */
    fun createFullScreenWindowManager() {
        isClipManager = true
        windowHelper.createFullScreenWindowManager()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return windowBinder
    }

    fun getIsClipManager(): Boolean {
        return isClipManager
    }

    override fun onDestroy() {
        isClipManager = false
        windowHelper.removeWindowView()
        SPUtils.getInstance().put(IS_WINDOW, false)
        super.onDestroy()
    }

    internal inner class WindowBinder : Binder() {
        val getService: SuspensionWindowService
            get() = this@SuspensionWindowService

        fun updateClipWindowView() {
            windowHelper.removeUpdateView()
            createClipWindowView()
        }

        fun updateClipManagerView() {
            windowHelper.removeWindowView()
            createFullScreenWindowManager()
        }
    }

}