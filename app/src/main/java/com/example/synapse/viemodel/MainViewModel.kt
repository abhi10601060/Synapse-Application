package com.example.synapse.viemodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.synapse.model.res.AllActiveStreamOutput
import com.example.synapse.model.res.SubscriptionsOutput
import com.example.synapse.network.Resource
import com.example.synapse.repo.MainRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val mainRepo: MainRepo
) : ViewModel() {

    //*******************************************************  Home Page States **************************************************

    val isSearching = MutableStateFlow(false)


    val allActiveStreams : StateFlow<Resource<AllActiveStreamOutput>>
        get() = mainRepo.activeStreams


    fun getAllActiveStreams(){
        viewModelScope.launch(Dispatchers.IO) {
            mainRepo.getAllActiveStreams()
        }
    }



    //******************************************************* Subscription Page **************************************************

    val subscriptions : StateFlow<Resource<SubscriptionsOutput>>
        get() = mainRepo.subscriptions

    val subscriptionStreams : StateFlow<Resource<AllActiveStreamOutput>>
        get() = mainRepo.subscriptionStreams

    fun getAllSubscriptions(){
        viewModelScope.launch(Dispatchers.IO) {
            mainRepo.getAllSubscriptions()
        }
    }

    fun getAllSubscriptionsStreams(){
        viewModelScope.launch(Dispatchers.IO) {
            mainRepo.getAllSubscriptionStreams()
        }
    }
}