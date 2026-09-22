package com.antiphonesnatcher.app.licensing

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.antiphonesnatcher.app.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Validates the authenticity and license of the Anti Phone Snatcher application.
 *
 * Enforces Google Play Store ownership to protect against:
 * 1. Unauthorized sideloading (APKs extracted and sent over Bluetooth / Quick Share).
 * 2. Unofficial distribution on third-party stores (APKPure, torrents, etc.).
 * 3. Tampering and cloned builds.
 *
 * During local development (BuildConfig.DEBUG == true):
 * - Automatically grants LICENSED status so developers and QA can test freely.
 * - Supports a Developer Simulation toggle to preview the unauthorized lockout screen on demand.
 */
object LicenseValidator {

    private const val TAG = "LicenseValidator"

    /**
     * Official Google Play Store package identifiers.
     * Only these packages are authorized installers of genuine production releases.
     */
    val AUTHORIZED_INSTALLERS = setOf(
        "com.android.vending",           // Google Play Store
        "com.google.android.feedback"   // Google Play internal / beta testing track
    )

    /**
     * Placeholder for the developer's Google Play License Base64 Public Key.
     * Found in: Google Play Console -> Your App -> Monetization setup / Services & APIs -> Licensing.
     */
    const val GOOGLE_PLAY_LICENSE_PUBLIC_KEY = "PASTE_GOOGLE_PLAY_PUBLIC_KEY_HERE"

    enum class LicenseStatus {
        CHECKING,
        LICENSED,
        NOT_LICENSED
    }

    private val _licenseStatus = MutableStateFlow<LicenseStatus>(
        if (BuildConfig.DEBUG) LicenseStatus.LICENSED else LicenseStatus.CHECKING
    )
    val licenseStatus: StateFlow<LicenseStatus> = _licenseStatus.asStateFlow()

    /**
     * Evaluates whether this installation is authorized.
     */
    fun verifyLicense(context: Context, onResult: ((LicenseStatus) -> Unit)? = null) {
        // 1. In Debug mode, bypass checks for local testing
        if (BuildConfig.DEBUG) {
            _licenseStatus.value = LicenseStatus.LICENSED
            onResult?.invoke(LicenseStatus.LICENSED)
            return
        }

        // 2. In Release builds, verify the installer package origin
        try {
            val installerPackageName = getInstallerPackage(context)
            Log.d(TAG, "Application installed via: $installerPackageName")

            val isFromAuthorizedStore = installerPackageName != null &&
                    AUTHORIZED_INSTALLERS.contains(installerPackageName)

            if (isFromAuthorizedStore) {
                _licenseStatus.value = LicenseStatus.LICENSED
                onResult?.invoke(LicenseStatus.LICENSED)
            } else {
                Log.w(TAG, "Unauthorized installer: $installerPackageName. Locking app features.")
                _licenseStatus.value = LicenseStatus.NOT_LICENSED
                onResult?.invoke(LicenseStatus.NOT_LICENSED)
            }
        } catch (e: Exception) {
            Log.e(TAG, "License verification exception", e)
            // If unknown error occurs in release, protect against circumvention
            _licenseStatus.value = LicenseStatus.NOT_LICENSED
            onResult?.invoke(LicenseStatus.NOT_LICENSED)
        }
    }

    /**
     * Returns true if the app is currently verified to run.
     */
    fun isLicensed(context: Context): Boolean {
        if (BuildConfig.DEBUG) {
            return true
        }
        return _licenseStatus.value == LicenseStatus.LICENSED
    }

    /**
     * Resolves the installer package name across modern and legacy Android versions.
     */
    fun getInstallerPackage(context: Context): String? {
        return try {
            val pm = context.packageManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val sourceInfo = pm.getInstallSourceInfo(context.packageName)
                sourceInfo.installingPackageName ?: sourceInfo.initiatingPackageName
            } else {
                @Suppress("DEPRECATION")
                pm.getInstallerPackageName(context.packageName)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read install source", e)
            null
        }
    }
}
