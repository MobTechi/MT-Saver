package com.mobtechi.mtsaver.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.MediaController
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.mobtechi.mtsaver.Constants.video
import com.mobtechi.mtsaver.Constants.videoTypes
import com.mobtechi.mtsaver.Functions
import com.mobtechi.mtsaver.Functions.copyFile
import com.mobtechi.mtsaver.Functions.getFileSize
import com.mobtechi.mtsaver.Functions.shareFile
import com.mobtechi.mtsaver.R
import java.io.File
import kotlin.properties.Delegates

@Suppress("DEPRECATION")
class PreviewActivity : AppCompatActivity() {
    private var isFromStatus by Delegates.notNull<Boolean>()
    private lateinit var share: ImageView
    private lateinit var videoPreview: VideoView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)
        isFromStatus = intent.getBooleanExtra("isFromStatus", false)
        initPreview()
    }

    private fun initPreview() {
        val filePath = intent.getStringExtra("filePath")!!
        share = findViewById(R.id.share)
        if (isFromStatus) {
            val fileUri = intent.getStringExtra("fileUri")!!
            share.setImageResource(R.drawable.ic_download)
            initStatusMenuOptions(filePath)
            if (intent.getStringExtra("fileType") == video) {
                initVideoPreview(fileUri)
            } else {
                initImagePreview(fileUri)
            }
        } else {
            val previewFile = File(filePath)
            initSavedMenuOptions(previewFile)
            if (videoTypes.contains(previewFile.extension)) {
                initVideoPreview(previewFile.path)
            } else {
                initImagePreview(previewFile.path)
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun initStatusMenuOptions(filePath: String) {
        val fileName = intent.getStringExtra("fileName")
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = fileName
        setSupportActionBar(toolbar)

        // If is status first download it
        share.setOnClickListener {
            val statusPath = Functions.getAppPath() + "/status/"
            copyFile(filePath, statusPath + fileName)
        }
    }

    @SuppressLint("InflateParams")
    private fun initSavedMenuOptions(previewFile: File) {
        val fileName = previewFile.name
        val fileSize = getFileSize(previewFile.length())
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = fileName
        toolbar.subtitle = fileSize
        setSupportActionBar(toolbar)

        share.setOnClickListener {
            val fileUri = FileProvider.getUriForFile(this, "${packageName}.provider", previewFile)
            shareFile(this, previewFile.name, fileUri)
        }
    }

    private fun initImagePreview(imagePath: String) {
        val imagePreview = findViewById<ImageView>(R.id.imagePreview)
        imagePreview.visibility = View.VISIBLE
        Glide.with(this).load(imagePath).placeholder(R.drawable.loader).into(imagePreview)
    }

    private fun initVideoPreview(videoPath: String) {
        videoPreview = findViewById(R.id.videoPreview)
        videoPreview.visibility = View.VISIBLE
        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoPreview)
        videoPreview.setMediaController(mediaController)
        videoPreview.setVideoPath(videoPath)
        videoPreview.requestFocus()
        videoPreview.setOnPreparedListener {
            videoPreview.start()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}