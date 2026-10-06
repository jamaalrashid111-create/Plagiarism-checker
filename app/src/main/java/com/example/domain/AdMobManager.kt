package com.example.domain

import android.content.Context

/**
 * Clean architectural abstraction for Google AdMob.
 * Per requirements: Ads must only load when properly configured with real AdMob IDs.
 * Fake ads are strictly avoided.
 */
interface AdProvider {
    fun isAdMobConfigured(): Boolean
    fun showBannerAd(context: Context)
    fun showInterstitialAd(context: Context, onDismissed: () -> Unit)
}

class AdMobManager(
    private val bannerAdUnitId: String? = null,
    private val interstitialAdUnitId: String? = null
) : AdProvider {

    override fun isAdMobConfigured(): Boolean {
        return !bannerAdUnitId.isNullOrBlank() && !bannerAdUnitId.startsWith("ca-app-pub-3940256099942544")
    }

    override fun showBannerAd(context: Context) {
        if (!isAdMobConfigured()) {
            // No fake ads rendered
            return
        }
        // When real AdMob ID is configured, load real MobileAds view
    }

    override fun showInterstitialAd(context: Context, onDismissed: () -> Unit) {
        if (!isAdMobConfigured()) {
            onDismissed()
            return
        }
        // Load and present real interstitial ad
        onDismissed()
    }
}
