package com.qpsoft.checkrender.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qpsoft.checkrender.data.network.adapter.NetworkResponse
import com.qpsoft.checkrender.data.model.*
import com.qpsoft.checkrender.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(val mainRepo: MainRepository) : ViewModel() {

    var comboItemListLiveData = MutableLiveData<MutableList<ComboItem>>()

    fun comboItemList(item: String? = "") {
        viewModelScope.launch {
            when(val response = mainRepo.comboItemList(item)) {
                is NetworkResponse.Success -> {
                    val comboItemList = response.body.data
                    comboItemListLiveData.value = comboItemList
                }
            }
        }
    }
}