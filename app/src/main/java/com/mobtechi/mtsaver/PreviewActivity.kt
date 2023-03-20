package com.mobtechi.mtsaver

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.MediaController
import android.widget.VideoView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.mobtechi.mtsaver.Constants.videoTypes
import com.mobtechi.mtsaver.Functions.getFileSize
import java.io.File

@Suppress("DEPRECATION")
class PreviewActivity : AppCompatActivity() {
    private lateinit var previewFile: File
    private lateinit var videoPreview: VideoView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)
        val filePath = intent.getStringExtra("filePath")
        if (filePath != null) {
            initPreview(filePath)
        } else {
            finish()
        }
    }

    private fun initPreview(filePath: String) {
        previewFile = File(filePath)
        initMenuOptions()
        if (videoTypes.contains(previewFile.extension)) {
            initVideoPreview()
        } else {
            initImagePreview()
        }
    }

    @SuppressLint("InflateParams")
    private fun initMenuOptions() {
        val fileName = previewFile.name
        val fileSize = getFileSize(previewFile.length())
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = fileName
        toolbar.subtitle = fileSize
        setSupportActionBar(toolbar)

        val share = findViewById<ImageView>(R.id.share)

        share.setOnClickListener {
            Functions.shareFile(this, previewFile)
        }
    }

    private fun initImagePreview() {
        val imagePreview = findViewById<ImageView>(R.id.imagePreview)
        imagePreview.visibility = View.VISIBLE
        Glide.with(this).load(previewFile.path).placeholder(R.drawable.loader).into(imagePreview)
    }

    private fun initVideoPreview() {
        videoPreview = findViewById(R.id.videoPreview)
        videoPreview.visibility = View.VISIBLE
        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoPreview)
        videoPreview.setMediaController(mediaController)
        videoPreview.setVideoPath(previewFile.path)
        videoPreview.requestFocus()
        videoPreview.setOnPreparedListener {
            videoPreview.start()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}