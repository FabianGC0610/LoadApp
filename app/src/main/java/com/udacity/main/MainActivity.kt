package com.udacity.main

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.udacity.R
import com.udacity.customview.ButtonState
import com.udacity.databinding.ActivityMainBinding
import com.udacity.model.TransferData
import com.udacity.utils.ToastUtils.showToast

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var viewModel: MainActivityViewModel
    private lateinit var viewModelFactory: MainActivityViewModelFactory

    private var downloadID: Long = 0

    private var url: String? = null

    @RequiresApi(33)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        viewModelFactory = MainActivityViewModelFactory(this.application)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainActivityViewModel::class.java]

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        createChannel(
            getString(R.string.notification_channel_id),
            getString(R.string.notification_channel_name),
        )

        requestNotificationPermission()

        startFlashingAnimation(binding.contentMain.swipeUpHelper)
        startFlashingAnimation(binding.contentMain.swipeDownHelper)

        binding.contentMain.customButton.setOnClickListener {
            viewModel.onDownloadingStarted()
        }

        binding.contentMain.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioButton1 -> {
                    viewModel.setRadioUrl(Pair(URLs.URL_1.repositoryName, URLs.URL_1.url))
                }

                R.id.radioButton2 -> {
                    viewModel.setRadioUrl(Pair(URLs.URL_2.repositoryName, URLs.URL_2.url))
                }

                R.id.radioButton3 -> {
                    viewModel.setRadioUrl(Pair(URLs.URL_3.repositoryName, URLs.URL_3.url))
                }
            }
        }

        viewModel.downloadingStarted.observe(this) { downloadingStarted ->
            if (downloadingStarted) {
                viewModel.setInputUrl(binding.contentMain.urlInputLayout.editText?.text.toString())
                viewModel.checkForm(viewModel.checkOptionAvailable(binding.contentMain.urlInputLayout))
            }
        }

        viewModel.isThereSomethingToDownload.observe(this) { userCanDownload ->
            if (userCanDownload) {
                binding.contentMain.customButton.buttonState = ButtonState.Clicked
                viewModel.cancelNotifications()
                download()
                viewModel.onDownloadingStartedComplete()
            }
        }

        viewModel.userSelection.observe(this) { userSelection ->
            url = when (userSelection) {
                UserSelection.RadioSelection -> {
                    viewModel.radioUrl.value?.second
                }

                UserSelection.Input -> {
                    viewModel.inputUrl.value
                }
            }
        }

        viewModel.radioSelectionState.observe(this) { radioState ->
            if (radioState != MainActivityViewModel.FieldState.Correct) {
                showToast(this, getString(R.string.no_option_selected_message))
            }
        }

        viewModel.inputOptionState.observe(this) { inputState ->
            when (inputState) {
                MainActivityViewModel.FieldState.Correct -> {
                    showToast(this, getString(R.string.correct_url_toast_message))
                }
                MainActivityViewModel.FieldState.InCorrect -> {
                    showToast(this, getString(R.string.incorrect_url_toast_message))
                }
                MainActivityViewModel.FieldState.Empty -> {
                    showToast(this, getString(R.string.empty_url_toast_message))
                }
            }
        }
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("Range")
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            if (id == downloadID) {
                val query = DownloadManager.Query().setFilterById(downloadID)
                val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                val cursor = downloadManager.query(query)

                if (cursor.moveToFirst()) {
                    val size = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    val type = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE))
                    val uri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_URI))
                    val title = if (viewModel.userSelection.value == UserSelection.RadioSelection) {
                        viewModel.radioUrl.value?.first
                    } else {
                        Uri.parse(uri).lastPathSegment
                    }
                    requestNotificationPermission()
                    when (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            viewModel.setDownLoadState(ButtonState.Completed)
                            binding.contentMain.customButton.buttonState = ButtonState.Completed
                            viewModel.sendNotification(
                                title!!,
                                TransferData(
                                    title,
                                    getString(R.string.success_status),
                                    size,
                                    type,
                                    uri,
                                ),
                            )
                        }

                        DownloadManager.STATUS_FAILED -> {
                            viewModel.setDownLoadState(ButtonState.Failed)
                            binding.contentMain.customButton.buttonState = ButtonState.Failed
                            viewModel.sendNotification(
                                title!!,
                                TransferData(
                                    title,
                                    getString(R.string.failed_status),
                                    size,
                                    type,
                                    uri,
                                ),
                            )
                        }
                    }
                }

                cursor.close()
            }
        }
    }

    private fun download() {
        viewModel.setDownLoadState(ButtonState.Loading)
        val request =
            DownloadManager.Request(Uri.parse(url))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request) // enqueue puts the download request in the queue.
    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW,
            )

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = "Load App Download"

            val notificationManager = this.getSystemService(
                NotificationManager::class.java,
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun startFlashingAnimation(view: View) {
        val animator = ObjectAnimator.ofFloat(view, View.ALPHA, 0F)
        animator.duration = 500
        animator.repeatCount = ObjectAnimator.INFINITE
        animator.repeatMode = ObjectAnimator.REVERSE
        animator.start()
    }

    private fun requestNotificationPermission() {
        if (!viewModel.notificationManager.areNotificationsEnabled()) {
            Snackbar.make(
                findViewById<View>(android.R.id.content).rootView,
                "Please grant Notification permission from App Settings",
                Snackbar.LENGTH_LONG,
            ).setAction("Open Settings") {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }.show()
        }
    }
}

enum class URLs(val repositoryName: String, val url: String) {
    URL_1("Glide - Image Loading Library by BumpTech", "https://github.com/bumptech/glide/archive/refs/heads/master.zip"),
    URL_2("LoadApp - Current repository by Udacity", "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"),
    URL_3("Retrofit - Type-safe HTTP client for Android and Java by Square, Inc", "https://github.com/square/retrofit/archive/refs/heads/master.zip"),
}
