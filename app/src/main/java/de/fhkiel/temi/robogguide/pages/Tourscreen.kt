package de.fhkiel.temi.robogguide.pages

import android.app.Activity
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.robotemi.sdk.Robot
import com.robotemi.sdk.TtsRequest
import de.fhkiel.temi.robogguide.R
import de.fhkiel.temi.robogguide.database.DatabaseHelper
import de.fhkiel.temi.robogguide.triplogic.RoundTrip

class Tourscreen(private val context: Activity,
                 private val robot: Robot,
                 private val handleInitScreen: () -> Unit,
                 private val database: DatabaseHelper,
                 private val isAusführlich: Boolean = false,
                 private val locations : List<String> = robot.locations,
) {
     fun handleTourScreen() {
        context.setContentView(R.layout.tour_screen)
        val bar = context.findViewById<ProgressBar>(R.id.progressBar)

        val trip = RoundTrip(robot, 0, locations, context)
        robot.addOnGoToLocationStatusChangedListener(trip)
         updateText(trip);


         val backButton = context.findViewById<Button>(R.id.backbutton)
        backButton.setOnClickListener {
            handleBackAction(trip)
            handleInitScreen()
        }

        val continueButton = context.findViewById<Button>(R.id.continuebutton)
        continueButton.setOnClickListener{

            robot.cancelAllTtsRequests();
            val index = trip.index+1
            if(index == locations.size){
                //end screen I guess?
            }
            trip.index = index % locations.size
            bar.progress = trip.index * 100 / locations.size
            robot.goTo(locations[trip.index]);
            updateText(trip)
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
        val textPair = database.getTextsOf(locations[trip.index], isAusführlich)
        context.findViewById<TextView>(R.id.text_view).text = textPair[0].first
        context.findViewById<TextView>(R.id.title_view).text = textPair[0].second
    }
}