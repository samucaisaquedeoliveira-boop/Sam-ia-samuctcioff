package com.example.data.update

import com.example.data.local.AppReleaseEntity

data class SemanticVersion(
    val major: Int,
    val minor: Int,
    val patch: Int
) : Comparable<SemanticVersion> {

    override fun compareTo(other: SemanticVersion): Int {
        if (major != other.major) return major.compareTo(other.major)
        if (minor != other.minor) return minor.compareTo(other.minor)
        return patch.compareTo(other.patch)
    }

    override fun toString(): String = "$major.$minor.$patch"

    companion object {
        fun parse(raw: String): SemanticVersion {
            val cleaned = raw
                .trim()
                .lowercase()
                .removePrefix("versão")
                .removePrefix("versao")
                .removePrefix("v")
                .trim()
                .substringBefore("-")
                .substringBefore("+")
            val parts = cleaned.split(".")
                .mapNotNull { part -> part.filter { it.isDigit() }.toIntOrNull() }
            return SemanticVersion(
                major = parts.getOrElse(0) { 0 },
                minor = parts.getOrElse(1) { 0 },
                patch = parts.getOrElse(2) { 0 }
            )
        }

        fun isValidFormat(raw: String): Boolean {
            val cleaned = raw.trim().removePrefix("v").removePrefix("V")
            val regex = Regex("""^\d+\.\d+(\.\d+)?$""")
            return regex.matches(cleaned)
        }
    }
}

object UpdatePolicyEvaluator {

    const val STATUS_PUBLISHED = "PUBLISHED"
    const val STATUS_SCHEDULED = "SCHEDULED"
    const val STATUS_IN_DEVELOPMENT = "IN_DEVELOPMENT"
    const val STATUS_RETIRED = "RETIRED"

    fun isReleaseActive(release: AppReleaseEntity, nowMillis: Long = System.currentTimeMillis()): Boolean {
        return when (release.status) {
            STATUS_PUBLISHED -> true
            STATUS_SCHEDULED -> release.scheduledAt > 0L && nowMillis >= release.scheduledAt
            else -> false
        }
    }

    fun isNewerThanInstalled(
        release: AppReleaseEntity,
        installedVersionName: String,
        installedVersionCode: Int
    ): Boolean {
        val releaseSemVer = SemanticVersion.parse(release.versionName)
        val installedSemVer = SemanticVersion.parse(installedVersionName)
        return releaseSemVer > installedSemVer || release.versionCode > installedVersionCode
    }

    fun isMandatoryForDevice(
        release: AppReleaseEntity,
        installedVersionName: String,
        installedVersionCode: Int,
        nowMillis: Long = System.currentTimeMillis()
    ): Boolean {
        if (!isReleaseActive(release, nowMillis)) return false
        val installedSemVer = SemanticVersion.parse(installedVersionName)
        val minRequiredSemVer = SemanticVersion.parse(release.minRequiredVersion.ifBlank { "1.0.0" })
        val releaseSemVer = SemanticVersion.parse(release.versionName)

        // If installed version is below minRequiredVersion, an active release that brings it up is mandatory.
        val belowMinimum = installedSemVer < minRequiredSemVer && releaseSemVer >= minRequiredSemVer
        val newerMandatory = release.isMandatory && isNewerThanInstalled(release, installedVersionName, installedVersionCode)
        return belowMinimum || newerMandatory
    }

    fun findPendingMandatoryUpdate(
        releases: List<AppReleaseEntity>,
        installedVersionName: String,
        installedVersionCode: Int,
        nowMillis: Long = System.currentTimeMillis()
    ): AppReleaseEntity? {
        return releases
            .filter { isMandatoryForDevice(it, installedVersionName, installedVersionCode, nowMillis) }
            .maxWithOrNull(
                compareBy<AppReleaseEntity> { SemanticVersion.parse(it.versionName) }
                    .thenBy { it.versionCode }
                    .thenBy { it.publishedAt }
            )
    }

    fun findAvailableUpdates(
        releases: List<AppReleaseEntity>,
        installedVersionName: String,
        installedVersionCode: Int,
        nowMillis: Long = System.currentTimeMillis()
    ): List<AppReleaseEntity> {
        return releases
            .filter { isReleaseActive(it, nowMillis) && isNewerThanInstalled(it, installedVersionName, installedVersionCode) }
            .sortedWith(
                compareByDescending<AppReleaseEntity> { SemanticVersion.parse(it.versionName) }
                    .thenByDescending { it.versionCode }
            )
    }
}
