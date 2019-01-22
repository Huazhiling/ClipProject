package com.dasu.clipproject.base

import android.support.annotation.Nullable

abstract class BasePresenter<M, V> {
    protected abstract var mIModel: M?
    protected abstract var mIView: V?

    public fun attchMVP(v: V) {
        this.mIModel = getModel()
        this.mIView = v
    }

    public fun dettchMVP() {
        this.mIModel = null
        this.mIView = null
    }

    abstract fun getModel(): M

}