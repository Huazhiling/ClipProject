package com.dasu.clipproject.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.dasu.clipproject.R
import com.gyf.barlibrary.ImmersionBar

class ClipboardRecordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_clip_list)
        initView()
        initData()
    }

    private fun initData() {

    }

    private fun initView() {
        ImmersionBar.with(this).titleBar(R.id.status_bar).statusBarDarkFont(true).init()
    }
}