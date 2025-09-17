package com.example.pcpartpicker

import androidx.room.Entity

@Entity(primaryKeys = ["listId", "componentUrl"])
data class ListComponentCrossRef(
    val listId: Int,
    val componentUrl: String
)
