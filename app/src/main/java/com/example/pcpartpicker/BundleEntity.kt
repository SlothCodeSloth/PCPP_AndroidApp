package com.example.pcpartpicker

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bundles")
data class BundleEntity (
    @PrimaryKey(autoGenerate = true) val bundleId: Int = 0,
    val vendor: String,
    val name: String,
    val price: String,
    val url: String,
    val image: String,
    val listId: Int
)