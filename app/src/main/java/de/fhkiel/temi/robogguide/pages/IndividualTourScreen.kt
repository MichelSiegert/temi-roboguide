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

class IndividualTourScreen(
                        private val activity: Activity,
                        private val mRobot: Robot,
                        private val handleInitScreen: () -> Unit) {
    private var isAusfuhrlich = false

    fun handleIndivTourScreen() {
        activity.setContentView(R.layout.individual_tour_planner)

        val layout = activity.findViewById<GridLayout>(R.id.indiv_grid)
        val locationToggleManager = LocationToggleManager(activity, mRobot)
        locationToggleManager.populateLocationToggles(layout)

        val back = activity.findViewById<ImageButton>(R.id.backbuttonindiv)
        back.setOnClickListener {
            handleInitScreen()
        }
        val detailedButton  =activity.findViewById<Button>(R.id.isadvbutton)
        detailedButton.setOnClickListener{
            if(isAusfuhrlich) detailedButton.setBackgroundColor(Color.LTGRAY)
                else detailedButton.setBackgroundColor(Color.GREEN)
            isAusfuhrlich = !isAusfuhrlich
        }

        val unselectAll = activity.findViewById<Button>(R.id.unselectall)
        unselectAll.setOnClickListener{ layout.forEach { if(it.tag == 1){it.performClick() } } }

        val selectAll = activity.findViewById<Button>(R.id.selectAll)
        selectAll.setOnClickListener{ layout.forEach { if(it.tag == 0){it.performClick() } }
        }

        val startTour: ImageButton = activity.findViewById(R.id.start_individual_tour)
        startTour.setOnClickListener{
            val customRoute = Routes.route.filter { locationToggleManager.toggledList.contains( it) }
            Log.i("CustomRoute", customRoute.joinToString { "$it, " })
            if(customRoute.isEmpty()) {
                Toast.makeText(activity, "Man muss mindestens 1 Station Auswählen!", Toast.LENGTH_LONG).show()
            } else {
                val tour = Tourscreen(
                    activity,
                    mRobot,
                    handleInitScreen,
                    isAusführlich = isAusfuhrlich,
                    customRoute
                )
                tour.initializeTourScreen()
            }
        }
    }
}