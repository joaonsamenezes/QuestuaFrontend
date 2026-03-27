package com.questua.app.data.remote.api

import com.questua.app.data.remote.dto.AdventurerTierRequestDTO
import com.questua.app.data.remote.dto.AdventurerTierResponseDTO
import com.questua.app.data.remote.dto.PageResponse
import retrofit2.Response
import retrofit2.http.*

interface AdventurerTierApi {
    @GET("adventurer-tiers")
    suspend fun list(
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): Response<PageResponse<AdventurerTierResponseDTO>>

    @GET("adventurer-tiers/{id}")
    suspend fun getById(@Path("id") id: String): Response<AdventurerTierResponseDTO>

    @POST("adventurer-tiers")
    suspend fun create(@Body dto: AdventurerTierRequestDTO): Response<AdventurerTierResponseDTO>

    @PUT("adventurer-tiers/{id}")
    suspend fun update(@Path("id") id: String, @Body dto: AdventurerTierRequestDTO): Response<AdventurerTierResponseDTO>

    @DELETE("adventurer-tiers/{id}")
    suspend fun delete(@Path("id") id: String): Response<Unit>
}