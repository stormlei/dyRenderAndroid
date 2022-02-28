package com.qpsoft.checkrender.data.model

data class ComboItem (
    val name: String,
    val doubleEye: Boolean,
    val fileModule: Boolean,
    val remarkModule: Boolean,
    val items: MutableList<CheckItem>,
)
