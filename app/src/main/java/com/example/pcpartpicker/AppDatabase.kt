package com.example.pcpartpicker

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * The main Room Database for the PC Part Picker app.
 * This database stores information on PC Components, alongside user-created Lists and Bundles
 * Definitions for [ComponentEntity], [ListEntity], [ListComponentCrossRef], [BundleEntity],
 * and [BundleComponentCrossRef]
 */
@Database(
    entities = [
        ComponentEntity::class,
        ListEntity::class,
        ListComponentCrossRef::class,
        BundleEntity:: class,
        BundleComponentCrossRef:: class
                ],
    version = 1
)

abstract class AppDatabase : RoomDatabase() {
    /**
     * Provides access to the DAO for [ComponentEntity] and related operations
     */
    abstract fun componentDao(): ComponentDao

    /**
     * Provides access to the DAO for [BundleEntity] and related operations
     */
    abstract fun bundleDao(): BundleDao
}