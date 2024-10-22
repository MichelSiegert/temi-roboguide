package de.fhkiel.temi.robogguide.pages

import android.app.Activity
import android.widget.Button
import com.robotemi.sdk.Robot
import de.fhkiel.temi.robogguide.R
import de.fhkiel.temi.robogguide.database.DatabaseHelper


class InitialScreen(
    private val context: Activity,
    private val robot: Robot,
    private val database: DatabaseHelper) {

    fun handleInitScreen() {

        val map = database.getLocationMap();
        val route = calculateRoute(map)

        val importantRoute = (calculateImportantRoute(route.toTypedArray()))
        context.setContentView(R.layout.first_screen)

        context.findViewById<Button>(R.id.individual)?.setOnClickListener {
            robot.let {
                val indivTour = IndividualTourScreen(context, it, database,  ::handleInitScreen, route.toTypedArray())
                indivTour.handleIndivTourScreen()
            }
        }

        context.findViewById<Button>(R.id.easylong).setOnClickListener {
            val tourScreen = Tourscreen(context, robot, ::handleInitScreen, database, false, route)
            tourScreen.handleTourScreen()
        }

        context.findViewById<Button>(R.id.easyshort).setOnClickListener {
            val tourScreen = Tourscreen(context, robot, ::handleInitScreen,database,  false, importantRoute)
            tourScreen.handleTourScreen()
        }

        context.findViewById<Button>(R.id.advancedlong).setOnClickListener {
            val tourScreen = Tourscreen(context, robot, ::handleInitScreen, database, true, route)
            tourScreen.handleTourScreen()
        }

        context.findViewById<Button>(R.id.advancedshort).setOnClickListener {
            val tourScreen = Tourscreen(context, robot, ::handleInitScreen, database, true, importantRoute)
            tourScreen.handleTourScreen()
        }
    }

    private fun calculateRoute(map: Map<String, String>): List<String> {
        var transfers  =database.getAllTransfers()
        transfers.shuffle()
        transfers = transfers.map { Pair(map[it.first]!!, map[it.second]!!) }.filter { robot.locations.contains(it.first) ||  robot.locations.contains(it.second)  }.toTypedArray()
        val locationToSet = transfers.map { it.second }.toSet()
        val startLocation = transfers.map { it.first }
            .filterNot { locationToSet.contains(it) }[0]

        val tmp = transfers.map { it.first }.toSet()
        val endLocation = transfers.map { it.second }
            .filterNot { tmp.contains(it) }[0]


        var route = arrayOf(startLocation)

        while (route[route.size-1] != endLocation){
            val current = route[route.size - 1]
            val next = (transfers.find { it.first == current })!!.second

            route += next
        }
        return route.toList()
    }

    private fun calculateImportantRoute(route: Array<String>): List<String> {
        val locations  =database.getAllImportantStations()
        val importantRoute = route.filter { locations.contains(it) }
        return importantRoute
    }
}
