package com.qpsoft.checkrender.data.network

import com.qpsoft.checkrender.data.model.CheckItem
import com.qpsoft.checkrender.data.model.ComboItem
import com.qpsoft.checkrender.data.model.LzyResponse
import com.qpsoft.checkrender.data.model.SimpleError
import com.qpsoft.checkrender.data.network.adapter.NetworkResponse
import retrofit2.http.*

interface ApiService {
    @GET("clinic/api/public/init/comboitem")
    suspend fun comboItemList(
        @Query("item") item: String?,
    ): NetworkResponse<LzyResponse<MutableList<ComboItem>>, SimpleError>

}