package de.fhkiel.temi.robogguide.pages

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.robotemi.sdk.Robot
import com.robotemi.sdk.TtsRequest
import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener
import de.fhkiel.temi.robogguide.R
import de.fhkiel.temi.robogguide.Routes
import de.fhkiel.temi.robogguide.database.DatabaseHandler
import de.fhkiel.temi.robogguide.media.YoutubePlayerListener
import de.fhkiel.temi.robogguide.media.getID
import de.fhkiel.temi.robogguide.media.downloadImage
import de.fhkiel.temi.robogguide.triplogic.RoundTrip
import de.fhkiel.temi.robogguide.triplogic.Speaker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Tourscreen(private val context: Activity,
                 private val robot: Robot,
                 private val handleInitScreen: () -> Unit,
                 private val isAusführlich: Boolean = false,
                 private val locations : List<String>,
) {
    private val database = DatabaseHandler.getDb()!!
    private val trip = RoundTrip(robot, 0, locations, context, ::handleBackAction, ::tryAgain, ::continueTour, ::progressTour, ::hasFinishedSpeaking)
    private val bar: ProgressBar
    private val speaking = Speaker(::progressTour)
    private val youtubeHandlers: MutableList<YoutubePlayerListener>  = mutableListOf()


    init {
        context.setContentView(R.layout.tour_screen)
        bar = context.findViewById(R.id.progressBar)

        robot.addTtsListener(speaking)
    }
    @SuppressLint("SetJavaScriptEnabled")
    fun handleTourScreen() {

        robot.addOnGoToLocationStatusChangedListener(trip)
        continueTour(true)

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
        handleInitScreen()
        robot.removeOnGoToLocationStatusChangedListener(trip)
        robot.removeTtsListener(speaking)
        robot.goTo(Routes.start, false)
        val ttsRequest = TtsRequest.create(
            speech = "Okay! Ich gehe dann wieder zum Anfang. Viel Spaß im Museum!",
            isShowOnConversationLayer = false)
        robot.speak(ttsRequest)
        Log.i("what", ttsRequest.status.toString())
    }

    private fun updateText(information: List<String>){
        context.findViewById<TextView>(R.id.text_view)?.text = information[0]
        context.findViewById<TextView>(R.id.title_view)?.text = information[1]
        loadImages(information[2])
    }

    private fun continueTour(isFirst: Boolean = false){
        trip.queue.clear()
        trip.lastLocationStatus = "ABORT"
        speaking.lastStatus = TtsRequest.Status.STARTED
        robot.cancelAllTtsRequests()
        val index = if(isFirst) trip.index else trip.index+1
        if(index == locations.size){
            robot.removeOnGoToLocationStatusChangedListener(trip)
            robot.removeTtsListener(speaking)
            val eval = EvalScreen(context, robot)
            eval.initScreen()
        } else {
            trip.index = index
            bar.progress = trip.index * 100 / locations.size
            val request = database.getTextsOfTransfer(locations[trip.index], isAusführlich)
            if(request.all { it.isNotBlank() }){
                updateText((request))
                val ttsRequest = TtsRequest.create(
                    speech = request[0],
                    isShowOnConversationLayer = false)
                robot.speak(ttsRequest)
            } else {
                updateText(database.getTextsOfLocation(locations[trip.index], isAusführlich))
            }
            trip.queue.add(database.getTextsOfLocation(locations[trip.index], isAusführlich))
            robot.goTo(locations[trip.index], false)
        }

    }


    private fun hasFinishedSpeaking() {
        if(speaking.lastStatus == TtsRequest.Status.COMPLETED){
            val ttsRequest = TtsRequest.create(
                speech = context.findViewById<TextView>(R.id.text_view)?.text.toString(),
                isShowOnConversationLayer = false)
            robot.speak(ttsRequest)
        }
        return
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun progressTour(){
        if(!(trip.lastLocationStatus == OnGoToLocationStatusChangedListener.COMPLETE &&
            speaking.lastStatus ==  TtsRequest.Status.COMPLETED))return
        speaking.lastStatus = TtsRequest.Status.STARTED

        GlobalScope.launch {
                withContext(Dispatchers.Main) {
                    while(true) {
                        delay(10000)
                        if(youtubeHandlers.all{ !it.isRunning} ){
                        youtubeHandlers.clear()

                        if(trip.queue.size > 0 ){
                            val next  = trip.queue.removeAt(0)
                            updateText(next)
                            val ttsRequest = TtsRequest.create(
                                speech = next[0],
                                isShowOnConversationLayer = false)
                            robot.speak(ttsRequest)
                        } else {
                            continueTour()
                        }
                            break
                    }
                }
            }
        }
    }

    private fun tryAgain(){
        if(speaking.lastStatus === TtsRequest.Status.COMPLETED) {
            val ttsRequest = TtsRequest.create(
                speech = context.findViewById<TextView>(R.id.text_view)?.text.toString(),
                isShowOnConversationLayer = false)
            robot.speak(ttsRequest)
        }
        robot.goTo(locations[trip.index])
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun loadImages(id: String){
        val images = context.findViewById<LinearLayout>(R.id.img)
        val pics = database.getMediaOfText(id)
        images.removeAllViews()
        for (url in pics) {
            if(url.contains("youtube.com/")){
                val youtubePlayerView = YouTubePlayerView(context)

                val widthInPixels = 1066
                val heightInPixels = 600
                val layoutParams = ViewGroup.LayoutParams(widthInPixels, heightInPixels)
                layoutParams.width = widthInPixels
                layoutParams.height = heightInPixels
                youtubePlayerView.layoutParams = layoutParams
                youtubePlayerView.enableAutomaticInitialization= false
                val listener: YouTubePlayerListener = object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        youTubePlayer.cueVideo(getID(url),0f)
                        youtubeHandlers.add(YoutubePlayerListener())
                        youTubePlayer.addListener(youtubeHandlers[youtubeHandlers.size - 1])
                    }
                }
                youtubePlayerView.initialize(listener)
                images.addView(youtubePlayerView)
            } else {
                val imageView = ImageView(context)

                val layoutParams = LinearLayout.LayoutParams(
                    600,
                    600
                )
                imageView.layoutParams = layoutParams

                CoroutineScope(Dispatchers.IO).launch {
                    val bitmap = downloadImage(url)
                    withContext(Dispatchers.Main) {
                        imageView.setImageBitmap(bitmap)
                        images.addView(imageView)
                    }
                }
            }
        }

    }
}
