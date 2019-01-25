package com.dasu.clipproject.service

import android.app.Service
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.view.*
import android.view.animation.AnimationUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SPUtils
import com.dasu.clipproject.R
import com.dasu.clipproject.adapter.ClipAdapter
import com.dasu.clipproject.bean.ClipBean
import com.dasu.clipproject.common.Constans.IS_WINDOW
import com.dasu.clipproject.listener.IClipManagerListener
import com.dasu.clipproject.listener.IWindowHelperListener
import com.dasu.clipproject.listener.IWindowOnClickListener
import com.dasu.clipproject.utils.ClipHelper
import com.dasu.clipproject.utils.WindowHelper
import com.dasu.clipproject.utils.WindowHelper.showDialog
import kotlinx.android.synthetic.main.layout_clip_list.view.*

class SuspensionWindowService : Service() {
    private var windowBinder = WindowBinder()
    private lateinit var clipAdapter: ClipAdapter
    private lateinit var clipSearchAdapter: ClipAdapter
    private lateinit var touchManager: WindowManager
    private lateinit var clipManager: ClipboardManager
    private var windowClick: IWindowOnClickListener? = null
    private var data = ArrayList<ClipBean.ClipItemData>()
    private var searchData = ArrayList<ClipBean.ClipItemData>()
    private lateinit var windowHelper: WindowHelper
    private lateinit var clipHelper: ClipHelper
    private var isClipManager = false
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
        clipSearchAdapter = ClipAdapter(R.layout.item_clip_layout, searchData)
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
            override fun clearAllItem() {
                data.clear()
                clipAdapter.notifyDataSetChanged()
                windowHelper.showDialog("剪贴板清除成功")
            }

            override fun setAdapter(fullScreenView: View) {
                clipAdapter.setOnItemChildClickListener { adapter, view, position ->
                    when (view.id) {
                        R.id.item_layout -> {
                            windowHelper.removeUpdateView()
                            createClipSideWindowView()
                        }
                        R.id.clip_content -> {
                            clipHelper.setIsAppClip(true)
                            var clipData = ClipData.newPlainText("", data[position].content)
                            clipManager.primaryClip = clipData
                            windowHelper.showDialog("文本已复制到剪贴板")
                        }
                    }

                }
                fullScreenView.setOnClickListener {
                    windowHelper.removeUpdateView()
                    createClipSideWindowView()
                }
                fullScreenView.setOnKeyListener { v, keyCode, event ->
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        windowHelper.removeUpdateView()
                        createClipSideWindowView()
                        true
                    } else {
                        false
                    }
                }

                fullScreenView.clip_list.adapter = clipAdapter
                fullScreenView.clip_cleanAll.setOnClickListener {
                    if (data.size > 0) {
                        windowHelper.createClearAllView()
                    } else {
                        windowHelper.showDialog("当前没有记录")
                    }
                }
            }

            override fun startSettingActivity() {
                windowClick?.startSettingActivity()
            }

            override fun scrollToPosition(view: View) {
                view.clip_list.scrollToPosition(data.size - 1)
            }
        })
        createClipSideWindowView()
    }

    /**
     * 创建初始的闪贴window
     */
    private fun createClipSideWindowView() {
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
            createClipSideWindowView()
        }

        fun updateClipManagerView() {
            windowHelper.removeWindowView()
            createFullScreenWindowManager()
        }
    }

}