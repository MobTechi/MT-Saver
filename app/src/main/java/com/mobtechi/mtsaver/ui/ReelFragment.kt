@file:Suppress("DEPRECATION")

package com.mobtechi.mtsaver.ui

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.mobtechi.mtsaver.Constants.image
import com.mobtechi.mtsaver.Constants.mp4
import com.mobtechi.mtsaver.Constants.png
import com.mobtechi.mtsaver.Constants.rapidAPIHost
import com.mobtechi.mtsaver.Constants.rapidAPIHostValue
import com.mobtechi.mtsaver.Constants.rapidAPIKey
import com.mobtechi.mtsaver.Constants.rapidAPIKeyValue
import com.mobtechi.mtsaver.Constants.reelDownloadAPI
import com.mobtechi.mtsaver.Functions.downloadFile
import com.mobtechi.mtsaver.R
import com.mobtechi.mtsaver.databinding.FragmentReelBinding
import okhttp3.*
import okio.IOException
import org.json.JSONObject


class ReelFragment : Fragment() {

    private var _binding: FragmentReelBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentReelBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val linkEditText: EditText = root.findViewById(R.id.reelLink)
        val clearLink: Button = root.findViewById(R.id.clearLink)
        val searchReel: Button = root.findViewById(R.id.searchReel)
        val downloadReel: Button = root.findViewById(R.id.downloadReel)
        val downloadCard: CardView = root.findViewById(R.id.downloadCard)

        val reelTitle: TextView = root.findViewById(R.id.reelTitle)
        val reelImg: ImageView = root.findViewById(R.id.reelImg)

        var downloadLink = ""
        var fileType = ""

        clearLink.setOnClickListener {
            linkEditText.setText("")
            linkEditText.clearFocus()
            downloadCard.visibility = View.GONE
        }

        searchReel.setOnClickListener {
            if (checkInternet()) {
                val dialog = ProgressDialog(requireContext())
                dialog.setCancelable(false)
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
                val reelLink = linkEditText.text.toString()
                if (TextUtils.isEmpty(reelLink)) {
                    linkEditText.error = getString(R.string.please_enter_link)
                    downloadCard.visibility = View.GONE
                } else {
                    // show search progress dialog
                    dialog.setTitle(R.string.fetching_details)
                    dialog.show()
                    // fetch the videos
                    val client = OkHttpClient()
                    val request: Request = Request.Builder()
                        .url("$reelDownloadAPI?url=$reelLink")
                        .get()
                        .addHeader(rapidAPIKey, rapidAPIKeyValue)
                        .addHeader(rapidAPIHost, rapidAPIHostValue)
                        .build()
                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            dialog.dismiss()
                            Toast.makeText(
                                requireContext(),
                                "Error While Parsing Url",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        @Throws(IOException::class)
                        override fun onResponse(call: Call, response: Response) {
                            if (response.isSuccessful) {
                                val reelResponse = response.body!!.string()
                                val reelResponseObj = JSONObject(reelResponse)
                                val type = reelResponseObj.getString("Type")
                                downloadLink = reelResponseObj.getString("media")
                                fileType = if (type.contains(image)) png else mp4
                                val reelName = reelResponseObj.getString("title")
                                val thumbnail = reelResponseObj.getString("thumbnail")
                                // run on ui thread to load properties in fragment
                                requireActivity().runOnUiThread {
                                    run()
                                    {
                                        downloadCard.visibility = View.VISIBLE
                                        reelTitle.text = reelName
                                        Glide.with(requireContext())
                                            .load(thumbnail)
                                            .placeholder(R.drawable.ic_reel)
                                            .centerCrop()
                                            .into(reelImg)
                                    }
                                }
                            }
                            dialog.dismiss()
                        }
                    })

                }
            }
        }

        downloadReel.setOnClickListener {
            if (checkInternet()) {
                val fileName = System.currentTimeMillis().toString() + ".$fileType"
                downloadFile(requireContext(), fileName, downloadLink)
                Toast.makeText(context, "Downloading..", Toast.LENGTH_SHORT).show()
            }
        }

        return root
    }

    private fun checkInternet(): Boolean {
        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val isConnected =
            connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo!!
                .isConnected
        if (!isConnected) {
            showInternetDialog()
        }
        return isConnected
    }

    private fun showInternetDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.no_internet)
            .setMessage(R.string.need_internet)
            .setPositiveButton(R.string.try_again) { dialog, _ -> dialog.cancel() }
        builder.create().show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}