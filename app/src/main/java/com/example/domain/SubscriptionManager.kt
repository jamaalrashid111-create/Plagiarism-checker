package com.example.domain

import com.example.data.local.SettingsPreferences
import com.example.data.model.PlanType
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Interface prepared for real Google Play Billing integration.
 */
interface BillingService {
    suspend fun queryPurchases(): Result<PlanType>
    suspend fun launchBillingFlow(planType: PlanType): Result<Boolean>
    fun isBillingSupported(): Boolean
}

class PlayBillingServiceStub : BillingService {
    override suspend fun queryPurchases(): Result<PlanType> = Result.success(PlanType.FREE)
    override suspend fun launchBillingFlow(planType: PlanType): Result<Boolean> {
        // Prepared for Google Play Billing Client implementation
        return Result.failure(UnsupportedOperationException("Google Play Billing client configuration required."))
    }
    override fun isBillingSupported(): Boolean = false
}

class SubscriptionManager(
    private val preferences: SettingsPreferences,
    private val billingService: BillingService = PlayBillingServiceStub()
) {
    private val _userProfile = MutableStateFlow(loadCurrentProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    fun loadCurrentProfile(): UserProfile {
        return UserProfile(
            isLoggedIn = preferences.isUserLoggedIn(),
            email = preferences.getUserEmail(),
            displayName = preferences.getUserName(),
            plan = preferences.getPlanType(),
            checksUsedToday = preferences.getChecksUsedToday()
        )
    }

    fun refreshProfile() {
        _userProfile.value = loadCurrentProfile()
    }

    fun login(email: String, name: String) {
        preferences.setUserSession(true, email, name)
        refreshProfile()
    }

    fun logout() {
        preferences.setUserSession(false, null, "Guest User")
        refreshProfile()
    }

    fun setPlan(planType: PlanType) {
        preferences.setPlanType(planType)
        refreshProfile()
    }

    fun canPerformCheck(wordCount: Int): Pair<Boolean, String?> {
        val profile = _userProfile.value
        val plan = profile.plan

        if (wordCount > plan.wordLimit) {
            return Pair(false, "Word limit exceeded. ${plan.displayName} allows up to ${plan.wordLimit} words. Upgrade to Pro for 25,000 words.")
        }

        if (profile.checksUsedToday >= plan.dailyChecksLimit) {
            return Pair(false, "Daily scan limit reached (${profile.checksUsedToday}/${plan.dailyChecksLimit}). Upgrade to Pro for unlimited checks.")
        }

        return Pair(true, null)
    }
}
