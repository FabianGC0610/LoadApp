package com.udacity.detail

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.udacity.databinding.ActivityDetailBinding
import com.udacity.main.MainActivity
import com.udacity.model.TransferData
import com.udacity.utils.DATA_KEY

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    private lateinit var viewModel: DetailActivityViewModel

    private var isAnimationStarted = false

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        viewModel = ViewModelProvider(this)[DetailActivityViewModel::class.java]

        val intent = intent

        if (intent != null && intent.hasExtra(DATA_KEY)) {
            val data: TransferData? = intent.getParcelableExtra(DATA_KEY)

            if (data != null) {
                binding.contentDetail.fileNameValue.text = data.fileName
                binding.contentDetail.statusValue.text = data.downloadStatus
                viewModel.toStatusFormat(data.downloadStatus, binding.contentDetail.statusValue)
                binding.contentDetail.sizeValue.text = viewModel.toMbFormat(data.fileSize)
                binding.contentDetail.typeValue.text = data.fileType
                binding.contentDetail.uriValue.text = data.fileUri
            }
        }

        binding.contentDetail.dismissButton.setOnClickListener {
            viewModel.onBackToMainEvent()
        }

        viewModel.backToMainEvent.observe(this) { backToMainEvent ->
            if (backToMainEvent) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                viewModel.onBackToMainEventCompleted()
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus && !isAnimationStarted) {
            startAnimation()
            isAnimationStarted = true
        }
    }

    private fun startAnimation() {
        binding.contentDetail.detailLayout.transitionToEnd()
    }
}
