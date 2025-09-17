package com.example.pcpartpicker

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ComponentDao {
    @Transaction
    suspend fun deleteListAndCrossRefs(listId: Int) {
        deleteCrossRefsForList(listId)
        deleteListById(listId)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComponent(componentEntity: ComponentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: ListEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRef(crossRef: ListComponentCrossRef)

    @Query("SELECT * FROM components")
    suspend fun getAllComponents(): List<ComponentEntity>

    @Transaction
    @Query("SELECT * FROM lists")
    suspend fun getAllListsWithComponents(): List<ListWithItems>

    @Transaction
    @Query("SELECT * FROM lists WHERE id = :listId")
    suspend fun getListWithComponents(listId: Int): ListWithItems

    @Transaction
    @Query("SELECT * FROM lists WHERE name = :listName")
    suspend fun getListWithItems(listName: String): ListWithItems?

    @Transaction
    @Query("SELECT * FROM lists")
    suspend fun getAllListsWithItems(): List<ListWithItems>

    @Query("DELETE FROM ListComponentCrossRef WHERE listId = :listId AND componentUrl = :componentUrl")
    suspend fun deleteCrossRef(listId: Int, componentUrl: String)

    @Query("SELECT COUNT(*) FROM ListComponentCrossRef WHERE componentUrl = :url")
    suspend fun getListCountForComponent(url: String): Int

    @Query("DELETE FROM components WHERE url = :url")
    suspend fun deleteComponent(url: String)

    @Query("DELETE FROM lists WHERE id = :listId")
    suspend fun deleteListById(listId: Int)

    @Query("DELETE FROM ListComponentCrossRef WHERE listId = :listId")
    suspend fun deleteCrossRefsForList(listId: Int)

    @Query("SELECT * FROM lists WHERE name = :listName LIMIT 1")
    suspend fun getListByName(listName: String): ListEntity?

    @Query("SELECT * FROM components WHERE url = :url")
    fun getComponentByUrl(url: String): LiveData<ComponentEntity?>

    @Query("UPDATE components SET customPrice = :customPrice, customUrl = :customUrl, customVendor = :customVendor WHERE url = :componentUrl")
    suspend fun updateComponentCustomData(componentUrl: String, customPrice: String?, customUrl: String?, customVendor: String?)

    @Transaction
    @Query("SELECT * FROM bundles WHERE listId = :listId")
    suspend fun getBundlesForList(listId: Int): List<BundleWithComponents>

    @Transaction
    @Query("SELECT * FROM bundles WHERE bundleId = :bundleId")
    suspend fun getBundleWithComponents(bundleId: Int): BundleWithComponents

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBundle(bundle: BundleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBundleComponentCrossRef(ref: BundleComponentCrossRef)
}