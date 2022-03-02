package com.qpsoft.checkrender.data.model

data class CheckItem (
    val item: String,
    val name: String,
    val key: String,
    val doubleEye: Boolean,
    val type: String,
    val unit: String,
    val template: String,
    val optionList: MutableList<String>?,
    val itemList: MutableList<CheckItem>?,
    val suffix: CheckItem?
)
