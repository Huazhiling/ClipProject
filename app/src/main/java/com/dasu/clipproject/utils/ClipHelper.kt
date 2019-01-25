package com.dasu.clipproject.utils

import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.Context
import com.blankj.utilcode.util.SPUtils
import com.dasu.clipproject.R
import com.dasu.clipproject.adapter.ClipAdapter
import com.dasu.clipproject.bean.ClipBean
import com.dasu.clipproject.common.Constans
import com.dasu.clipproject.listener.IClipManagerListener
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject

@SuppressLint("StaticFieldLeak")
object ClipHelper {
    private var previousClip = ""
    private var clip = SPUtils.getInstance().getString(Constans.CLIP_TEXT)
    private var isAppClip = false
    private var clipJson = JsonObject()
    private var clipArray = JsonArray()
    private lateinit var applicationContext: Context
    private lateinit var clipManager: ClipboardManager
    private lateinit var data: ArrayList<ClipBean.ClipItemData>
    private lateinit var clipAdapter: ClipAdapter
    private lateinit var clipCallBack: IClipManagerListener

    fun init(clipCallBack: IClipManagerListener,clipManager: ClipboardManager, applicationContext: Context): ClipHelper {
        this.clipCallBack = clipCallBack
        this.clipManager = clipManager
        this.applicationContext = applicationContext
        initData()
        initializationClip()
        return this
    }

    fun setIsAppClip(isAppClip: Boolean) {
        this.isAppClip = isAppClip
    }

    private fun initData() {
        data = java.util.ArrayList()
        clipAdapter = ClipAdapter(R.layout.item_clip_layout, data)
        if (clip == "" || clip == "{}") {
            SPUtils.getInstance().put(Constans.CLIP_TEXT, clipJson.toString())
        } else {
            var gson = Gson()
            var clipClass = gson.fromJson(clip, ClipBean::class.java)
            for (clipItemData in clipClass.clipList) {
                var element = JsonObject()
                element.addProperty("content", clipItemData.content)
                element.addProperty("isWhetherToCollect", false)
                data.add(clipItemData)
                clipArray.add(element)
                clipCallBack.addClipItem(clipItemData)
                previousClip = clipItemData.content
            }
            clipJson.add("clipList", clipArray)
        }
    }

    private fun initializationClip() {
        clipManager.addPrimaryClipChangedListener {
            var primary = clipManager.primaryClip.getItemAt(0).text
            if (primary != null && primary != "") {
                var clipBean = ClipBean.ClipItemData(primary.toString(), false)
                if (primary.toString() != previousClip && !isAppClip) {
                    addClipPrimary(clipBean.content, false)
                    previousClip = clipBean.content
                    clipAdapter.notifyDataSetChanged()
                    clipCallBack.addClipItem(clipBean)
                    clipCallBack.scrollToPosition()
                }
                isAppClip = false
            }
        }
    }

    /**
     * 添加新的剪贴板记录
     */
    private fun addClipPrimary(content: String, collect: Boolean) {
        var element = JsonObject()
        element.addProperty("content", content)
        element.addProperty("isWhetherToCollect", collect)
        clipArray.add(element)
        clipJson.add("clipList", clipArray)
        SPUtils.getInstance().put(Constans.CLIP_TEXT, clipJson.toString())
    }
}
