package com.udacity.detail

import android.graphics.Color
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

private const val SUCCESS_STATUS = "Success"

class DetailActivityViewModel : ViewModel() {

    private val _backToMainEvent = MutableLiveData<Boolean>()
    val backToMainEvent: LiveData<Boolean> get() = _backToMainEvent

    fun toMbFormat(size: String): String {
        val sizeInMB = size.toInt().toDouble() / (1024 * 1024)
        val result = String.format("%.2f", sizeInMB)
        return "$result MB"
    }

    fun toStatusFormat(status: String, textView: TextView) {
        if (status != SUCCESS_STATUS) {
            textView.setTextColor(Color.RED)
        }
    }

    fun onBackToMainEvent() {
        _backToMainEvent.value = true
    }

    fun onBackToMainEventCompleted() {
        _backToMainEvent.value = false
    }
}
