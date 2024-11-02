package de.fhkiel.temi.robogguide

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageButton
import com.robotemi.sdk.Robot
import de.fhkiel.temi.robogguide.database.DatabaseHandler
import de.fhkiel.temi.robogguide.media.downloadImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LocationToggleManager(private val context: Context, private val mRobot: Robot?) {
    val toggledList = mutableListOf<String>()

    fun populateLocationToggles(layout: GridLayout) {

        val db = DatabaseHandler.getDb()!!
        mRobot?.locations?.forEach { location ->
            val url = db.getImageOfLocation(location)

            if(url.isEmpty()) {
                val button = Button(context)
                button.text = location
                button.layoutParams = ViewGroup.LayoutParams(400, 300)
                button.setBackgroundColor(Color.LTGRAY)
                button.setOnClickListener{
                    if(toggledList.contains(location)) {
                        toggledList.remove(location)
                        button.setBackgroundColor(Color.LTGRAY)
                    }
                    else {
                        toggledList.add(location)
                        button.setBackgroundColor(Color.GREEN)
                    }
                }
                layout.addView(button)
            } else {
            CoroutineScope(Dispatchers.IO).launch {
                val bitmap = downloadImage(url) ?: return@launch
                val resizedBitmap = resizeBitmap(bitmap, 400, 300)
                val grayscaleBitmap = convertToGrayscale(resizedBitmap)

                withContext(Dispatchers.Main) {
                    val button = ImageButton(context)
                    val layoutParams = ViewGroup.LayoutParams(400, 300)
                    button.layoutParams = layoutParams

                    button.setImageBitmap(grayscaleBitmap)
                    button.setOnClickListener {
                        if(toggledList.contains(location)) {
                            toggledList.remove(location)
                            button.setImageBitmap(grayscaleBitmap)
                        }
                        else {
                            toggledList.add(location)
                            button.setImageBitmap(resizedBitmap)
                            }
                        }
                        layout.addView(button)
                    }
                }
            }
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    private fun convertToGrayscale(src: Bitmap): Bitmap {
        // Create a new bitmap with the same dimensions as the source
        val width = src.width
        val height = src.height
        val grayscaleBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        // Create a canvas to draw on the new bitmap
        val canvas = Canvas(grayscaleBitmap)

        // Create a paint object to apply the color filter
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f) // Set saturation to 0 for grayscale
        val colorFilter = ColorMatrixColorFilter(colorMatrix)
        paint.colorFilter = colorFilter

        // Draw the source bitmap on the new bitmap using the paint with the color filter
        canvas.drawBitmap(src, 0f, 0f, paint)

        return grayscaleBitmap
    }
    }

