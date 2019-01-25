package com.dasu.clipproject.listener

import android.view.View
import com.dasu.clipproject.bean.ClipBean

interface IClipManagerListener {
    fun addClipItem(clipBean: ClipBean.ClipItemData)
    fun scrollToPosition()
}