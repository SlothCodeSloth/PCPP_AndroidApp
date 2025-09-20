package com.example.pcpartpicker

import android.media.Image
import android.provider.ContactsContract.CommonDataKinds.Im
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

/**
 * Adapter for displaying components and bundles in a List, represented as a RecyclerView.
 *
 * This adapter handles two types of items: [ListItem.ComponentItem] and [ListItem.BundleItem].
 * It supports item clicks and "Add" button actions for components.
 *
 * @param items The list of [ListItem] objects (components or bundles) to display.
 * @param onItemClick Callback invoked when a list item is clicked.
 * @param onAddClick Callback invoked when the "Add" button of a component is clicked.
 * @param showButton Determines if the "Add" button should be shown for components. (If in Search)
 */
class ComponentAdapter (
    private val items: MutableList<ListItem>,
    private val onItemClick: (ListItem) -> Unit,
    private val onAddClick: (ListItem) -> Unit,
    private val showButton: Boolean = true
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_COMPONENT = 0
        private const val TYPE_BUNDLE = 1
    }

    /**
     * ViewHolder for displaying a component item.
     *
     * @property productImage Image of the component.
     * @property productName Name of the component.
     * @property productPrice Displayed price of the component.
     * @property priceAlt Optional custom price.
     * @property addButton Button for adding the component to a list or bundle.
     */
    class ComponentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.componentImage)
        val productName: TextView = itemView.findViewById(R.id.componentName)
        val productPrice: TextView = itemView.findViewById(R.id.componentPrice)
        val priceAlt: TextView = itemView.findViewById(R.id.componentPriceAlt)
        val addButton: Button = itemView.findViewById(R.id.componentButton)
    }

    /**
     * ViewHolder for displaying a bundle item.
     *
     * @property bundleName Name of the bundle.
     * @property bundlePrice Displayed price of the bundle.
     * @property bundleImage Image representing the bundle.
     */
    class BundleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bundleName: TextView = itemView.findViewById(R.id.bundleName)
        val bundlePrice: TextView = itemView.findViewById(R.id.bundlePrice)
        val bundleImage: ImageView = itemView.findViewById(R.id.bundleImage)
    }

    /**
     * Determines the type of view for a given position in the list. (Bundle or Component)
     */
    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ListItem.ComponentItem -> TYPE_COMPONENT
            is ListItem.BundleItem -> TYPE_BUNDLE
        }
    }
    /**
     * Inflates the correct layout based on the view type.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            TYPE_COMPONENT -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_component, parent, false)
                ComponentViewHolder(view)
            }

            TYPE_BUNDLE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bundle, parent, false)
                BundleViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid View Input")
        }
    }

    /**
     * Binds the data to the ViewHolder
     *
     * Handles displaying component and bundle details, loading images with Glide,
     * managing the visibility of the "Add" button and alternate price, and setting click listeners.
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ListItem.ComponentItem -> {
                val componentHolder = holder as ComponentViewHolder
                componentHolder.productName.text = item.component.name
                componentHolder.productPrice.text = SettingsDataManager.formatPrice(
                    holder.itemView.context,
                    item.component.price
                )
                componentHolder.priceAlt.text = SettingsDataManager.getDisplayPriceForList(
                    holder.itemView.context,
                    item.component.price,
                    item.component.customPrice
                )

                // Load component image using Glide with placeholder fallback
                Glide.with(holder.itemView.context)
                    .load(item.component.image)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(holder.productImage)
                componentHolder.itemView.setOnClickListener { onItemClick(item) }
                componentHolder.addButton.setOnClickListener { onAddClick(item) }

                // Show or hide "Add" button and alternate price based on what screen is displayed
                if (showButton) {
                    componentHolder.addButton.visibility = View.VISIBLE
                    componentHolder.priceAlt.visibility = View.GONE
                    componentHolder.productPrice.visibility = View.VISIBLE
                }
                else {
                    componentHolder.addButton.visibility = View.GONE
                    componentHolder.priceAlt.visibility = View.VISIBLE
                    componentHolder.productPrice.visibility = View.GONE
                }
            }

            is ListItem.BundleItem -> {
                val bundleHolder = holder as BundleViewHolder
                bundleHolder.bundleName.text = item.bundle.name
                bundleHolder.bundlePrice.text = SettingsDataManager.formatPrice(holder.itemView.context, item.bundle.price)
                Glide.with(holder.itemView.context)
                    .load(item.bundle.image)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.bundleImage)
                bundleHolder.itemView.setOnClickListener { onItemClick(item) }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    /**
     * Adds a new component to the list and updates the RecyclerView.
     */
    fun addComponents(component: ComponentEntity) {
        items.add(ListItem.ComponentItem(component))
        notifyItemInserted(items.size - 1)
    }

    /**
     * Adds a new bundle to the list and updates the RecyclerView.\
     */
    fun addBundle(bundle: BundleEntity) {
        items.add(ListItem.BundleItem(bundle))
        notifyItemInserted(items.size - 1)
    }

    /**
     * Removes a component by its URL and notifies the RecyclerView.
     */
    fun removeComponentByUrl(url: String) {
        val index = items.indexOfFirst { it is ListItem.ComponentItem && it.component.url == url }
        if (index != -1) {
            items.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    /**
     * Removes a bundle at a specific position.
     */
    fun removeBundleAt(position: Int) {
        if (position in items.indices && items[position] is ListItem.BundleItem) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    /**
     * Returns the component at a given position, or null if not a component.
     */
    fun getComponentAt(position: Int) {
        (items.getOrNull(position) as? ListItem.ComponentItem)?.component
    }

    /**
     * Returns the [ListItem] at a given position.
     */
    fun getItemAt(position: Int): ListItem = items[position]

    /**
     * Removes an item at a given position, regardless of type.
     */
    fun removeItemAt(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }

    /**
     * Removes a component at a specific position.
     */
    fun removeComponentAt(position: Int) {
        if (position in items.indices && items[position] is ListItem.ComponentItem) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    /**
     * Returns all component items in the adapter as a list.
     */
    fun getAllComponents(): List<ComponentEntity> =
        items.mapNotNull { if (it is ListItem.ComponentItem) it.component else null}

    /**
     * Returns all items in the adapter.
     */
    fun getAllItems(): List<ListItem> {
        return items
    }

    /**
     * Removes all component items and refreshes the RecyclerView.
     */
    fun clearComponents() {
        items.removeAll { it is ListItem.ComponentItem }
        notifyDataSetChanged()
    }

    /**
     * Clears all items (components and bundles) and refreshes the RecyclerView.
     */
    fun clearItems() {
        items.clear()
        notifyDataSetChanged()
    }
}

