package app.kamy.saatApp.infrastructure.review

import android.app.Activity
import android.content.Context
import com.google.android.play.core.review.ReviewManagerFactory

object AppReviewManager {
    private const val PREFS = "saat_app_review_prefs"
    private const val KEY_INSTALL_TIME = "install_time"
    private const val KEY_READ_SESSIONS = "read_sessions"
    private const val KEY_REVIEW_REQUESTED = "review_requested"

    fun recordAppLaunch(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getLong(KEY_INSTALL_TIME, 0L) == 0L) {
            prefs.edit().putLong(KEY_INSTALL_TIME, System.currentTimeMillis()).apply()
        }
    }

    fun recordReadSession(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val currentSessions = prefs.getInt(KEY_READ_SESSIONS, 0)
        prefs.edit().putInt(KEY_READ_SESSIONS, currentSessions + 1).apply()
    }

    fun shouldRequestReview(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_REVIEW_REQUESTED, false)) return false

        val installTime = prefs.getLong(KEY_INSTALL_TIME, 0L)
        if (installTime == 0L) {
            // If install_time was somehow not recorded, record it now
            prefs.edit().putLong(KEY_INSTALL_TIME, System.currentTimeMillis()).apply()
            return false
        }

        val daysSinceInstall = (System.currentTimeMillis() - installTime) / (1000 * 60 * 60 * 24)
        val readSessions = prefs.getInt(KEY_READ_SESSIONS, 0)

        // Rule: minimal 3 days using app, and at least 5 read sessions
        return daysSinceInstall >= 3 && readSessions >= 5
    }

    fun launchReviewFlow(activity: Activity, onComplete: () -> Unit = {}) {
        val context = activity.applicationContext
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        
        val manager = ReviewManagerFactory.create(activity)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                val flow = manager.launchReviewFlow(activity, reviewInfo)
                flow.addOnCompleteListener {
                    prefs.edit().putBoolean(KEY_REVIEW_REQUESTED, true).apply()
                    onComplete()
                }
            } else {
                onComplete()
            }
        }
    }
}
