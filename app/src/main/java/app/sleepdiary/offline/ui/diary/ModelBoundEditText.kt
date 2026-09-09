package app.sleepdiary.offline.ui.diary

import android.content.Context
import android.widget.EditText

/** Only a changed model value may replace native IME editing state. */
internal class ModelBoundEditText(context: Context) : EditText(context) {
    private var lastModelValue: String? = null
    var syncingModel = false
        private set

    fun syncValue(value: String) {
        if (lastModelValue == value) return
        lastModelValue = value
        if (text.toString() == value) return
        syncingModel = true
        try { setText(value); setSelection(text.length) }
        finally { syncingModel = false }
    }
}
