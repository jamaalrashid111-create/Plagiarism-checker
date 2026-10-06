package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import com.example.data.model.PlanType

class SettingsPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("munasar_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CUSTOM_API_URL = "custom_api_url"
        private const val KEY_CUSTOM_API_KEY = "custom_api_key"
        private const val KEY_CUSTOM_GEMINI_KEY = "custom_gemini_key"
        private const val KEY_USER_LOGGED_IN = "user_logged_in"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_PLAN_TYPE = "plan_type"
        private const val KEY_CHECKS_TODAY = "checks_today"
        private const val KEY_LAST_CHECK_DATE = "last_check_date"
    }

    /**
     * Resolves the active API URL, prioritizing user-configured endpoint in Settings,
     * falling back to the compile-time BuildConfig.PLAGIARISM_API_URL (from .env).
     */
    fun getApiUrl(): String {
        val custom = prefs.getString(KEY_CUSTOM_API_URL, null)
        if (!custom.isNullOrBlank()) return custom.trim()
        val envUrl = try {
            BuildConfig.PLAGIARISM_API_URL
        } catch (_: Exception) {
            ""
        }
        return if (envUrl.isBlank() || envUrl.equals("UNCONFIGURED", ignoreCase = true) || envUrl.equals("DEFAULT_URL", ignoreCase = true)) "" else envUrl.trim()
    }

    /**
     * Resolves the active API Key, prioritizing user-configured key in Settings,
     * falling back to the compile-time BuildConfig.PLAGIARISM_API_KEY (from .env).
     */
    fun getApiKey(): String {
        val custom = prefs.getString(KEY_CUSTOM_API_KEY, null)
        if (!custom.isNullOrBlank()) return custom.trim()
        val envKey = try {
            BuildConfig.PLAGIARISM_API_KEY
        } catch (_: Exception) {
            ""
        }
        return if (envKey.isBlank() || envKey.equals("UNCONFIGURED", ignoreCase = true) || envKey.equals("DEFAULT_KEY", ignoreCase = true)) "" else envKey.trim()
    }

    fun getGeminiApiKey(): String {
        val custom = prefs.getString(KEY_CUSTOM_GEMINI_KEY, null)
        if (!custom.isNullOrBlank()) return custom.trim()
        val envKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }
        return if (envKey.isBlank() || envKey.equals("UNCONFIGURED", ignoreCase = true)) "" else envKey.trim()
    }

    fun isGeminiConfigured(): Boolean = getGeminiApiKey().isNotBlank()

    fun isApiConfigured(): Boolean {
        if (isGeminiConfigured()) return true
        val url = getApiUrl()
        return url.isNotBlank() && (url.startsWith("http://") || url.startsWith("https://"))
    }

    fun setCustomGeminiKey(key: String) {
        prefs.edit().putString(KEY_CUSTOM_GEMINI_KEY, key.trim()).apply()
    }

    fun setCustomApiConfig(url: String, apiKey: String) {
        prefs.edit()
            .putString(KEY_CUSTOM_API_URL, url.trim())
            .putString(KEY_CUSTOM_API_KEY, apiKey.trim())
            .apply()
    }

    fun clearCustomApiConfig() {
        prefs.edit()
            .remove(KEY_CUSTOM_API_URL)
            .remove(KEY_CUSTOM_API_KEY)
            .apply()
    }

    fun isUserLoggedIn(): Boolean = prefs.getBoolean(KEY_USER_LOGGED_IN, false)
    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)
    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "Guest User") ?: "Guest User"

    fun setUserSession(loggedIn: Boolean, email: String?, name: String) {
        prefs.edit()
            .putBoolean(KEY_USER_LOGGED_IN, loggedIn)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_NAME, name)
            .apply()
    }

    fun getPlanType(): PlanType {
        val name = prefs.getString(KEY_PLAN_TYPE, PlanType.FREE.name)
        return try {
            PlanType.valueOf(name ?: PlanType.FREE.name)
        } catch (_: Exception) {
            PlanType.FREE
        }
    }

    fun setPlanType(planType: PlanType) {
        prefs.edit().putString(KEY_PLAN_TYPE, planType.name).apply()
    }

    fun getChecksUsedToday(): Int {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        val savedDate = prefs.getString(KEY_LAST_CHECK_DATE, null)
        if (savedDate != today) {
            prefs.edit().putString(KEY_LAST_CHECK_DATE, today).putInt(KEY_CHECKS_TODAY, 0).apply()
            return 0
        }
        return prefs.getInt(KEY_CHECKS_TODAY, 0)
    }

    fun incrementChecksUsedToday() {
        val current = getChecksUsedToday()
        prefs.edit().putInt(KEY_CHECKS_TODAY, current + 1).apply()
    }

    fun clearAllPreferences() {
        prefs.edit().clear().apply()
    }
}
