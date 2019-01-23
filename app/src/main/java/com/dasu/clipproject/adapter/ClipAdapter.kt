package com.dasu.clipproject.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.dasu.clipproject.R
import com.dasu.clipproject.bean.ClipBean

class ClipAdapter(layoutResId: Int, data: ArrayList<ClipBean.ClipItemData>?) : BaseQuickAdapter<ClipBean.ClipItemData, BaseViewHolder>(layoutResId, data) {
    override fun convert(helper: BaseViewHolder?, item: ClipBean.ClipItemData?) {
        helper?.setText(R.id.clip_content, item!!.content)
    }
}