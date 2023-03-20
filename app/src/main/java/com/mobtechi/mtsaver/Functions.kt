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
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.mobtechi.mtsaver.Constants.newWAPath
import com.mobtechi.mtsaver.Constants.oldWAPath
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.DecimalFormat

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

    fun getFileSize(size: Long): String {
        val bytes = 1024
        val dec = DecimalFormat("###.#")
        val kb = " KB"
        val mb = " MB"
        var fileSize = 0f
        var suffix = kb
        if (size > bytes) {
            fileSize = (size / bytes).toFloat()
            if (fileSize > bytes) {
                fileSize /= bytes
                if (fileSize < bytes) {
                    suffix = mb
                }
            }
        }
        return (dec.format(fileSize) + suffix)
    }


    fun openPreviewActivity(context: Context, filePath: String) {
        val previewIntent = Intent(context, PreviewActivity::class.java)
        previewIntent.putExtra("filePath", filePath)
        context.startActivity(previewIntent)
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

    fun toast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun checkIsReelLink(link: String): Boolean {
        val regex = Regex("https://www.instagram.com/(.*)")
        return link.matches(regex)
    }

    fun glideImageSet(context: Context, url: String, imageView: ImageView) {
        Glide.with(context)
            .load(url)
            .placeholder(R.drawable.loader)
            .centerCrop()
            .into(imageView)
    }

    fun hideSoftKeyboard(context: Context, view: View) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun md5(string: String): String {
        if (string.isEmpty()) {
            return System.currentTimeMillis().toString()
        }
        try {
            // Create MD5 Hash
            val digest: MessageDigest = MessageDigest.getInstance("MD5")
            digest.update(string.toByteArray())
            val messageDigest: ByteArray = digest.digest()

            // Create Hex String
            val hexString = StringBuffer()
            for (i in messageDigest.indices) hexString.append(
                Integer.toHexString(
                    0xFF and messageDigest[i]
                        .toInt()
                )
            )
            return hexString.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return System.currentTimeMillis().toString()
    }
}