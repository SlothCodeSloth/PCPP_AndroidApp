package com.example.pcpartpicker

import androidx.room.*

/**
 * DAO for performing database operations related to [BundleEntity] and its relationship with
 * [ComponentEntity].
 *
 * This includes CRUD and many-to-many handling via [BundleComponentCrossRef]
 */
@Dao
interface BundleDao {

    /**
     * Inserts or replaces a single [BundleEntity] into the database.
     *
     * @param bundle The Bundle to insert.
     * @return the row ID of the inserted Bundle.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bundle: BundleEntity): Long

    /**
     * Inserts or replaces a list of [BundleEntity]s into the database.
     *
     * @param bundles the Bundles to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bundles: List<BundleEntity>)

    /**
     * Updates an existing [BundleEntity]
     *
     * @param bundle The Bundle with updated information.
     */
    @Update
    suspend fun update(bundle: BundleEntity)

    /**
     * Deletes a [BundleEntity] from the database.
     *
     * @param bundle The Bundle to delete.
     */
    @Delete
    suspend fun delete(bundle: BundleEntity)

    /**
     * Retrieves a [BundleEntity] by its ID.
     *
     * @param bundleId The unique ID of the Bundle.
     * @return The Bundle with the specified ID, or null if not found.
     */
    @Query("SELECT * FROM bundles WHERE bundleId = :bundleId")
    suspend fun getBundle(bundleId: Int): BundleEntity?

    /**
     * Retrieves all [ComponentEntity]s from the database.
     *
     * @return a list of all [ComponentEntity]s
     */
    @Query("SELECT * FROM components")
    suspend fun getAllComponents(): List<ComponentEntity>


    /**
     * Retrieves all [BundleEntity]s associated with a specific [ListEntity].
     *
     * @return a list of [BundleEntity]s in a specific [ListEntity].
     */
    @Query("SELECT * FROM bundles WHERE listId = :listId")
    suspend fun getBundlesForList(listId: Int): List<BundleEntity>

    /**
     * Retrieves all [BundleEntity]s from the database.
     *
     * @return a list of all [BundleEntity]s
     */
    @Query("SELECT * FROM bundles")
    suspend fun getAllBundles(): List<BundleEntity>

    /**
     * Retrieves a [BundleEntity] with its associated [ComponentEntity]s
     *
     * @return a [BundleWithComponents] object or null if not found.
     */
    @Transaction
    @Query("SELECT * FROM bundles WHERE bundleId = :bundleId")
    suspend fun getBundleWithComponents(bundleId: Int): BundleWithComponents?

    /**
     * Inserts a [BundleComponentCrossRef] into the database, representing a many-to-many relationship
     * between a bundle and a component.
     *
     * @param crossRef The [BundleComponentCrossRef] to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRef(crossRef: BundleComponentCrossRef)

    /**
     * Counts how many bundles a given [ComponentEntity] is associated with.
     *
     * @param url The [ComponentEntity]'s URL.
     * @return The count of bundles associated with the component.
     */
    @Query("SELECT COUNT(*) FROM BundleComponentCrossRef WHERE url = :url")
    suspend fun getBundleCountForComponent(url: String): Int
}