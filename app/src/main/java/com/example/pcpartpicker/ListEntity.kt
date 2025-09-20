package com.example.pcpartpicker

import androidx.room.Entity
import androidx.room.PrimaryKey


/**
 * Entity representing a user created list of components.
 *
 * @property id The auto-generated unique identifier for the list.
 * @property name The name of the list, provided by the user.
 * @property iconResId The resource ID of the icon associated with this list.
 */
@Entity(tableName = "lists")
data class ListEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val iconResId: Int
)
