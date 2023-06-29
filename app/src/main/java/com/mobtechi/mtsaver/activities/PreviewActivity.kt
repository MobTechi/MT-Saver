package com.mobtechi.mtsaver.activities

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
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
import com.mobtechi.mtsaver.Functions.copyFileUsingInputStream
import com.mobtechi.mtsaver.Functions.getFileSize
import com.mobtechi.mtsaver.Functions.shareFile
import com.mobtechi.mtsaver.R
import java.io.File

@Suppress("DEPRECATION")
class PreviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)
        initPreview()
    }

    private fun initPreview() {
        val isFromStatus = intent.getBooleanExtra("isFromStatus", false)
        if (isFromStatus) {
            val fileUri = intent.getStringExtra("fileUri")!!
            initSaveToolbarMenu(fileUri)
            if (intent.getStringExtra("fileType") == video) {
                initVideoPreview(fileUri)
            } else {
                initImagePreview(fileUri)
            }
        } else {
            val filePath = intent.getStringExtra("filePath")!!
            val previewFile = File(filePath)
            initShareToolbarMenu(previewFile)
            if (videoTypes.contains(previewFile.extension)) {
                initVideoPreview(previewFile.path)
            } else {
                initImagePreview(previewFile.path)
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun initSaveToolbarMenu(fileUri: String) {
        val fileName = intent.getStringExtra("fileName")
        val fileType = intent.getStringExtra("fileType")
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = fileName
        setSupportActionBar(toolbar)

        // If is status first download it
        val save = findViewById<ImageView>(R.id.share)
        save.setImageResource(R.drawable.ic_download)
        save.setOnClickListener {
            val statusPath = Functions.getAppPath() + "/status/"
            // copy the file using android File() for below android 10
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                copyFile(fileUri, statusPath + fileName)
                Functions.toast(this, "Status Saved!")
            } else {
                // copy the file using android content resolver for above android 10
                copyFileUsingInputStream(
                    this,
                    fileName!!,
                    fileType!!,
                    Uri.parse(fileUri),
                    statusPath
                )
                Functions.toast(this, "Status Saved!")
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun initShareToolbarMenu(previewFile: File) {
        val fileName = previewFile.name
        val fileSize = getFileSize(previewFile.length())
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = fileName
        toolbar.subtitle = fileSize
        setSupportActionBar(toolbar)
        val share = findViewById<ImageView>(R.id.share)
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
        val videoPreview = findViewById<VideoView>(R.id.videoPreview)
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
        this.finish()
    }
}