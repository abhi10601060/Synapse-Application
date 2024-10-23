package com.example.synapse.viemodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.synapse.model.res.AllActiveStreamOutput
import com.example.synapse.model.res.SearchStreamsOutput
import com.example.synapse.model.res.SearchUserOutput
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

    val searchStreamsResult : StateFlow<Resource<SearchStreamsOutput>>
        get() = mainRepo.searchedStreams

    val searchUsersResult : StateFlow<Resource<SearchUserOutput>>
        get() = mainRepo.searchedUsers

    val allActiveStreams : StateFlow<Resource<AllActiveStreamOutput>>
        get() = mainRepo.activeStreams


    fun getAllActiveStreams(){
        viewModelScope.launch(Dispatchers.IO) {
            mainRepo.getAllActiveStreams()
        }
    }

    fun searchRequest(searchParam : String){
        viewModelScope.launch(Dispatchers.IO) {
            mainRepo.searchStreams(searchParam)
        }
        viewModelScope.launch(Dispatchers.IO) {
            mainRepo.searchPeoples(searchParam)
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