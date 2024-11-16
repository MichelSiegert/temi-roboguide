package de.fhkiel.temi.robogguide

import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.robotemi.sdk.Robot
import com.robotemi.sdk.listeners.OnRobotReadyListener
import de.fhkiel.temi.robogguide.database.DatabaseHandler
import de.fhkiel.temi.robogguide.pages.InitialScreen



    class MainActivity : AppCompatActivity(), OnRobotReadyListener {
    private var mRobot: Robot? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.first_screen)
        // I use a singleton to handle the db instead.
        DatabaseHandler.init(this)
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
        DatabaseHandler.onDestroy()
    }

        //start the robot in the correct screen, and let it drive to the first location.
    override fun onRobotReady(isReady: Boolean) {
        if (isReady){
            mRobot = Robot.getInstance()
            mRobot?.toggleNavigationBillboard(true)
            mRobot?.hideTopBar()

            val activityInfo: ActivityInfo = packageManager.getActivityInfo(componentName, PackageManager.GET_META_DATA)
            Robot.getInstance().onStart(activityInfo)
            mRobot?.let { robot-> run {
                Routes.initialize(robot)
                robot.goTo(Routes.start)

                val initScreen = InitialScreen(this,  robot)
                 initScreen.handleInitScreen()
            } }

        }
    }
}