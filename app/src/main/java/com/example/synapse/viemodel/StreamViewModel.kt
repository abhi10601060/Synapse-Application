package com.example.synapse.viemodel

import com.example.synapse.repo.StreamRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StreamViewModel @Inject constructor(
    private val streamRepo: StreamRepo
) {

}