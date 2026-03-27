package com.questua.app.data.remote.dto

import com.questua.app.domain.enums.StatusLanguage
import com.questua.app.domain.model.UnlockedContent
import kotlinx.serialization.Serializable

@Serializable
data class UserLanguageRequestDTO(
    val userId: String,
    val languageId: String,
    val statusLanguage: StatusLanguage = StatusLanguage.ACTIVE,
    val cefrLevel: String = "A1",
    val questsTowardsNextLevel: Int = 0,
    val gamificationLevel: Int = 1,
    val xpTotal: Int = 0,
    val streakDays: Int = 0,
    val unlockedContent: UnlockedContent? = null,
    val adventurerTierId: String? = null,
    val startedAt: String,
    val lastActiveAt: String? = null
)

@Serializable
data class UserLanguageResponseDTO(
    val id: String,
    val userId: String,
    val languageId: String,
    val statusLanguage: StatusLanguage,
    val cefrLevel: String,
    val questsTowardsNextLevel: Int = 0,
    val gamificationLevel: Int,
    val xpTotal: Int,
    val streakDays: Int,
    val unlockedContent: UnlockedContent?,
    val adventurerTierId: String? = null,
    val startedAt: String,
    val lastActiveAt: String?
)