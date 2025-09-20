package com.example.pcpartpicker

import android.app.Application
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.room.Room

/**
 * Custom [Application] class for global initialization.
 *
 * Responsibilities:
 * - Provides a Retrofit API client ([PyPartPickerApi]).
 * - Initializes and exposes the Room [AppDatabase] instance.
 */
class MyApplication : Application() {
    val api: PyPartPickerApi by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PyPartPickerApi::class.java)
    }

    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        // Initialize the Database
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "component_db"
        ).build()
    }
}