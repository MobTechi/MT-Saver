package com.mobtechi.mtsaver.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mobtechi.mtsaver.Constants.videoTypes
import com.mobtechi.mtsaver.Functions
import com.mobtechi.mtsaver.Functions.copyFile
import com.mobtechi.mtsaver.Functions.getAppPath
import com.mobtechi.mtsaver.Functions.glideImageSet
import com.mobtechi.mtsaver.Functions.shareFile
import com.mobtechi.mtsaver.Functions.toast
import com.mobtechi.mtsaver.R
import java.io.File

@Suppress("DEPRECATION")
class StatusAdapter(private var context: Activity) :
    RecyclerView.Adapter<StatusAdapter.ViewHolder>() {

    private var dataList = emptyList<File>()

    internal fun setDataList(dataList: List<File>) {
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
        holder.playIcon.visibility =
            if (videoTypes.contains(data.extension)) View.VISIBLE else View.GONE

        glideImageSet(context, data.path, holder.statusImage)

        holder.statusImage.setOnClickListener {
            Functions.openPreviewActivity(context, data.path)
        }

        holder.moreOption.setOnClickListener {
            val dialog = BottomSheetDialog(context, R.style.BottomSheetStyle)
            val view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_option, null)
            val btnShare = view.findViewById<FloatingActionButton>(R.id.share)
            val btnSave = view.findViewById<FloatingActionButton>(R.id.save)

            btnShare.setOnClickListener {
                dialog.dismiss()
                shareFile(context, data)
            }

            btnSave.setOnClickListener {
                dialog.dismiss()
                val fileName = data.name
                val statusPath = getAppPath() + "/status/"
                copyFile(data.path, statusPath + fileName)
                toast(context, "Status Saved!")
            }
            dialog.setContentView(view)
            dialog.show()
        }
    }

    //  total count of items in the list
    override fun getItemCount() = dataList.size
}