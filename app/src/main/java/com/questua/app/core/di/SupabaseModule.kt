package com.questua.app.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.googleNativeLogin
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = "https://ekunnuhafzvxvyqkhknx.supabase.co",
            supabaseKey = "sb_publishable_g6qxciNgYHfWJTk5irlkLA_jqNB4fmI"
        ) {
            install(Auth)
            install(ComposeAuth) {
                googleNativeLogin(serverClientId = "492556656186-5kijpae0k60vivm14vmanf6t991890nq.apps.googleusercontent.com")
            }
        }
    }
}