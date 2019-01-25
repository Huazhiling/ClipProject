package com.dasu.clipproject.listener

import android.view.View
interface IWindowHelperListener {
    fun startSettingActivity()
    fun scrollToPosition(fullScreenView: View)
    fun setAdapter(fullScreenView: View)
    fun clearAllItem()
}