package com.qpsoft.checkrender.repository


import com.qpsoft.checkrender.data.network.ApiService
import javax.inject.Inject


class MainRepository @Inject constructor(val apiService: ApiService) {

    suspend fun comboItemList(item: String?) = apiService.comboItemList(item)

}
