package de.fhkiel.temi.robogguide.pages

import android.app.Activity
import android.widget.ImageButton
import com.robotemi.sdk.Robot
import com.robotemi.sdk.TtsRequest
import de.fhkiel.temi.robogguide.R
import de.fhkiel.temi.robogguide.database.DatabaseHandler
import de.fhkiel.temi.robogguide.evaluation.Evaluation
import de.fhkiel.temi.robogguide.evaluation.saveToFile

class EvalScreen(
    private val context: Activity,
    private val robot: Robot,
){
    private val database = DatabaseHandler.getDb()!!

    fun initScreen(){
        context.setContentView(R.layout.eval_screen)
        val ttsRequest = TtsRequest.create(
            speech = "Würdest du mir bitte noch feedback zu der Führung geben?",
            isShowOnConversationLayer = false
        )
        robot.speak(ttsRequest)
        context.findViewById<ImageButton>(R.id.goodbutton).setOnClickListener{ evaluate(Evaluation.GUT) }
        context.findViewById<ImageButton>(R.id.okbutton).setOnClickListener{ evaluate(Evaluation.NEUTRAL) }
        context.findViewById<ImageButton>(R.id.badButton).setOnClickListener{ evaluate(Evaluation.SCHLECHT) }
    }

    fun evaluate(eval: Evaluation) {
        val ttsRequest = TtsRequest.create(
            speech = "Okay! Danke für dein Feedback! Viel Spaß im Museum!",
            isShowOnConversationLayer = false
        )
        robot.speak(ttsRequest)
        saveToFile(context, eval.toString().lowercase() + '\n')
        val initScreen = InitialScreen(context,  robot)
        initScreen.handleInitScreen()
        robot.goTo("home base")
    }
}