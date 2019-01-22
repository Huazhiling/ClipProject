package com.dasu.clipproject.base

import android.os.Bundle

abstract class BaseMVPActivity<P : BasePresenter<*, *>> : BaseActivity(), IBaseView {
    private lateinit var mPresenter: P

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPresenter = initPresenter() as P
        if(mPresenter != null){
//            mPresenter.attchMVP(this)
        }
        initMVPView()
        initMVPData()
    }

    abstract fun initMVPView()

    abstract fun initMVPData()

}