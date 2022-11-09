package com.kotak.neobanking.interfaces

import androidx.activity.result.ActivityResult

interface IOnActivityHandler {
    fun onActivtyCLosed(result: ActivityResult)
}