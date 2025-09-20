package com.example.pcpartpicker

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * Data Access Object (DAO) for managing database operations related to
 * components, lists, cross-references, and bundles in the PC Part Picker application.
 *
 * This DAO provides comprehensive CRUD operations for:
 * - ComponentEntity: Individual PC components (CPU, GPU, etc.)
 * - ListEntity: User-created lists of components
 * - ListComponentCrossRef: Many-to-many relationships between lists and components
 * - BundleEntity: Grouped sets of components within lists
 * - BundleComponentCrossRef: Relationships between bundles and their components
 */
@Dao
interface ComponentDao {
    /**
     * Deletes a list and all cross-references to it in a single transaction.
     *
     * @param listId The unique identifier of the list to delete
     */
    @Transaction
    suspend fun deleteListAndCrossRefs(listId: Int) {
        deleteCrossRefsForList(listId)
        deleteListById(listId)
    }

    // -------------------- Insert Operations --------------------

    /**
     * Inserts a new component into the database or updates an existing one.
     *
     * @param componentEntity The component entity to insert/update
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComponent(componentEntity: ComponentEntity)

    /**
     * Inserts a new list into the database or updates an existing one.
     *
     * @param list The list entity to insert/update
     * @return The unique ID of the inserted/updated list
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: ListEntity): Long

    /**
     * Inserts a cross-reference between a list and a component.
     *
     * @param crossRef The cross-reference entity linking a list to a component
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRef(crossRef: ListComponentCrossRef)

    // -------------------- Query Operations --------------------

    /**
     * Retrieves all components from the database.
     *
     * @return A list of all component entities
     */
    @Query("SELECT * FROM components")
    suspend fun getAllComponents(): List<ComponentEntity>

    /**
     * Retrieves all lists along with their associated components using a transaction.
     *
     * @return A list of ListWithItems objects, each containing a list and its components
     */
    @Transaction
    @Query("SELECT * FROM lists")
    suspend fun getAllListsWithComponents(): List<ListWithItems>

    /**
     * Retrieves a specific list with all its associated components.
     *
     * @param listId The unique identifier of the list to retrieve
     * @return A ListWithItems object containing the list and its components
     */
    @Transaction
    @Query("SELECT * FROM lists WHERE id = :listId")
    suspend fun getListWithComponents(listId: Int): ListWithItems

    /**
     * Retrieves a list with its components by list name.
     * Returns null if no list with the specified name exists.
     *
     * @param listName The name of the list to retrieve
     * @return A ListWithItems object or null if not found
     */
    @Transaction
    @Query("SELECT * FROM lists WHERE name = :listName")
    suspend fun getListWithItems(listName: String): ListWithItems?

    /**
     * Retrieves all lists along with their associated items (components and bundles).
     *
     * @return A list of ListWithItems objects
     */
    @Transaction
    @Query("SELECT * FROM lists")
    suspend fun getAllListsWithItems(): List<ListWithItems>

    // -------------------- Delete Operations --------------------

    /**
     * Removes a specific cross-reference between a list and a component.
     * This effectively removes a component from a list without deleting
     * the component or list entities themselves.
     *
     * @param listId The ID of the list
     * @param componentUrl The URL identifier of the component to remove from the list
     */
    @Query("DELETE FROM ListComponentCrossRef WHERE listId = :listId AND componentUrl = :componentUrl")
    suspend fun deleteCrossRef(listId: Int, componentUrl: String)

    /**
     * Counts how many lists currently reference a specific component.
     * Used in conjunction with deleteCrossRef (see above).
     * Useful for determining if a component can be safely deleted or if it's still in use.
     *
     * @param url The component's URL identifier
     * @return The number of lists that contain this component
     */
    @Query("SELECT COUNT(*) FROM ListComponentCrossRef WHERE componentUrl = :url")
    suspend fun getListCountForComponent(url: String): Int

    /**
     * Permanently deletes a component from the database.
     * Note: This will not automatically remove cross-references.
     *
     * @param url The URL identifier of the component to delete
     */
    @Query("DELETE FROM components WHERE url = :url")
    suspend fun deleteComponent(url: String)

    /**
     * Deletes a list by its unique identifier.
     * This is used internally by deleteListAndCrossRefs() and should generally
     * not be called directly to avoid orphaned cross-references.
     *
     * @param listId The unique identifier of the list to delete
     */
    @Query("DELETE FROM lists WHERE id = :listId")
    suspend fun deleteListById(listId: Int)

    /**
     * Removes all cross-references associated with a specific list.
     * This is used internally by deleteListAndCrossRefs() to clean up
     * relationships before deleting the list itself.
     *
     * @param listId The ID of the list whose cross-references should be deleted
     */
    @Query("DELETE FROM ListComponentCrossRef WHERE listId = :listId")
    suspend fun deleteCrossRefsForList(listId: Int)


    // --------------------- Find by Name/URL ---------------------

    /**
     * Finds a list entity by its name.
     *
     * @param listName The name of the list to find
     * @return The ListEntity if found, null otherwise
     */
    @Query("SELECT * FROM lists WHERE name = :listName LIMIT 1")
    suspend fun getListByName(listName: String): ListEntity?

    /**
     * Retrieves a component by its URL identifier as LiveData.
     *
     * @param url The component's URL identifier
     * @return LiveData containing the ComponentEntity or null if not found
     */
    @Query("SELECT * FROM components WHERE url = :url")
    fun getComponentByUrl(url: String): LiveData<ComponentEntity?>

    // -------------------- Update Operations --------------------

    /**
     * Updates the custom data fields for a specific component.
     * Allows users to override default component information with custom values
     * (pricing from different vendors or alternative purchase URLs).
     *
     * @param componentUrl The URL identifier of the component to update
     * @param customPrice Custom price override (nullable)
     * @param customUrl Custom purchase URL override (nullable)
     * @param customVendor Custom vendor name override (nullable)
     */
    @Query("UPDATE components SET customPrice = :customPrice, customUrl = :customUrl, customVendor = :customVendor WHERE url = :componentUrl")
    suspend fun updateComponentCustomData(componentUrl: String, customPrice: String?, customUrl: String?, customVendor: String?)

    // -------------------- Bundle Queries --------------------

    /**
     * Retrieves all bundles associated with a specific list, including their components.
     * Bundles represent a list of components... within a list
     *
     * @param listId The ID of the list whose bundles to retrieve
     * @return A list of BundleWithComponents objects
     */
    @Transaction
    @Query("SELECT * FROM bundles WHERE listId = :listId")
    suspend fun getBundlesForList(listId: Int): List<BundleWithComponents>

    /**
     * Retrieves a specific bundle along with all its associated components.
     *
     * @param bundleId The unique identifier of the bundle to retrieve
     * @return A BundleWithComponents object containing the bundle and its components
     */
    @Transaction
    @Query("SELECT * FROM bundles WHERE bundleId = :bundleId")
    suspend fun getBundleWithComponents(bundleId: Int): BundleWithComponents

    // -------------------- Bundle Insert --------------------

    /**
     * Inserts a new bundle into the database or updates an existing one.
     * Bundles are a list of components within a list.
     *
     * @param bundle The bundle entity to insert/update
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBundle(bundle: BundleEntity)

    /**
     * Creates a cross-reference between a bundle and a component.
     * This establishes which components belong to which bundles.
     *
     * @param ref The cross-reference entity linking a bundle to a component
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBundleComponentCrossRef(ref: BundleComponentCrossRef)
}