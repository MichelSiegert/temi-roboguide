package de.fhkiel.temi.robogguide.triplogic

import com.robotemi.sdk.Robot
import com.robotemi.sdk.TtsRequest

class Speaker (private val handleUpdate: () -> Unit): Robot.TtsListener  {
    var lastStatus = TtsRequest.Status.COMPLETED
    override fun onTtsStatusChanged(ttsRequest: TtsRequest) {
        lastStatus = ttsRequest.status
        handleUpdate()

    }
}
