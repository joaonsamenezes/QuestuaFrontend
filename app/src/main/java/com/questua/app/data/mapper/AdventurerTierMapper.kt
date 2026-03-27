package com.questua.app.data.mapper

import com.questua.app.data.remote.dto.AdventurerTierResponseDTO
import com.questua.app.domain.model.AdventurerTier

fun AdventurerTierResponseDTO.toDomain(): AdventurerTier {
    return AdventurerTier(
        id = this.id,
        keyName = this.keyName,
        nameDisplay = this.nameDisplay,
        iconUrl = this.iconUrl,
        colorHex = this.colorHex,
        orderIndex = this.orderIndex,
        levelRequired = this.levelRequired,
        createdAt = this.createdAt
    )
}