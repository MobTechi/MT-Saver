package com.mobtechi.mtsaver.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mobtechi.mtsaver.Constants.video
import com.mobtechi.mtsaver.Functions
import com.mobtechi.mtsaver.Functions.copyFile
import com.mobtechi.mtsaver.Functions.copyFileUsingInputStream
import com.mobtechi.mtsaver.Functions.glideImageSet
import com.mobtechi.mtsaver.Functions.shareFile
import com.mobtechi.mtsaver.Functions.toast
import com.mobtechi.mtsaver.R
import com.mobtechi.mtsaver.activities.PreviewActivity
import com.mobtechi.mtsaver.modal.StatusModal

@Suppress("DEPRECATION")
class StatusAdapter(private var context: Activity) :
    RecyclerView.Adapter<StatusAdapter.ViewHolder>() {

    private var dataList = emptyList<StatusModal>()

    internal fun setDataList(dataList: List<StatusModal>) {
        this.dataList = dataList
    }

    // Provide a direct reference to each of the views with data items

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var statusImage: ImageView
        var playIcon: ImageView
        var moreOption: ImageView

        init {
            statusImage = itemView.findViewById(R.id.statusImage)
            playIcon = itemView.findViewById(R.id.playIcon)
            moreOption = itemView.findViewById(R.id.moreOption)
        }

    }

    // Usually involves inflating a layout from XML and returning the holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate the custom layout
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.recycler_item, parent, false)
        return ViewHolder(view)
    }

    // Involves populating data into the item through holder
    @SuppressLint("CheckResult", "MissingInflatedId", "InflateParams")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Get the data model based on position
        val data = dataList[position]
        holder.playIcon.visibility = if (data.fileType == video) View.VISIBLE else View.GONE
        glideImageSet(context, data.fileUri.toString(), holder.statusImage)

        holder.statusImage.setOnClickListener {
            val previewIntent = Intent(context, PreviewActivity::class.java)
            previewIntent.putExtra("fileUri", data.fileUri.toString())
            previewIntent.putExtra("fileName", data.fileName)
            previewIntent.putExtra("fileType", data.fileType)
            previewIntent.putExtra("filePath", data.filePath)
            previewIntent.putExtra("isFromStatus", true)
            context.startActivity(previewIntent)
        }

        holder.moreOption.setOnClickListener {
            val dialog = BottomSheetDialog(context, R.style.BottomSheetStyle)
            val view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_option, null)
            val btnShare = view.findViewById<FloatingActionButton>(R.id.share)
            val btnSave = view.findViewById<FloatingActionButton>(R.id.save)

            btnShare.setOnClickListener {
                dialog.dismiss()
                shareFile(context, data.fileName, data.fileUri)
            }

            btnSave.setOnClickListener {
                dialog.dismiss()
                val fileName = data.fileName
                val statusPath = Functions.getAppPath() + "/status/"
                // copy the file using android File() for below android 10
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    copyFile(data.fileUri.toString(), statusPath + fileName)
                    toast(context, "Status Saved!")
                } else {
                    // copy the file using android content resolver for above android 10
                    copyFileUsingInputStream(
                        context,
                        fileName,
                        data.fileType,
                        data.fileUri,
                        statusPath
                    )
                    toast(context, "Status Saved!")
                }
            }
            dialog.setContentView(view)
            dialog.show()
        }
    }

    //  total count of items in the list
    override fun getItemCount() = dataList.size
}