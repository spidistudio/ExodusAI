package com.exodus.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GitHubRelease(
    val tagName: String,
    val name: String,
    val body: String,
    val htmlUrl: String,
    val downloadUrl: String?,
    val publishedAt: String,
    val prerelease: Boolean = false,
    val draft: Boolean = false
) : Parcelable

data class UpdateInfo(
    val isUpdateAvailable: Boolean,
    val latestVersion: String,
    val currentVersion: String,
    val releaseInfo: GitHubRelease?
)

enum class UpdateStatus {
    CHECKING,
    UPDATE_AVAILABLE,
    NO_UPDATE,
    ERROR
}