package com.example.pcpartpicker

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class BundleWithComponents(
    @Embedded val bundle: BundleEntity,
    @Relation(
        parentColumn = "bundleId",
        entityColumn = "url",
        associateBy = Junction(
            BundleComponentCrossRef::class,
            parentColumn = "bundleId",
            entityColumn =  "url"
        )
    )
    val components: List<ComponentEntity>
)
