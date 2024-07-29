package com.example.synapse.viemodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.synapse.model.res.AllActiveStreamOutput
import com.example.synapse.network.Resource
import com.example.synapse.repo.MainRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val mainRepo: MainRepo
) : ViewModel() {
    val allActiveStreams : StateFlow<Resource<AllActiveStreamOutput>>
        get() = mainRepo.activeStreams


    fun getAllActiveStreams(){
        viewModelScope.launch(Dispatchers.IO) {
            mainRepo.getAllActiveStreams()
        }
    }
}