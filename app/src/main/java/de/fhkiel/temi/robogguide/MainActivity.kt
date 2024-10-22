package de.fhkiel.temi.robogguide

import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.robotemi.sdk.Robot
import com.robotemi.sdk.listeners.OnRobotReadyListener
import de.fhkiel.temi.robogguide.database.DatabaseHelper
import de.fhkiel.temi.robogguide.pages.InitialScreen
import java.io.IOException


// ADB Connect (ip address)
    class MainActivity : AppCompatActivity(), OnRobotReadyListener {
    private var mRobot: Robot? = null
    private lateinit var database: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.first_screen)

        // use database
        val databaseName = "roboguide.db"
        database = DatabaseHelper(this, databaseName)

        try {
            database.initializeDatabase() // Initialize the database and copy it from assets

        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    override fun onStart() {
        super.onStart()
        Robot.getInstance().addOnRobotReadyListener(this)
    }


    override fun onStop() {
        super.onStop()
        Robot.getInstance().removeOnRobotReadyListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        database.closeDatabase()
    }

    override fun onRobotReady(isReady: Boolean) {
        if (isReady){
            mRobot = Robot.getInstance()
            mRobot?.toggleNavigationBillboard(true)

            mRobot?.hideTopBar()

            val activityInfo: ActivityInfo = packageManager.getActivityInfo(componentName, PackageManager.GET_META_DATA)
            Robot.getInstance().onStart(activityInfo)
            mRobot?.let { robot-> run {

                Log.i("Robot", database.getTextsOfLocation(robot.locations[0], false)[0].toString())
                val initScreen = InitialScreen(this,  robot, database)
                 initScreen.handleInitScreen()
            } }

        }
    }
}