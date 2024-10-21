package de.fhkiel.temi.robogguide.triplogic

import android.app.Activity
import android.util.Log
import android.widget.TextView
import com.robotemi.sdk.Robot
import com.robotemi.sdk.TtsRequest
import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener
import de.fhkiel.temi.robogguide.R

class RoundTrip(
    private val mRobot: Robot,
    var index: Int =0,
    private val locations: List<String> = mRobot.locations,
    private val activity: Activity
    ) : OnGoToLocationStatusChangedListener {

    init {
        mRobot.goTo(locations[0])
    }

    override fun onGoToLocationStatusChanged(
        location: String,
        status: String,
        descriptionId: Int,
        description: String) {
        if(status == OnGoToLocationStatusChangedListener.COMPLETE) {
            val ttsRequest: TtsRequest = TtsRequest.create(speech = activity.findViewById<TextView>(R.id.text_view).text.toString(), isShowOnConversationLayer = false);
            mRobot.speak(ttsRequest)
        }
        Log.i("Movement", "$descriptionId: $description, $status");
        if(descriptionId in 2000..2009){
            activity.findViewById<TextView>(R.id.error_text).text= description
            val ttsRequest: TtsRequest = TtsRequest.create(speech = "Entschuldigung, hier komme ich nicht durch!", isShowOnConversationLayer = false);
            mRobot.speak(ttsRequest)
        }
         else if (descriptionId == 500) activity.findViewById<TextView>(R.id.error_text).text = ""

    }
}