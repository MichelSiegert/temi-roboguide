package de.fhkiel.temi.robogguide.database

import android.content.Context
import android.util.Log
import java.io.IOException

/**
 * Creates a singleton of a database for the other processes, so It is easier to handle.
 */
object DatabaseHandler {
    var database: DatabaseHelper? = null

    fun init(context: Context ){
        if (database == null) {
            val databaseName = "roboguide.db"
            database = DatabaseHelper(context, databaseName)
            try {
                database!!.initializeDatabase()

            } catch (e: IOException) {
                e.printStackTrace()
            }
    }}

    fun getDb(): DatabaseHelper?{
        if(database !== null) return database!!
        return database
    }

    fun onDestroy(){
        database?.closeDatabase()
    }
}