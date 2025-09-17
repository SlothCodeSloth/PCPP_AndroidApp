package com.example.pcpartpicker

sealed class ListItem {
    data class ComponentItem(val component: ComponentEntity) : ListItem()
    data class BundleItem(val bundle: BundleEntity): ListItem()
}