package com.example.pcpartpicker

/**
 * Represents items that can appear in a list.
 *
 * This sealed class allows a list to hold components or bundles.
 */
sealed class ListItem {
    data class ComponentItem(val component: ComponentEntity) : ListItem()
    data class BundleItem(val bundle: BundleEntity): ListItem()
}