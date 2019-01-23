package com.dasu.clipproject.bean

data class ClipBean(
        var clipList: MutableList<ClipItemData>
) {
    data class ClipItemData(
            var content: String,
            var isWhetherToCollect: Boolean
    )
}
