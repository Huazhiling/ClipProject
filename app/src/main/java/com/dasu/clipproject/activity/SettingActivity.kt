package com.dasu.clipproject.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.dasu.clipproject.R
import com.gyf.barlibrary.ImmersionBar
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        toolbar.setNavigationOnClickListener { finish() }
        initView()
    }

    private fun initView() {
        ImmersionBar.with(this).statusBarView(R.id.status_bar).statusBarDarkFont(true).init()
    }
}