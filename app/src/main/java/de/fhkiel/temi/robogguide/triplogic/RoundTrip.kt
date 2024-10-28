package de.fhkiel.temi.robogguide.triplogic

import android.app.Activity
import android.app.AlertDialog
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.robotemi.sdk.Robot
import com.robotemi.sdk.TtsRequest
import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener
import de.fhkiel.temi.robogguide.R

class RoundTrip(
    private val mRobot: Robot,
    var index: Int =0,
    locations: List<String>,
    private val activity: Activity,
    private val backFunction: () -> Any,
    private val tryAgainFunction: () -> Any,
    private val continueFunction: () -> Any,
    private val tourProgress: () -> Any
    ) : OnGoToLocationStatusChangedListener {
        var queue = mutableListOf<List<String>>()
    var lastLocationStatus: String = OnGoToLocationStatusChangedListener.START


    init {
        mRobot.goTo(locations[0])
    }

    override fun onGoToLocationStatusChanged(
        location: String,
        status: String,
        descriptionId: Int,
        description: String) {
        lastLocationStatus = status
        if(status == OnGoToLocationStatusChangedListener.COMPLETE) {
            tourProgress()
            // val ttsRequest: TtsRequest = TtsRequest.create(speech = activity.findViewById<TextView>(R.id.text_view).text.toString(), isShowOnConversationLayer = false)
            // mRobot.speak(ttsRequest)
        }
        Log.i("Movement", "$descriptionId: $description, $status")
        if(descriptionId == 1003 || descriptionId == 1004 )  {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle("Confirm Action")
            builder.setMessage("Wie wollen Sie weiter Verfahren?")

            builder.setPositiveButton("Station überspringen") { _, _ -> continueFunction()}
            builder.setNeutralButton("Erneut versuchen") { _, _ -> tryAgainFunction()}

            builder.setNegativeButton("Tour Beenden") { _, _ -> backFunction() }

            // Create and show the dialog
            val alertDialog = builder.create()
            alertDialog.show()
        }
        if(descriptionId in 2000..2009){
            Toast.makeText(activity, "Entschuldigung, hier komme ich nicht durch!", Toast.LENGTH_LONG).show()
            val ttsRequest: TtsRequest = TtsRequest.create(speech = "Entschuldigung, hier komme ich nicht durch!", isShowOnConversationLayer = false)
            mRobot.speak(ttsRequest)
        }
         else if (descriptionId == 500) activity.findViewById<TextView>(R.id.error_text).text = ""

    }
}