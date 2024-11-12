package de.fhkiel.temi.robogguide.pages

import android.app.Activity
import android.widget.ImageButton
import com.robotemi.sdk.Robot
import com.robotemi.sdk.TtsRequest
import de.fhkiel.temi.robogguide.R
import de.fhkiel.temi.robogguide.Routes
import de.fhkiel.temi.robogguide.evaluation.Evaluation
import de.fhkiel.temi.robogguide.evaluation.saveToFile
import de.fhkiel.temi.robogguide.helper.speak
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant

class EvalScreen(
    private val context: Activity,
    private val robot: Robot,
){
    var step =0
    private var lastTimeStamp = Instant.now()

    fun initScreen(){
        asyncGoBackTask()
        context.setContentView(R.layout.eval_screen)
        val ttsRequest = TtsRequest.create(
            speech = "Okay, das war die gesamte Tour. Ich hoffe es hat Ihnen gefallen." +
                    "Würdest sie mir bitte noch Feedback zu der Führung geben?",
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

    @OptIn(DelicateCoroutinesApi::class)
    fun asyncGoBackTask() {
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                step = 0
                while (true) {
                    delay(2000)
                    if (context.findViewById<ImageButton>(R.id.goodbutton) === null) break
                    if (step == 0) {
                        if (lastTimeStamp.plusSeconds(30).isAfter(Instant.now())) continue
                        speak(robot, "Bitte geben sie eine Bewertung.")
                        step++
                    }
                    if (step == 1) {
                        if (lastTimeStamp.plusSeconds(60).isAfter(Instant.now())) continue
                        speak(
                            robot,
                            "Ich würde gleich wieder zurück zum anfang gehen, klicken sie bitte auf einen der Smileys, um eine Bewertung abzugeben."
                        )
                        step++
                    } else {
                        if (lastTimeStamp.plusSeconds(90).isAfter(Instant.now())) continue
                        val initScreen = InitialScreen(context, robot)
                        initScreen.handleInitScreen()
                        robot.goTo(Routes.start)
                        break
                    }
                }
            }
        }
    }
}
