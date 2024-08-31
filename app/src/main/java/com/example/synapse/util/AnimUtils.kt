package com.example.synapse.util

import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import com.example.synapse.R

fun View.slideDown(animTime : Long, startOffset : Long){
    val slideDown = AnimationUtils.loadAnimation(context, R.anim.slide_down_anim).apply {
        duration = animTime
        interpolator = LinearInterpolator()
        this.startOffset = startOffset
    }
    startAnimation(slideDown)
}