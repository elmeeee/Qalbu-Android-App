package app.kamy.saatApp.infrastructure.notifications

import android.content.Context
import android.content.SharedPreferences
import app.kamy.saatApp.domain.model.LocalReadingProgress
import app.kamy.saatApp.infrastructure.preferences.QuranLastReadReminderStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy

class QuranLastReadReminderTest {

    private class FakeEditor(private val sp: FakeSharedPreferences) : SharedPreferences.Editor {
        private val temp = mutableMapOf<String, Any?>()

        override fun putString(key: String, value: String?): SharedPreferences.Editor {
            temp[key] = value
            return this
        }

        override fun putStringSet(key: String, values: Set<String>?): SharedPreferences.Editor {
            temp[key] = values
            return this
        }

        override fun putInt(key: String, value: Int): SharedPreferences.Editor {
            temp[key] = value
            return this
        }

        override fun putLong(key: String, value: Long): SharedPreferences.Editor {
            temp[key] = value
            return this
        }

        override fun putFloat(key: String, value: Float): SharedPreferences.Editor {
            temp[key] = value
            return this
        }

        override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor {
            temp[key] = value
            return this
        }

        override fun remove(key: String): SharedPreferences.Editor {
            temp[key] = this // Sentinel for removal
            return this
        }

        override fun clear(): SharedPreferences.Editor {
            temp.clear()
            return this
        }

        override fun commit(): Boolean {
            apply()
            return true
        }

        override fun apply() {
            temp.forEach { (k, v) ->
                if (v === this) {
                    sp.values.remove(k)
                } else if (v != null) {
                    sp.values[k] = v
                } else {
                    sp.values.remove(k)
                }
            }
            temp.clear()
        }
    }

    private class FakeSharedPreferences : SharedPreferences {
        val values = mutableMapOf<String, Any>()

        override fun getAll(): Map<String, *> = values
        override fun getString(key: String, defValue: String?): String? = values[key] as? String ?: defValue
        override fun getStringSet(key: String, defValues: Set<String>?): Set<String>? =
            @Suppress("UNCHECKED_CAST") (values[key] as? Set<String>) ?: defValues
        override fun getInt(key: String, defValue: Int): Int = values[key] as? Int ?: defValue
        override fun getLong(key: String, defValue: Long): Long = values[key] as? Long ?: defValue
        override fun getFloat(key: String, defValue: Float): Float = values[key] as? Float ?: defValue
        override fun getBoolean(key: String, defValue: Boolean): Boolean = values[key] as? Boolean ?: defValue
        override fun contains(key: String): Boolean = values.containsKey(key)
        override fun edit(): SharedPreferences.Editor = FakeEditor(this)
        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
    }

    @Test
    fun testReminderStoreTrackingAndReset() {
        val sp = FakeSharedPreferences()
        val store = QuranLastReadReminderStore(sp)

        val ts1 = 1700000000000L
        assertFalse(store.is3DayReminderSent(ts1))
        assertFalse(store.is7DayReminderSent(ts1))

        store.mark3DayReminderSent(ts1, "2026-09-03")
        assertTrue(store.is3DayReminderSent(ts1))
        assertFalse(store.is7DayReminderSent(ts1))
        assertEquals("2026-09-03", store.lastNotificationDate())

        store.mark7DayReminderSent(ts1, "2026-09-07")
        assertTrue(store.is7DayReminderSent(ts1))

        // Reset when user reads again
        store.reset()
        assertFalse(store.is3DayReminderSent(ts1))
        assertFalse(store.is7DayReminderSent(ts1))
    }

    @Test
    fun testEvaluateStageTransitions() {
        val sp = FakeSharedPreferences()
        val store = QuranLastReadReminderStore(sp)

        val baseTime = 1700000000000L
        val oneDayMillis = 86_400_000L
        val progress = LocalReadingProgress(
            chapterNumber = 2,
            verseNumber = 25,
            updatedAtMillis = baseTime
        )

        // 0-2 days: NONE
        assertEquals(
            QuranLastReadReminderScheduler.ReminderStage.NONE,
            QuranLastReadReminderScheduler.evaluateStage(store, progress, baseTime + oneDayMillis * 1)
        )
        assertEquals(
            QuranLastReadReminderScheduler.ReminderStage.NONE,
            QuranLastReadReminderScheduler.evaluateStage(store, progress, baseTime + oneDayMillis * 2)
        )

        // 3 days: THREE_DAYS
        val timeDay3 = baseTime + oneDayMillis * 3 + 1000L
        assertEquals(
            QuranLastReadReminderScheduler.ReminderStage.THREE_DAYS,
            QuranLastReadReminderScheduler.evaluateStage(store, progress, timeDay3)
        )

        // Mark 3-day sent -> next check during 3-6 days should be NONE (idempotent)
        store.mark3DayReminderSent(progress.updatedAtMillis, "2026-09-03")
        assertEquals(
            QuranLastReadReminderScheduler.ReminderStage.NONE,
            QuranLastReadReminderScheduler.evaluateStage(store, progress, timeDay3)
        )
        assertEquals(
            QuranLastReadReminderScheduler.ReminderStage.NONE,
            QuranLastReadReminderScheduler.evaluateStage(store, progress, baseTime + oneDayMillis * 5)
        )

        // 7 days: SEVEN_DAYS
        val timeDay7 = baseTime + oneDayMillis * 7 + 1000L
        assertEquals(
            QuranLastReadReminderScheduler.ReminderStage.SEVEN_DAYS,
            QuranLastReadReminderScheduler.evaluateStage(store, progress, timeDay7)
        )

        // Mark 7-day sent -> next check during 7-13 days should be NONE
        store.mark7DayReminderSent(progress.updatedAtMillis, "2026-09-07")
        assertEquals(
            QuranLastReadReminderScheduler.ReminderStage.NONE,
            QuranLastReadReminderScheduler.evaluateStage(store, progress, timeDay7)
        )

        // 14+ days: NONE (no spam)
        val timeDay15 = baseTime + oneDayMillis * 15
        assertEquals(
            QuranLastReadReminderScheduler.ReminderStage.NONE,
            QuranLastReadReminderScheduler.evaluateStage(store, progress, timeDay15)
        )
    }
}
