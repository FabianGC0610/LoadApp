package com.udacity.main

import android.app.Application
import android.app.NotificationManager
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.udacity.R
import com.udacity.customview.ButtonState
import com.udacity.model.TransferData
import com.udacity.utils.cancelNotifications
import com.udacity.utils.sendNotification

class MainActivityViewModel(private val app: Application) : ViewModel() {

    private val _isThereSomethingToDownload = MutableLiveData<Boolean>()
    val isThereSomethingToDownload: LiveData<Boolean> get() = _isThereSomethingToDownload

    private val _radioSelectionState = MutableLiveData<FieldState>()
    val radioSelectionState: LiveData<FieldState> get() = _radioSelectionState

    private val _inputOptionState = MutableLiveData<FieldState>()
    val inputOptionState: LiveData<FieldState> get() = _inputOptionState

    private val _userSelection = MutableLiveData<UserSelection>()
    val userSelection: LiveData<UserSelection> get() = _userSelection

    private val _radioUrl = MutableLiveData<Pair<String, String>>()
    val radioUrl: LiveData<Pair<String, String>> get() = _radioUrl

    private val _inputUrl = MutableLiveData<String>()
    val inputUrl: LiveData<String> get() = _inputUrl

    private val _downloadState = MutableLiveData<ButtonState>()
    val downloadState: LiveData<ButtonState> get() = _downloadState

    private val _downloadingStarted = MutableLiveData<Boolean>()
    val downloadingStarted: LiveData<Boolean> get() = _downloadingStarted

    private val notificationManager = ContextCompat.getSystemService(
        app,
        NotificationManager::class.java,
    ) as NotificationManager

    fun setRadioUrl(url: Pair<String, String>) {
        _radioUrl.value = url
    }

    fun setInputUrl(url: String) {
        _inputUrl.value = url
    }
    fun setDownLoadState(state: ButtonState) {
        _downloadState.value = state
    }

    fun checkForm(userSelection: UserSelection) {
        _isThereSomethingToDownload.value = when (userSelection) {
            UserSelection.RadioSelection -> validateRadio(_radioUrl.value?.second)
            UserSelection.Input -> validateInput(_inputUrl.value) == FieldState.Correct
        }
    }

    fun checkOptionAvailable(input: View): UserSelection {
        return if (input.isVisible) {
            _userSelection.value = UserSelection.Input
            UserSelection.Input
        } else {
            _userSelection.value = UserSelection.RadioSelection
            UserSelection.RadioSelection
        }
    }

    private fun validateRadio(selection: String?): Boolean {
        val result = !selection.isNullOrEmpty()
        _radioSelectionState.value = if (result) {
            FieldState.Correct
        } else {
            FieldState.Empty
        }
        return !selection.isNullOrEmpty()
    }

    private fun validateInput(input: String?): FieldState {
        return if (input.isNullOrEmpty()) {
            _inputOptionState.value = FieldState.Empty
            FieldState.Empty
        } else if (isUrlValid(input)) {
            _inputOptionState.value = FieldState.Correct
            FieldState.Correct
        } else {
            _inputOptionState.value = FieldState.InCorrect
            FieldState.InCorrect
        }
    }

    private fun isUrlValid(url: String): Boolean {
        val urlRegex = """^(https?|ftp):\/\/[^\s/$.?#].[^\s]*$""".toRegex()
        return url.matches(urlRegex)
    }

    fun sendNotification(message: String, data: TransferData) {
        var description = ""
        if (_downloadState.value == ButtonState.Failed) {
            description = app.getString(R.string.failed_notification_message)
        } else {
            when (_userSelection.value) {
                UserSelection.RadioSelection -> {
                    description = message
                }
                UserSelection.Input -> {
                    description = app.getString(R.string.custom_url_success_notification_message)
                }
                else -> {}
            }
        }
        notificationManager.sendNotification(description, app, data)
    }

    fun cancelNotifications() {
        notificationManager.cancelNotifications()
    }

    fun onDownloadingStarted() {
        _downloadingStarted.value = true
    }

    fun onDownloadingStartedComplete() {
        _downloadingStarted.value = false
    }

    sealed class FieldState {
        object Correct : FieldState()
        object InCorrect : FieldState()
        object Empty : FieldState()
    }
}

sealed class UserSelection {
    object Input : UserSelection()
    object RadioSelection : UserSelection()
}
