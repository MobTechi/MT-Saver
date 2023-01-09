package com.mobtechi.mtsaver

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.mobtechi.mtsaver.Constants.newWAPath
import com.mobtechi.mtsaver.Constants.oldWAPath
import java.io.File
import java.net.URLConnection

object Functions {

    fun checkStoragePermission(activity: Activity): Boolean {
        var isGranted = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                isGranted = true
            }
        } else {
            val result: Int = ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            if (result == PackageManager.PERMISSION_GRANTED) {
                isGranted = true
            }
        }
        return isGranted
    }

    fun getAppPath(): String {
        return Environment.getExternalStorageDirectory().path + "/Download/MT Saver"
    }

    fun getStatusPath(): String {
        val oldPath = Environment.getExternalStorageDirectory().absolutePath + oldWAPath
        val newPath = Environment.getExternalStorageDirectory().absolutePath + newWAPath
        return if (Build.VERSION.SDK_INT <= 30) oldPath else newPath
    }

    fun getStatusList(activity: Activity, dirPath: String): List<File>? {
        if (checkStoragePermission(activity)) {
            val statusExtensions = arrayOf("3gp", "mp4", "png", "jpg", "jpeg")
            return File(dirPath).walk()
                // before entering this dir check if
                .filter { statusExtensions.contains(it.extension) }
                .toList()
        }
        return null
    }

    fun downloadFile(
        context: Context,
        fileName: String,
        downloadUrl: String,
    ) {
        val uri: Uri = Uri.parse(downloadUrl)
        val request = DownloadManager.Request(uri)
        request.setTitle(fileName)
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            "/MT Saver/reels/$fileName"
        )
        (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
    }

    fun shareFile(context: Activity, file: File) {
        val intentShareFile = Intent(Intent.ACTION_SEND)
        intentShareFile.type = URLConnection.guessContentTypeFromName(file.name)
        val fileUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        intentShareFile.putExtra(Intent.EXTRA_STREAM, fileUri)
        context.startActivity(Intent.createChooser(intentShareFile, "Share a file"))
    }

    fun copyFile(fromPath: String, toPath: String) {
        File(fromPath).copyTo(File(toPath), true)
    }
}