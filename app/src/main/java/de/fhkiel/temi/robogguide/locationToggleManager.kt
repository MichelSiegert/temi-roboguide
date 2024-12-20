package de.fhkiel.temi.robogguide

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
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


    /**
     * Creates the toggleable buttons and adds them to a gridview.
     * It Resizes them to be clickable.
     */
    fun populateLocationToggles(layout: GridLayout, updateTimestamp: () -> Unit) {
        val db = DatabaseHandler.getDb()!!
        mRobot?.locations?.forEach { location ->
            if( Routes.map.filter { it.value == location}.toList().isEmpty()) return
            val url = db.getImageOfLocation(location)

            if(url.isEmpty()) {
                val button = Button(context)
                button.text = location
                button.tag = 0
                button.layoutParams = ViewGroup.LayoutParams(400, 300)
                button.setBackgroundColor(Color.LTGRAY)
                button.setOnClickListener{
                    updateTimestamp()
                    if(toggledList.contains(location)) {
                        toggledList.remove(location)
                        button.tag = 0
                        button.setBackgroundColor(Color.LTGRAY)
                    }
                    else {
                        toggledList.add(location)
                        button.tag = 1
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
                    button.tag = 0

                    val layoutParams = ViewGroup.LayoutParams(400, 300)
                    button.layoutParams = layoutParams

                    button.setImageBitmap(grayscaleBitmap)
                    button.setOnClickListener {
                        updateTimestamp()
                        if(toggledList.contains(location)) {
                            button.tag = 0
                            toggledList.remove(location)
                            button.setImageBitmap(grayscaleBitmap)
                        }
                        else {
                            button.tag=1
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
        val width = src.width
        val height = src.height
        val grayscaleBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(grayscaleBitmap)

        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)
        val colorFilter = ColorMatrixColorFilter(colorMatrix)
        paint.colorFilter = colorFilter
        canvas.drawBitmap(src, 0f, 0f, paint)

        return grayscaleBitmap
    }
    }

