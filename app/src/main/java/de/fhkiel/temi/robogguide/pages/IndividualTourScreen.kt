package de.fhkiel.temi.robogguide.pages

import android.app.Activity
import android.graphics.Color
import android.util.Log
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.view.forEach
import com.robotemi.sdk.Robot
import de.fhkiel.temi.robogguide.LocationToggleManager
import de.fhkiel.temi.robogguide.R
import de.fhkiel.temi.robogguide.Routes
import de.fhkiel.temi.robogguide.helper.speak
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant

class IndividualTourScreen(
                        private val activity: Activity,
                        private val mRobot: Robot,
                        private val handleInitScreen: () -> Unit) {
    private var isAusführlich = false
    private var lastTimeStamp = Instant.now()
    private var step =0

    fun updateTimestamp(){
        lastTimeStamp = Instant.now()
        step=0

        Log.i("weee",lastTimeStamp.toString())
    }

    fun handleIndivTourScreen() {
        activity.setContentView(R.layout.individual_tour_planner)
        asyncGoBackTask()
        val layout = activity.findViewById<GridLayout>(R.id.indiv_grid)
        val locationToggleManager = LocationToggleManager(activity, mRobot)
        locationToggleManager.populateLocationToggles(layout, ::updateTimestamp)

        val back = activity.findViewById<ImageButton>(R.id.backbuttonindiv)
        back.setOnClickListener {
            handleInitScreen()
        }
        val detailedButton  =activity.findViewById<Button>(R.id.isadvbutton)
        detailedButton.setOnClickListener{
            if(isAusführlich) detailedButton.setBackgroundColor(Color.LTGRAY)
                else detailedButton.setBackgroundColor(Color.GREEN)
            isAusführlich = !isAusführlich
        }

        val unselectAll = activity.findViewById<Button>(R.id.unselectall)
        unselectAll.setOnClickListener{ layout.forEach { if(it.tag == 1){it.performClick() } } }

        val selectAll = activity.findViewById<Button>(R.id.selectAll)
        selectAll.setOnClickListener{ layout.forEach { if(it.tag == 0){it.performClick() } }
        }

        val startTour: ImageButton = activity.findViewById(R.id.start_individual_tour)
        startTour.setOnClickListener{
            val customRoute = Routes.route.filter { locationToggleManager.toggledList.contains( it) }
            if(customRoute.isEmpty()) {
                Toast.makeText(activity, "Man muss mindestens 1 Station Auswählen!", Toast.LENGTH_LONG).show()
            } else {
                val tour = Tourscreen(
                    activity,
                    mRobot,
                    handleInitScreen,
                    isAusführlich = isAusführlich,
                    customRoute
                )
                tour.initializeTourScreen()
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun asyncGoBackTask(){

        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                step = 0
                while(true){
                    delay(2000)
                    if(activity.findViewById<ImageButton>(R.id.backbuttonindiv) === null)break
                    if(step == 0)  {
                        if(lastTimeStamp.plusSeconds(120).isAfter(Instant.now()))continue
                        speak(mRobot,"Hallo? ist noch wer hier?")
                        step++
                    }
                    if(step == 1)  {
                        if(lastTimeStamp.plusSeconds(180).isAfter(Instant.now()))continue
                        speak(mRobot,"Ich würde gleich wieder zurück zum anfang gehen, klickt bitte auf eines der Bilder, oder startet eine Tour.")
                        step++
                    } else {
                        if(lastTimeStamp.plusSeconds(210).isAfter(Instant.now()))continue
                        handleInitScreen()
                        mRobot.goTo(Routes.start)
                        break
                    }

                }
            }

        }
    }
}