package com.dasu.clipproject.base

interface IBaseView {
    fun initPresenter(): BasePresenter<*, *>
}