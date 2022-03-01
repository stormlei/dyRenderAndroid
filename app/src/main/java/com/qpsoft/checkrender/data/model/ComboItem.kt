package com.qpsoft.checkrender.data.model

data class ComboItem (
    val category: String,
    val name: String,
    val doubleEye: Boolean,
    val fileModule: Boolean,
    val resultModule: Boolean,
    val remarkModule: Boolean,
    val items: MutableList<CheckItem>,
)
