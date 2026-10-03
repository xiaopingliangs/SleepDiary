package app.sleepdiary.offline.ui.publicnav

import android.content.Context

/** User-controlled local preference; intentionally excluded from Room and backups. */
class ContextualEducationPreference(context: Context) {
    private val preferences = context.getSharedPreferences("public_education_matching", Context.MODE_PRIVATE)

    fun isEnabled(): Boolean = preferences.getBoolean(KEY_ENABLED, true)

    fun setEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    private companion object {
        const val KEY_ENABLED = "enabled"
    }
}
