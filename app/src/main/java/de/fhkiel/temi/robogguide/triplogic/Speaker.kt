package de.fhkiel.temi.robogguide.triplogic

import com.robotemi.sdk.Robot
import com.robotemi.sdk.TtsRequest
// Handles the things related to the speech during the tour.
//mostly holds variables that are changed in the tourscreen.
class Speaker (private val handleUpdate: () -> Unit): Robot.TtsListener  {
    var lastStatus = TtsRequest.Status.COMPLETED
    var hasInformed = false
    var isInterruptQueued = false
    override fun onTtsStatusChanged(ttsRequest: TtsRequest) {
        lastStatus = ttsRequest.status

        if(ttsRequest.status != TtsRequest.Status.COMPLETED) {
            return
        }
        handleUpdate()
    }
}
