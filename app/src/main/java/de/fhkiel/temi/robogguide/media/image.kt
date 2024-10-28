package de.fhkiel.temi.robogguide.media

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.net.HttpURLConnection
import java.net.URL

fun downloadImage(urlString: String): Bitmap? {
    try {
        val url = URL(urlString)
        val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
        connection.doInput = true
        connection.connect()
        val inputStream = connection.inputStream
        return BitmapFactory.decodeStream(inputStream)
    } catch (e: Exception) {
        Log.i("download", e.toString())
    }
    return null
}
