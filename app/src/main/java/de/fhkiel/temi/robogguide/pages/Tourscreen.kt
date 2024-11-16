package de.fhkiel.temi.robogguide.pages

import GoingBackDialogue
import YoutubeVideoDialogue
import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.view.ViewGroup
import android.widget.Button
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
import de.fhkiel.temi.robogguide.helper.speak
import de.fhkiel.temi.robogguide.helper.speakCurrent
import de.fhkiel.temi.robogguide.media.YoutubePlayerListener
import de.fhkiel.temi.robogguide.media.getID
import de.fhkiel.temi.robogguide.media.downloadImage
import de.fhkiel.temi.robogguide.triplogic.MovementHandler
import de.fhkiel.temi.robogguide.triplogic.Speaker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant

/**
 * Handles the Tour.
 */
class Tourscreen(private val context: Activity,
                 private val robot: Robot,
                 private val handleInitScreen: () -> Unit,
                 private val locations : List<String>,
                 private val isAusführlich: Boolean = false,
                 ) {
    private val database = DatabaseHandler.getDb()!!
    private val movementHandler = MovementHandler(robot, 0, locations, context, ::handleBackAction, ::tryAgain, ::proceedToNextStop, ::continueTourWhenReady, ::handleSpeechInterrupt)
    private val bar: ProgressBar
    private val speaker = Speaker(::continueTourWhenReady)
    private val youtubeHandlers: MutableList<YoutubePlayerListener>  = mutableListOf()
    private var lastTimeStamp = Instant.now()
    private var isAlertDisplayed = false
    private lateinit var pics: Array<String>


    init {
        context.setContentView(R.layout.tour_screen)
        bar = context.findViewById(R.id.progressBar)
        robot.addTtsListener(speaker)
    }

    /**
     * Init the tour screen
     */
    @SuppressLint("SetJavaScriptEnabled")
    fun initializeTourScreen() {

        robot.addOnGoToLocationStatusChangedListener(movementHandler)
        //begin the tour
        proceedToNextStop(true)

        val backButton = context.findViewById<ImageButton>(R.id.backbutton)

        //Create dialog on back button.
        backButton.setOnClickListener {

            //stop whatever the robot is doing
            movementHandler.isWantedInterrupt = true
            robot.stopMovement()
            robot.cancelAllTtsRequests()
            movementHandler.isPaused = true

            //create dialog
            val goingBackDialogue = GoingBackDialogue(context)
            goingBackDialogue.show()
            goingBackDialogue.setOnDismissListener { isAlertDisplayed = false }
            isAlertDisplayed = true

            //repeat this location.
            goingBackDialogue.findViewById<Button>(R.id.thisLocation).setOnClickListener{
                movementHandler.queue.clear()
                proceedToNextStop(true, true)

                movementHandler.isPaused = false
                val pauseButton =  context.findViewById<ImageButton>(R.id.pausebutton)
                pauseButton.setImageResource(R.drawable.pause)

                goingBackDialogue.dismiss()
                isAlertDisplayed = false
                movementHandler.isWantedInterrupt = false

            }

            // go to previous location
            goingBackDialogue.findViewById<Button>(R.id.lastLocation).setOnClickListener{
                movementHandler.queue.clear()
                movementHandler.index = (movementHandler.index - 1).coerceAtLeast(0)
                proceedToNextStop(true)

                movementHandler.isPaused = false
                val pauseButton =  context.findViewById<ImageButton>(R.id.pausebutton)
                pauseButton.setImageResource(R.drawable.pause)

                goingBackDialogue.dismiss()
                isAlertDisplayed = false
                movementHandler.isWantedInterrupt = false

            }

            // Ending the tour
            goingBackDialogue.findViewById<Button>(R.id.Leave).setOnClickListener{
                movementHandler.isPaused = false
                val pauseButton =  context.findViewById<ImageButton>(R.id.pausebutton)
                pauseButton.setImageResource(R.drawable.pause)
                handleBackAction()
                goingBackDialogue.dismiss()
                movementHandler.isWantedInterrupt = false

            }
        }

        val continueButton = context.findViewById<ImageButton>(R.id.continuebutton)
        continueButton.setOnClickListener{
            processTourQueue(true)
        }


        //pausing the tour also has edge cases
        val pauseButton =  context.findViewById<ImageButton>(R.id.pausebutton)
        pauseButton.setOnClickListener{
            movementHandler.isPaused  = !movementHandler.isPaused

            //unpause tour
            if(!movementHandler.isPaused){
                movementHandler.isWantedInterrupt = false
                if(speaker.isInterruptQueued){
                    speakCurrent(context, robot)
                }
                pauseButton.setBackgroundColor(Color.WHITE)
                pauseButton.setImageResource(R.drawable.pause)
                robot.goTo(locations[movementHandler.index], false)
                if(speaker.lastStatus != TtsRequest.Status.COMPLETED) speakCurrent(context, robot)
                // Pause tour
            } else {
                movementHandler.isWantedInterrupt = true
                lastTimeStamp = Instant.now()
                if(speaker.lastStatus !== TtsRequest.Status.COMPLETED)speaker.isInterruptQueued = true
                asyncGoBackTask()
                robot.cancelAllTtsRequests()
                robot.stopMovement()
                pauseButton.setImageResource(R.drawable.unpause)
            }
        }
    }


    // cancel everything when the screen is left.
    private fun handleBackAction() {
        handleInitScreen()
        robot.removeOnGoToLocationStatusChangedListener(movementHandler)
        robot.removeTtsListener(speaker)
        robot.goTo(Routes.start, false)
        speak(robot,  "Okay! Ich gehe dann wieder zum Anfang. Viel Spaß im Museum!")
    }

    //Loading the images, videos and texts.
    private fun updateText(information: List<String>){

        context.findViewById<TextView>(R.id.text_view)?.text = information[0]
            context.findViewById<TextView>(R.id.title_view)?.text = information[1]
            loadImages(information[2])
    }


    private fun proceedToNextStop(isFirst: Boolean = false, isTriggeredByAgainButton: Boolean = false) {
        //setup
        movementHandler.queue.clear()
        if(!isTriggeredByAgainButton)movementHandler.lastLocationStatus = "ABORT"
        movementHandler.isPaused = false
        speaker.lastStatus = TtsRequest.Status.STARTED
        speaker.hasInformed = false
        robot.cancelAllTtsRequests()

        movementHandler.index = (if(isFirst) movementHandler.index else movementHandler.index+1)

        // is the tour over?
        if(movementHandler.index == locations.size){
            robot.removeOnGoToLocationStatusChangedListener(movementHandler)
            robot.removeTtsListener(speaker)
            val eval = EvalScreen(context, robot)
            eval.initScreen()
        } else {
            bar.progress = movementHandler.index * 100 / locations.size

            val request = database.getTextsOfTransfer(locations[movementHandler.index], isAusführlich)
            //display the text of the location
            if(!isTriggeredByAgainButton && request.all { it.isNotBlank() }){
                updateText(request)
                speak(robot, request[0])
            } else {
                //if the transfer is empty show the text of the location.
                updateText(database.getTextsOfLocation(locations[movementHandler.index], isAusführlich))
                speaker.lastStatus = TtsRequest.Status.COMPLETED
            }
            //Add things to the queue.
            movementHandler.queue.add(database.getTextsOfLocation(locations[movementHandler.index], isAusführlich))
            movementHandler.queue.addAll(database.getTextsOfItems(locations[movementHandler.index], isAusführlich))
            // start walking.
        if(!isTriggeredByAgainButton)robot.goTo(locations[movementHandler.index], false)
        else {processTourQueue(true)}
        }
    }


    // if the speaking was interrupted wait until the speaker is done and than repeat what you tried to say before.
    @OptIn(DelicateCoroutinesApi::class)
    private fun handleSpeechInterrupt() {
        if(speaker.lastStatus != TtsRequest.Status.COMPLETED &&
            !speaker.isInterruptQueued){
            speaker.isInterruptQueued = true
            GlobalScope.launch {
                withContext(Dispatchers.Main) {
                    while (true){
                        delay(200)
                        if(isAlertDisplayed){
                            break
                        }
                        if(speaker.lastStatus === TtsRequest.Status.COMPLETED ||
                        youtubeHandlers.any{ it.isRunning}) {
                        speakCurrent(context,robot)
                        speaker.isInterruptQueued = false
                        break
                        }
                    }
                }
            }
        }
    }

    //if the tour gets abandoned and paused say some things and then go back.
    @OptIn(DelicateCoroutinesApi::class)
    fun asyncGoBackTask() {
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                var step = 0
                while (true) {
                    delay(2000)
                    if (!movementHandler.isPaused || context.findViewById<ImageButton>(R.id.backbutton) == null) break
                    if (step == 0) {
                        if (lastTimeStamp.plusSeconds(540).isAfter(Instant.now())) continue
                        speak(robot, "Hallo, sind sie noch hier?")
                        step++
                    }
                    if (step == 1) {
                        if (lastTimeStamp.plusSeconds(600).isAfter(Instant.now())) continue
                        speak(
                            robot,
                            "Ich würde gleich zurück fahren wollen, bitte drücken sie auf weiter, um zur nächsten Station zu gehen."
                        )
                        step++
                    } else {
                        if (lastTimeStamp.plusSeconds(660).isAfter(Instant.now())) continue
                        handleBackAction()
                        break
                    }
                }
            }
        }
    }

    //continue the tour when it stopped moving and stopped talking.
    @OptIn(DelicateCoroutinesApi::class)
    private fun continueTourWhenReady(){
        if (movementHandler.isPaused) return
        if (!(movementHandler.lastLocationStatus == OnGoToLocationStatusChangedListener.COMPLETE &&
                        speaker.lastStatus == TtsRequest.Status.COMPLETED)) return

        GlobalScope.launch {
                withContext(Dispatchers.Main) {
                    while(true) {
                        delay(if(!movementHandler.hasMovedRecently) 8000 else 2000)
                        if(!(movementHandler.lastLocationStatus == OnGoToLocationStatusChangedListener.COMPLETE &&
                                    speaker.lastStatus == TtsRequest.Status.COMPLETED)) break
                        speaker.lastStatus = TtsRequest.Status.STARTED

                        if(movementHandler.isPaused) continue
                        if(youtubeHandlers.any{ it.isRunning} ) continue
                        youtubeHandlers.clear()
                        processTourQueue()
                        break
                }
            }
        }
    }

    private fun tryAgain(){
        if(speaker.lastStatus === TtsRequest.Status.COMPLETED) {
            speakCurrent(context, robot)
        }
        movementHandler.hasMovedRecently = true
        robot.goTo(locations[movementHandler.index])
    }

    /**
     * tour goes through the queue. if the last thing was said it announces that it will move soon.
     * if it doesnt get paused it will then try to proceed to the next stop of the tour or end the
     * if all stations have been visited.
     */
    @OptIn(DelicateCoroutinesApi::class)
    private fun processTourQueue(isManual :Boolean = false) {
        speaker.lastStatus = TtsRequest.Status.STARTED
        if(movementHandler.queue.size > 0 ){
                val next  = movementHandler.queue.removeAt(0)
                updateText(next)
                speak(robot, next[0])
            } else {
                val cIndex = movementHandler.index
            if(isManual || movementHandler.index == locations.size - 1){
                proceedToNextStop()
            } else if(!speaker.hasInformed){
            GlobalScope.launch {
                withContext(Dispatchers.Main) {
                    while (true) {
                        delay(200)
                        if(isAlertDisplayed || movementHandler.isPaused || youtubeHandlers.any{ it.isRunning}) continue
                        break
                    }
                    val next = listOf("Wenn ihr hier noch bleiben wollt, drückt auf Den Pause Knopf.", "Ende dieser Station", "-1")
                    speaker.hasInformed = true

                    speak(robot, next[0])
                    val t= Instant.now().plusSeconds(8)
                    if(cIndex == movementHandler.index) {
                        while(true){
                            delay(200)
                            if(cIndex != movementHandler.index || isAlertDisplayed) break
                            if( movementHandler.isPaused) continue
                            if(t.isAfter(Instant.now())) continue
                            proceedToNextStop()
                            break
                        }
                    }
                }
            }
        }
    }
}


    // load the media.
    @SuppressLint("SetJavaScriptEnabled")
    private fun loadImages(id: String){
        val images = context.findViewById<LinearLayout>(R.id.img)
        pics = database.getMediaOfText(id)

        images?.removeAllViews()
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
                imageView.setOnClickListener{
                    lastTimeStamp = Instant.now();
                }
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
                        imageView.setOnClickListener {
                            //this dialogue only shows for images.
                            val yt = YoutubeVideoDialogue(context)
                            yt.show()
                            isAlertDisplayed = true
                            yt.findViewById<ImageView>(R.id.mainImageView).setImageBitmap(bitmap)
                            yt.findViewById<ImageButton>(R.id.destroyImageView).setOnClickListener {
                                yt.dismiss()
                            }
                            yt.setOnDismissListener { isAlertDisplayed = false }
                        }
                    }
                }
            }
        }

    }
}
