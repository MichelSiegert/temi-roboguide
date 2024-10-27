package de.fhkiel.temi.robogguide

import android.app.Activity
import com.robotemi.sdk.Robot
import de.fhkiel.temi.robogguide.database.DatabaseHandler
import de.fhkiel.temi.robogguide.database.DatabaseHelper

object Routes {
    private var database:DatabaseHelper = DatabaseHandler.database!!
    lateinit var route: List<String>
    lateinit var importantRoute: List<String>

    fun initialize(robot: Robot) {
            route = calculateRoute(robot)
            importantRoute= calculateImportantRoute(route.toTypedArray())
    }

    private fun calculateRoute(robot:Robot): List<String> {

        val map = database.getLocationMap();
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