package de.fhkiel.temi.robogguide.pages

import android.app.Activity
import android.widget.Button
import com.robotemi.sdk.Robot
import de.fhkiel.temi.robogguide.R
import de.fhkiel.temi.robogguide.Routes

class InitialScreen(
    private val context: Activity,
    private val robot: Robot) {

    fun handleInitScreen() {
        context.setContentView(R.layout.first_screen)
        context.findViewById<Button>(R.id.individual)?.setOnClickListener {
                val indivTour = IndividualTourScreen(context, robot,  ::handleInitScreen)
                indivTour.handleIndivTourScreen()
        }

        context.findViewById<Button>(R.id.easylong).setOnClickListener {
            val tourScreen = Tourscreen(context, robot, ::handleInitScreen, false, Routes.route)
            tourScreen.initializeTourScreen()
        }

        context.findViewById<Button>(R.id.easyshort).setOnClickListener {
            val tourScreen = Tourscreen(context, robot, ::handleInitScreen,  false, Routes.importantRoute)
            tourScreen.initializeTourScreen()
        }

        context.findViewById<Button>(R.id.advancedlong).setOnClickListener {
            val tourScreen = Tourscreen(context, robot, ::handleInitScreen, true, Routes.route)
            tourScreen.initializeTourScreen()
        }

        context.findViewById<Button>(R.id.advancedshort).setOnClickListener {
            val tourScreen = Tourscreen(context, robot, ::handleInitScreen, true, Routes.importantRoute)
            tourScreen.initializeTourScreen()
        }
    }
}
