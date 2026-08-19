package com.sih2026.touristsafety.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.sih2026.touristsafety.data.local.TouristSafetyDatabase
import com.sih2026.touristsafety.data.local.dao.ChatMessageDao
import com.sih2026.touristsafety.data.local.dao.DisasterAlertDao
import com.sih2026.touristsafety.data.local.dao.DocumentDao
import com.sih2026.touristsafety.data.local.dao.EmergencyContactDao
import com.sih2026.touristsafety.data.local.dao.GeofenceZoneDao
import com.sih2026.touristsafety.data.local.dao.IncidentDao
import com.sih2026.touristsafety.data.local.dao.ProfileDao
import com.sih2026.touristsafety.data.local.dao.ReceivedSOSAlertDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

import java.util.concurrent.TimeUnit

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): TouristSafetyDatabase {
        return Room.databaseBuilder(
            appContext,
            TouristSafetyDatabase::class.java,
            "tourist_safety_db"
        ).fallbackToDestructiveMigration().build()
    }

    // DAO providers
    @Provides fun provideProfileDao(db: TouristSafetyDatabase): ProfileDao = db.profileDao()
    @Provides fun provideEmergencyContactDao(db: TouristSafetyDatabase): EmergencyContactDao = db.emergencyContactDao()
    @Provides fun provideGeofenceZoneDao(db: TouristSafetyDatabase): GeofenceZoneDao = db.geofenceZoneDao()
    @Provides fun provideIncidentDao(db: TouristSafetyDatabase): IncidentDao = db.incidentDao()
    @Provides fun provideDisasterAlertDao(db: TouristSafetyDatabase): DisasterAlertDao = db.disasterAlertDao()
    @Provides fun provideDocumentDao(db: TouristSafetyDatabase): DocumentDao = db.documentDao()
    @Provides fun provideChatMessageDao(db: TouristSafetyDatabase): ChatMessageDao = db.chatMessageDao()
    @Provides fun provideReceivedSOSAlertDao(db: TouristSafetyDatabase): ReceivedSOSAlertDao = db.receivedSOSAlertDao()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://192.168.1.41:8000/") // Local network IP for real devices
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideChatApiService(retrofit: Retrofit): com.sih2026.touristsafety.data.remote.ChatApiService {
        return retrofit.create(com.sih2026.touristsafety.data.remote.ChatApiService::class.java)
    }

    @Provides
    @Singleton
    fun providePlacesApiService(retrofit: Retrofit): com.sih2026.touristsafety.data.remote.PlacesApiService {
        return retrofit.create(com.sih2026.touristsafety.data.remote.PlacesApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext appContext: Context): DataStore<Preferences> {
        return appContext.dataStore
    }
}
