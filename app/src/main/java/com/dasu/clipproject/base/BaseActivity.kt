package com.dasu.clipproject.base

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

open abstract class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initData()
    }

    abstract fun initView()

    abstract fun initData()
}
