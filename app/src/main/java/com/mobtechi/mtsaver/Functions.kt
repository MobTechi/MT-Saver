@file:Suppress("CAST_NEVER_SUCCEEDS")

package com.mobtechi.mtsaver

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.mobtechi.mtsaver.Constants.mp4
import com.mobtechi.mtsaver.Constants.newWAPath
import com.mobtechi.mtsaver.Constants.oldWAPath
import com.mobtechi.mtsaver.Constants.png
import com.mobtechi.mtsaver.Constants.video
import java.io.File
import java.net.URLConnection
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.DecimalFormat

object Functions {

    private const val storagePref = "storagePref"

    // shared preference functions

    fun saveStoragePermissionPref(activity: Activity, isGranted: Boolean) {
        val editor: SharedPreferences.Editor =
            activity.getSharedPreferences(storagePref, MODE_PRIVATE).edit()
        editor.putBoolean("isGranted", isGranted)
        editor.apply()
    }

    private fun getStoragePermissionPref(activity: Activity): Boolean {
        val prefs: SharedPreferences = activity.getSharedPreferences(storagePref, MODE_PRIVATE)
        return prefs.getBoolean("isGranted", false)
    }

    // permission functions

    fun checkStoragePermission(activity: Activity): Boolean {
        var isPhotoVideoGranted = true
        var isStatusGranted = true
        // check photo and video access permission for android 13
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val imageResult: Int = ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_MEDIA_IMAGES
            )
            val videoResult: Int = ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_MEDIA_VIDEO
            )
            isPhotoVideoGranted =
                imageResult == PackageManager.PERMISSION_GRANTED && videoResult == PackageManager.PERMISSION_GRANTED
        }
        // check status access permission for android 10 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            isStatusGranted = getStoragePermissionPref(activity)
        }
        val isWriteGranted: Boolean = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            // check the write storage permission
            val result: Int = ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            result == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        return isPhotoVideoGranted && isStatusGranted && isWriteGranted
    }

    fun askStoragePermission(activity: Activity) {
        // ask storage permission for normal files access for android 13
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO
                ),
                Constants.lowerSdkStoragePermissionCode
            )
        }
        // request for the status folder access permission android 11 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !getStoragePermissionPref(activity)) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            val waPath = getStatusPath()
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.parse(waPath))
            activity.startActivityForResult(intent, Constants.higherSdkStoragePermissionCode)
        }

        // request for the status folder access permission below android 11
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            ActivityCompat.requestPermissions(
                activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                Constants.lowerSdkStoragePermissionCode
            )
        }
    }

    // get path functions

    fun getAppPath(): String {
        return Environment.getExternalStorageDirectory().absolutePath + "/Download/MT Saver"
    }

    fun getStatusPath(): String {
        val oldPath = Environment.getExternalStorageDirectory().absolutePath + oldWAPath
        val newPath = Environment.getExternalStorageDirectory().absolutePath + newWAPath
        return if (Build.VERSION.SDK_INT <= 30) oldPath else newPath
    }

    // get status list

    @SuppressLint("Range")
    fun getStatusList(activity: Activity, dirPath: String): List<File> {
        var savedFiles: ArrayList<File> = ArrayList()
        // For Android 10 and higher (API level 29+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.MIME_TYPE
            )
            val selection = "${MediaStore.MediaColumns.DATA} like ?"
            val selectionArgs = arrayOf("%$dirPath%")
            val sortOrder = "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"
            val cursor: Cursor? = activity.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                selectionArgs,
                sortOrder
            )
            cursor?.use {
                while (it.moveToNext()) {
                    val mimeType =
                        it.getString(it.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE))
                    if (mimeType.startsWith("image") || mimeType.startsWith("video")) {
                        // Add the file path to the list
                        savedFiles.add(File(it.getString(it.getColumnIndex(MediaStore.MediaColumns.DATA))))
                    }
                }
            }
        } else {
            savedFiles = getFiles(dirPath)
        }
        return savedFiles
    }

    // get saved list

    @SuppressLint("Range")
    fun getSavedList(activity: Activity, dirPath: String): List<File> {
        var savedFiles: ArrayList<File> = ArrayList()
        // For Android 10 and higher (API level 29+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.MIME_TYPE
            )
            val selection = "${MediaStore.MediaColumns.DATA} like ?"
            val selectionArgs = arrayOf("%$dirPath%")
            val sortOrder = "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"
            val cursor: Cursor? = activity.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                selectionArgs,
                sortOrder
            )
            cursor?.use {
                while (it.moveToNext()) {
                    val mimeType =
                        it.getString(it.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE))
                    if (mimeType.startsWith("image") || mimeType.startsWith("video")) {
                        // Add the file path to the list
                        savedFiles.add(File(it.getString(it.getColumnIndex(MediaStore.MediaColumns.DATA))))
                    }
                }
            }
        } else {
            savedFiles = getFiles(dirPath)
        }
        return savedFiles
    }

    private fun getFiles(dirPath: String): ArrayList<File> {
        val files: ArrayList<File> = ArrayList()
        val waFile = File(dirPath)
        if (waFile.exists() && waFile.listFiles() != null) {
            val statusExtensions = arrayOf("3gp", "mp4", "png", "jpg", "jpeg")
            for (file in waFile.listFiles()!!) {
                if (!files.contains(file) && statusExtensions.contains(file.extension)) {
                    files.add(file)
                }
            }
        }
        return files
    }

    // download function

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

    // util functions

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

    fun getContentType(type: String): String {
        return if (type.contains(Regex(video))) mp4 else png
    }
}