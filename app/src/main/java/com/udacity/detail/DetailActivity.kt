package com.udacity.detail

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.udacity.databinding.ActivityDetailBinding
import com.udacity.main.MainActivity
import com.udacity.model.TransferData
import com.udacity.utils.DATA_KEY

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val intent = intent

        if (intent != null && intent.hasExtra(DATA_KEY)) {
            val data: TransferData? = intent.getParcelableExtra(DATA_KEY)

            if (data != null) {
                binding.contentDetail.fileNameValue.text = data.fileName
                binding.contentDetail.statusValue.text = data.downloadStatus
            }
        }

        binding.contentDetail.dismissButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
