package com.mobtechi.mtsaver

object Constants {
    const val tooManyRequestErrorCode = 429
    const val image = "Image"
    const val png = "png"
    const val mp4 = "mp4"
    val videoTypes = arrayOf("3gp", "mp4")

    // whats app constants
    const val oldWAPath = "/WhatsApp/Media/.Statuses"
    const val newWAPath = "/Android/media/com.whatsapp/WhatsApp/Media/.Statuses"

    // instagram API constants
    const val reelDownloadAPI =
        "https://instagram-downloader-download-instagram-videos-stories.p.rapidapi.com/index"
    const val rapidAPIKey = "X-RapidAPI-Key"
    const val rapidAPIKeyValue = "4130ce89c1mshd39543e2a88d47dp1877a7jsn0ed7e153cb4c"
    const val rapidAPIHost = "X-RapidAPI-Host"
    const val rapidAPIHostValue =
        "instagram-downloader-download-instagram-videos-stories.p.rapidapi.com"

    // error messages
    const val parsingURL = "Error While Parsing Url"
    const val tooManyRequestErrorMessage = "Multiple requests were made. So, API is now in maintenance. Please come back after some time!"
}