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

    private val trip: RoundTrip
    private val bar: ProgressBar

    init {
        context.setContentView(R.layout.tour_screen)
        bar = context.findViewById(R.id.progressBar)
        trip = RoundTrip(robot, 0, locations, context, ::handleBackAction, ::tryAgain, ::continueTour)
    }
    @SuppressLint("SetJavaScriptEnabled")
    fun handleTourScreen() {

        robot.addOnGoToLocationStatusChangedListener(trip)
         updateText();

         val backButton = context.findViewById<ImageButton>(R.id.backbutton)
        backButton.setOnClickListener {
            handleBackAction()
            handleInitScreen()
        }

        val continueButton = context.findViewById<ImageButton>(R.id.continuebutton)
        continueButton.setOnClickListener{
            continueTour()
        }
    }

    private fun handleBackAction() {
        context.setContentView(R.layout.first_screen)

        robot.removeOnGoToLocationStatusChangedListener(trip)
        robot.goTo(robot.locations[0], false)
        val ttsRequest = TtsRequest.create(
            speech = "Okay! Ich gehe dann wieder zum Anfang. Viel Spaß im Museum!",
            isShowOnConversationLayer = false
        )
        robot.speak(ttsRequest)
    }

    private fun updateText(){
        val textPair = database.getTextsOfLocation(locations[trip.index], isAusführlich)
        //TODO: as far as I understand this cant work right now. I need to implement a queue for each place. fuck me!
        context.findViewById<TextView>(R.id.text_view).text = textPair[0][0]
        context.findViewById<TextView>(R.id.title_view).text = textPair[0][1]
        loadImages(textPair[0][2])
    }

    private fun continueTour(){

        robot.cancelAllTtsRequests()
        val index = trip.index+1
        if(index == locations.size){
            val eval = EvalScreen(context, robot, database)
            eval.initScreen()
        } else {
            trip.index = index % locations.size
            bar.progress = trip.index * 100 / locations.size
            robot.goTo(locations[trip.index], false);
            updateText()
        }

    }

    private fun tryAgain(){
        robot.goTo(locations[trip.index])
    }

    private fun loadImages(id: String){
        val images = context.findViewById<LinearLayout>(R.id.img)
        val pics = database.getMediaOfText(id)
        images.removeAllViews()
        for (url in pics) {
            Log.i("url", url)
            if(url.contains("youtube.com/")){
                Log.i("yes", url)
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
                val imageView = ImageView(context)

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

    }
}