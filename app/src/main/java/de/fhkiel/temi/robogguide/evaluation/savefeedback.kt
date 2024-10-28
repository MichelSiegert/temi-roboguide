package de.fhkiel.temi.robogguide.evaluation
import android.util.Log

import java.io.File

import android.content.Context
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter


fun saveToFile(context: Context, fileContent: String) {
    val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    val fileName = "$today.txt"
    val file = File(context.filesDir, fileName)

    try {
        FileOutputStream(file, true).use { outputStream ->
            outputStream.write(fileContent.toByteArray())
            outputStream.flush()
        }
    } catch (e: IOException) {
        e.printStackTrace()

        Log.i("weee", "Error saving the file: ${e.localizedMessage}")
    }
}

