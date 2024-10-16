package de.fhkiel.temi.robogguide.pages

import android.app.Activity
import android.widget.Button
import android.widget.LinearLayout
import com.robotemi.sdk.Robot
import de.fhkiel.temi.robogguide.LocationToggleManager
import de.fhkiel.temi.robogguide.R

class IndividualTourScreen(
                        private val activity: Activity,
                        private val mRobot: Robot,
                        private val handleInitScreen: () -> Unit) {

    public fun handleIndivTourScreen() {
        activity.setContentView(R.layout.individual_tour_planner)

        val layout = activity.findViewById<LinearLayout>(R.id.listoflocations)
        val locationToggleManager = LocationToggleManager(activity, mRobot)
        locationToggleManager.populateLocationToggles(layout)

        val back = activity.findViewById<Button>(R.id.backbuttonindiv)
        back.setOnClickListener {

            handleInitScreen()
        }

        val startTour: Button = activity.findViewById(R.id.start_individual_tour)
        startTour.setOnClickListener{
            val tour = Tourscreen(activity, mRobot, handleInitScreen, allStations = true, isAusführlich = true, locationToggleManager.toggledList, isIndividual= true)
            tour.handleTourScreen()
        }
    }
}