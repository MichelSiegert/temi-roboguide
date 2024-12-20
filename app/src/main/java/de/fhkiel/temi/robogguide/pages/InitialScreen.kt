package de.fhkiel.temi.robogguide.pages

import android.app.Activity
import android.widget.Button
import com.robotemi.sdk.Robot
import de.fhkiel.temi.robogguide.R
import de.fhkiel.temi.robogguide.Routes


/**
 * Renders the first screen
 */
class InitialScreen(
    private val context: Activity,
    private val robot: Robot) {


    /**
     * draws the first screen and sets the listeners of the buttons.
     */
    fun handleInitScreen() {
        context.setContentView(R.layout.first_screen)
        context.findViewById<Button>(R.id.individual)?.setOnClickListener {
                val indivTour = IndividualTourScreen(context, robot,  ::handleInitScreen)
                indivTour.handleIndivTourScreen()
        }

        context.findViewById<Button>(R.id.easylong).setOnClickListener {
            val tourScreen = Tourscreen(context, robot, ::handleInitScreen, Routes.route)
            tourScreen.initializeTourScreen()
        }

        context.findViewById<Button>(R.id.easyshort).setOnClickListener {
            val tourScreen = Tourscreen(context, robot, ::handleInitScreen, Routes.importantRoute)
            tourScreen.initializeTourScreen()
        }

        context.findViewById<Button>(R.id.advancedlong).setOnClickListener {
            val tourScreen = Tourscreen(context, robot, ::handleInitScreen, Routes.route, true)
            tourScreen.initializeTourScreen()
        }

        context.findViewById<Button>(R.id.advancedshort).setOnClickListener {
            val tourScreen = Tourscreen(context, robot, ::handleInitScreen, Routes.importantRoute, true)
            tourScreen.initializeTourScreen()
        }
    }
}
