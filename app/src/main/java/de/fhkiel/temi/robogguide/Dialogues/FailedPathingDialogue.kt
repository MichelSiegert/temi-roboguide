import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import de.fhkiel.temi.robogguide.R

class FailedPathingDialogue(context: Context) : Dialog(context) {

    init {
        setCancelable(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.failed_nav_dialogue)


    }
}
