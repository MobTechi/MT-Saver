@file:Suppress("DEPRECATION")

package com.mobtechi.mtsaver.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.mobtechi.mtsaver.Constants.parsingURL
import com.mobtechi.mtsaver.Constants.rapidAPIHost
import com.mobtechi.mtsaver.Constants.rapidAPIHostValue
import com.mobtechi.mtsaver.Constants.rapidAPIKey
import com.mobtechi.mtsaver.Constants.rapidAPIKeyValue
import com.mobtechi.mtsaver.Constants.reelDownloadAPI
import com.mobtechi.mtsaver.Constants.tooManyRequestErrorCode
import com.mobtechi.mtsaver.Constants.tooManyRequestErrorMessage
import com.mobtechi.mtsaver.Functions.askStoragePermission
import com.mobtechi.mtsaver.Functions.checkIsReelLink
import com.mobtechi.mtsaver.Functions.checkStoragePermission
import com.mobtechi.mtsaver.Functions.downloadFile
import com.mobtechi.mtsaver.Functions.getContentType
import com.mobtechi.mtsaver.Functions.glideImageSet
import com.mobtechi.mtsaver.Functions.hideSoftKeyboard
import com.mobtechi.mtsaver.Functions.md5
import com.mobtechi.mtsaver.Functions.toast
import com.mobtechi.mtsaver.R
import com.mobtechi.mtsaver.databinding.FragmentReelBinding
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class ReelFragment : Fragment() {

    enum class ReelKey(val key: String) {
        Id("id"), Thumbnail("thumbnail"), Media("media"), Type("Type"), Title("title")
    }

    private var okHttpCall: Call? = null
    private var _binding: FragmentReelBinding? = null

    // value properties
    private var currentPosition = 0
    private var postSize = 0
    private val visible = View.VISIBLE
    private val gone = View.GONE

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        _binding = FragmentReelBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val linkEditText: EditText = root.findViewById(R.id.reelLink)
        val pasteLink: Button = root.findViewById(R.id.pasteLink)
        val searchReel: Button = root.findViewById(R.id.searchReel)
        val loadingCard: CardView = root.findViewById(R.id.loadingCard)
        val downloadCard: CardView = root.findViewById(R.id.downloadCard)
        val loadingMessage: TextView = root.findViewById(R.id.loadingMessage)

        linkEditText.clearFocus()

        linkEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (linkEditText.text.isNotEmpty()) {
                    pasteLink.text = getString(R.string.clear)
                } else {
                    pasteLink.text = getString(R.string.paste)
                }
            }
        })

        pasteLink.setOnClickListener {
            if (pasteLink.text == getString(R.string.clear)) {
                pasteLink.text = getString(R.string.paste)
                linkEditText.setText("")
                linkEditText.clearFocus()
                loadingCard.visibility = gone
                downloadCard.visibility = gone
                currentPosition = 0
                postSize = 0
                // if already requested the reel, cancel the http call request
                cancelOkHttp()
            } else {
                val clipboard: ClipboardManager =
                    requireContext().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clipText = clipboard.text
                if (clipText != null && clipText.isNotEmpty()) {
                    linkEditText.setText(clipboard.text.toString())
                    linkEditText.clearFocus()
                    pasteLink.text = getString(R.string.clear)
                    hideSoftKeyboard(requireContext(), it)
                } else {
                    toast(requireActivity(), getString(R.string.please_enter_link))
                }
            }
        }

        searchReel.setOnClickListener {
            hideSoftKeyboard(requireContext(), it)
            if (checkInternet()) {
                val reelLink = linkEditText.text.toString()
                if (TextUtils.isEmpty(reelLink) || !checkIsReelLink(reelLink)) {
                    linkEditText.error = getString(R.string.please_enter_link)
                    downloadCard.visibility = gone
                } else {
                    // if already requested the reel, cancel the http call request
                    cancelOkHttp()
                    // show the fetching loader
                    loadingCard.visibility = visible
                    loadingMessage.text = getString(R.string.fetching_details)
                    linkEditText.clearFocus()
                    // fetch the videos
                    val client = OkHttpClient().newBuilder().connectTimeout(2, TimeUnit.MINUTES)
                        .readTimeout(30, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS)
                        .build()
                    val request: Request =
                        Request.Builder().url("$reelDownloadAPI?url=$reelLink").get()
                            .addHeader(rapidAPIKey, rapidAPIKeyValue)
                            .addHeader(rapidAPIHost, rapidAPIHostValue).build()
                    okHttpCall = client.newCall(request)
                    okHttpCall?.enqueue(object : Callback {
                        @SuppressLint("SetTextI18n")
                        override fun onFailure(call: Call, e: IOException) {
                            showErrorMessage(loadingMessage, e.toString())
                        }

                        @SuppressLint("SetTextI18n")
                        @Throws(IOException::class)
                        override fun onResponse(call: Call, response: Response) {
                            if (response.code != tooManyRequestErrorCode) {
                                // if (response.isSuccessful && reelResponse.isNotEmpty() && reelResponse != nullVal) {
                                val reelResponse = response.body!!.string()
                                if (response.isSuccessful && reelResponse.isNotEmpty()) {
                                    showReelsResponse(root, JSONObject(reelResponse))
                                } else {
                                    showErrorMessage(loadingMessage, reelResponse)
                                }
                            } else {
                                showErrorMessage(loadingMessage, tooManyRequestErrorMessage)
                            }
                        }
                    })
                }
            }
        }

        return root
    }

    @SuppressLint("SetTextI18n")
    fun showReelsResponse(root: View, reelDetails: JSONObject) {
        // declare properties
        val loadingCard: CardView = root.findViewById(R.id.loadingCard)
        val downloadCard: CardView = root.findViewById(R.id.downloadCard)
        val reelTitle: TextView = root.findViewById(R.id.reelTitle)
        val imageSlider = root.findViewById<ImageView>(R.id.imageSlider)
        val prevSlide = root.findViewById<ImageView>(R.id.prevSlide)
        val nextSlide = root.findViewById<ImageView>(R.id.nextSlide)
        val reelMediaList = ArrayList<JSONObject>()
        if (reelDetails.getString("Type").equals("Carousel")) {
            val reelMedias = reelDetails.getJSONArray("media_with_thumb")
            (0 until reelMedias.length()).forEach { i ->
                val reelMedia = reelMedias.getJSONObject(i)
                val reelObject = JSONObject()
                reelObject.put(ReelKey.Id.key, md5(reelMedia.getString(ReelKey.Media.key)))
                reelObject.put(ReelKey.Thumbnail.key, reelMedia.getString(ReelKey.Thumbnail.key))
                reelObject.put(ReelKey.Media.key, reelMedia.getString(ReelKey.Media.key))
                reelObject.put(ReelKey.Type.key, reelMedia.getString(ReelKey.Type.key))
                reelMediaList.add(reelObject)
            }
        } else {
            val reelObject = JSONObject()
            reelObject.put(ReelKey.Id.key, md5(reelDetails.getString(ReelKey.Media.key)))
            reelObject.put(ReelKey.Thumbnail.key, reelDetails.getString(ReelKey.Thumbnail.key))
            reelObject.put(ReelKey.Media.key, reelDetails.getString(ReelKey.Media.key))
            reelObject.put(ReelKey.Type.key, reelDetails.getString(ReelKey.Type.key))
            reelMediaList.add(reelObject)
        }
        postSize = reelMediaList.size

        // run on ui thread to load properties in fragment
        requireActivity().runOnUiThread {
            run {
                loadingCard.visibility = gone
                downloadCard.visibility = visible
                glideImageSet(
                    requireContext(),
                    reelMediaList[currentPosition].getString(ReelKey.Thumbnail.key),
                    imageSlider
                )
                if (reelDetails.has(ReelKey.Title.key) && !reelDetails.get(ReelKey.Title.key)
                        .equals("null")
                ) {
                    reelTitle.visibility = visible
                    reelTitle.text = reelDetails.getString(ReelKey.Title.key)
                } else {
                    reelTitle.visibility = gone
                }

                val downloadReel: Button = root.findViewById(R.id.downloadReel)
                downloadReel.setOnClickListener {
                    if (checkInternet() && checkStoragePermission(requireActivity())) {
                        val reelObject = reelMediaList[currentPosition]
                        val reelId = md5(reelObject.getString(ReelKey.Id.key))
                        val downloadLink = reelObject.getString(ReelKey.Media.key)
                        val fileType = getContentType(reelObject.getString(ReelKey.Type.key))
                        val fileName = "$reelId.$fileType"
                        downloadFile(requireContext(), fileName, downloadLink)
                        toast(requireContext(), "Downloading.. $fileName")
                    } else {
                        askStoragePermission(requireActivity())
                    }
                }

                if (postSize > 1) {
                    downloadReel.text = getPositionTitle()
                    // If there are multiple postings, display the previous and next buttons.
                    nextSlide.visibility = visible
                    prevSlide.setOnClickListener {
                        currentPosition -= 1
                        downloadReel.text = getPositionTitle()
                        glideImageSet(
                            requireContext(),
                            reelMediaList[currentPosition].getString(ReelKey.Thumbnail.key),
                            imageSlider
                        )
                        prevSlide.visibility = if (currentPosition > 0) visible else gone
                        nextSlide.visibility =
                            if (currentPosition < (postSize - 1)) visible else gone
                    }

                    nextSlide.setOnClickListener {
                        currentPosition += 1
                        downloadReel.text = getPositionTitle()
                        glideImageSet(
                            requireContext(),
                            reelMediaList[currentPosition].getString(ReelKey.Thumbnail.key),
                            imageSlider
                        )
                        prevSlide.visibility = if (currentPosition > 0) visible else gone
                        nextSlide.visibility =
                            if (currentPosition < (postSize - 1)) visible else gone
                    }
                }
            }
        }
    }

    private fun getPositionTitle(): String {
        val title = getString(R.string.download)
        val position = (currentPosition + 1)
        return "$title($position)"
    }

    @SuppressLint("SetTextI18n")
    fun showErrorMessage(loadingMessage: TextView, message: String) {
        requireActivity().runOnUiThread {
            run {
                loadingMessage.text = "$parsingURL: $message"
            }
        }
    }

    private fun checkInternet(): Boolean {
        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val isConnected =
            connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo!!.isConnected
        if (!isConnected) {
            showInternetDialog()
        }
        return isConnected
    }

    private fun showInternetDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.no_internet).setMessage(R.string.need_internet)
            .setPositiveButton(R.string.try_again) { dialog, _ -> dialog.cancel() }
        builder.create().show()
    }

    private fun cancelOkHttp() {
        if (okHttpCall != null && okHttpCall!!.isExecuted()) {
            okHttpCall?.cancel()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cancelOkHttp()
    }
}