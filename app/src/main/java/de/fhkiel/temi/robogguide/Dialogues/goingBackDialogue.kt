import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import de.fhkiel.temi.robogguide.R

//creates a dialog
class GoingBackDialogue(context: Context) : Dialog(context) {

    init {
        setCancelable(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.leaving_dialogue)
    }
}
