package de.fhkiel.temi.robogguide.triplogic

import FailedPathingDialogue
import android.app.Activity
import android.widget.Button
import android.widget.Toast
import com.robotemi.sdk.Robot
import com.robotemi.sdk.TtsRequest
import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener
import de.fhkiel.temi.robogguide.R

class MovementHandler(
    private val mRobot: Robot,
    var index: Int = 0,
    locations: List<String>,
    private val activity: Activity,
    private val backFunction: () -> Any,
    private val tryAgainFunction: () -> Any,
    private val continueFunction: () -> Any,

    private val tourProgress: () -> Any,
    private val fisnished: ()-> Any
) : OnGoToLocationStatusChangedListener {

    var isPaused = false
    var hasMovedRecently = true
    var queue = mutableListOf<List<String>>()
    var isWantedInterrupt = false
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
        if(!isWantedInterrupt &&(descriptionId == 1003 || descriptionId == 1004 || descriptionId == 1005))  {
            mRobot.cancelAllTtsRequests()
            mRobot.stopMovement()

            val customDialog = FailedPathingDialogue(activity)
            customDialog.show()

            customDialog.findViewById<Button>(R.id.dialogYes)?.setOnClickListener{
                backFunction()
                customDialog.dismiss()
            }
            customDialog.findViewById<Button>(R.id.dialogTryAgain)?.setOnClickListener {
                tryAgainFunction()
                customDialog.dismiss()
            }
            customDialog.findViewById<Button>(R.id.dialogSkip)?.setOnClickListener{
                continueFunction()
                customDialog.dismiss()
            }
        }
        if(descriptionId in 2000..2009){
            Toast.makeText(activity, "Entschuldigung, hier komme ich nicht durch!", Toast.LENGTH_LONG).show()
            val ttsRequest: TtsRequest = TtsRequest.create(speech = "Entschuldigung, hier komme ich nicht durch!", isShowOnConversationLayer = false)
            mRobot.speak(ttsRequest)
            fisnished()
        }
        if(status == OnGoToLocationStatusChangedListener.COMPLETE) {
            tourProgress()
        }
    }
}