package de.fhkiel.temi.robogguide.pages

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.robotemi.sdk.Robot
import com.robotemi.sdk.TtsRequest
import de.fhkiel.temi.robogguide.R
import de.fhkiel.temi.robogguide.database.DatabaseHelper
import de.fhkiel.temi.robogguide.media.createYoutube
import de.fhkiel.temi.robogguide.media.downloadImage
import de.fhkiel.temi.robogguide.triplogic.RoundTrip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Tourscreen(private val context: Activity,
                 private val robot: Robot,
                 private val handleInitScreen: () -> Unit,
                 private val database: DatabaseHelper,
                 private val isAusführlich: Boolean = false,
                 private val locations : List<String> = robot.locations,
) {

    private val pics: Array<String> = arrayOf("https://httpcats.com/102.jpg", "https://httpcats.com/420.jpg",
        "https://httpcats.com/308.jpg","https://http.pizza/404.jpg", "https://http.pizza/500.jpg", "https://www.youtube.com/embed/kYvA1MdYkpE?si=YALhvp73mXMaD_-m")

    @SuppressLint("SetJavaScriptEnabled")
    fun handleTourScreen() {

        context.setContentView(R.layout.tour_screen)
        val images = context.findViewById<LinearLayout>(R.id.img)
        for (url in pics) {
            if(url.contains("youtube.com/")){
                val webView = WebView(context)

                val widthInPixels = 600
                val heightInPixels = 400

                val layoutParams = ViewGroup.LayoutParams(widthInPixels, heightInPixels)
                layoutParams.width = widthInPixels
                layoutParams.height = heightInPixels
                webView.layoutParams = layoutParams

                webView.loadData(createYoutube(url), "text/html", "utf-8")
                webView.settings.javaScriptEnabled= true
                webView.webChromeClient = WebChromeClient()
                images.addView(webView)
            } else {
                // Create a new ImageView
                val imageView = ImageView(context)

                // Set layout parameters for the ImageView
                val layoutParams = LinearLayout.LayoutParams(
                    400,
                    400
                )
                imageView.layoutParams = layoutParams

                CoroutineScope(Dispatchers.IO).launch {
                    val bitmap = downloadImage(url)
                    withContext(Dispatchers.Main) {
                        Log.i("wee", bitmap.toString())
                        imageView.setImageBitmap(bitmap)
                        images.addView(imageView)
                    }
                }
            }
        }


        val bar = context.findViewById<ProgressBar>(R.id.progressBar)

        val trip = RoundTrip(robot, 0, locations, context)
        robot.addOnGoToLocationStatusChangedListener(trip)
         updateText(trip);


         val backButton = context.findViewById<ImageButton>(R.id.backbutton)
        backButton.setOnClickListener {
            handleBackAction(trip)
            handleInitScreen()
        }

        val continueButton = context.findViewById<ImageButton>(R.id.continuebutton)
        continueButton.setOnClickListener{

            robot.cancelAllTtsRequests()
            val index = trip.index+1
            if(index == locations.size){
                val eval = EvalScreen(context, robot, database)
                eval.initScreen()
            } else {
                trip.index = index % locations.size
                bar.progress = trip.index * 100 / locations.size
                robot.goTo(locations[trip.index]);
                updateText(trip)
            }
        }
    }

    private fun handleBackAction(trip: RoundTrip) {
        context.setContentView(R.layout.first_screen)

        robot.removeOnGoToLocationStatusChangedListener(trip)
        robot.goTo(robot.locations[0])
        val ttsRequest = TtsRequest.create(
            speech = "Okay! Ich gehe dann wieder zum Anfang. Viel Spaß im Museum!",
            isShowOnConversationLayer = false
        )
        robot.speak(ttsRequest)
    }

    private fun updateText(trip: RoundTrip){
        val textPair = database.getTextsOfLocation(locations[trip.index], isAusführlich)
        //TODO: as far as I understand this cant work right now. I need to implement a queue for each place. fuck me!
        context.findViewById<TextView>(R.id.text_view).text = textPair[0][0]
        context.findViewById<TextView>(R.id.title_view).text = textPair[0][1]
    }
}