package de.fhkiel.temi.robogguide.helper

import android.app.Activity
import android.widget.TextView
import com.robotemi.sdk.Robot
import com.robotemi.sdk.TtsRequest
import de.fhkiel.temi.robogguide.R

fun speak(robot: Robot, text: String){
    val ttsRequest = TtsRequest.create(
        speech = text,
        isShowOnConversationLayer = false)
    robot.speak(ttsRequest)
}

fun speakCurrent(context: Activity, robot: Robot){
    val ttsRequest = TtsRequest.create(
        speech = context.findViewById<TextView>(R.id.text_view)?.text.toString(),
        isShowOnConversationLayer = false)
    robot.speak(ttsRequest)
}