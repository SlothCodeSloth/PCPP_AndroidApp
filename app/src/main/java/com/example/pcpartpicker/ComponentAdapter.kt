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

    class ComponentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.componentImage)
        val productName: TextView = itemView.findViewById(R.id.componentName)
        val productPrice: TextView = itemView.findViewById(R.id.componentPrice)
        val priceAlt: TextView = itemView.findViewById(R.id.componentPriceAlt)
        val addButton: Button = itemView.findViewById(R.id.componentButton)
    }

    class BundleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bundleName: TextView = itemView.findViewById(R.id.bundleName)
        val bundlePrice: TextView = itemView.findViewById(R.id.bundlePrice)
        val bundleImage: ImageView = itemView.findViewById(R.id.bundleImage)
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ListItem.ComponentItem -> TYPE_COMPONENT
            is ListItem.BundleItem -> TYPE_BUNDLE
        }
    }

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
                Glide.with(holder.itemView.context)
                    .load(item.component.image)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(holder.productImage)
                componentHolder.itemView.setOnClickListener { onItemClick(item) }
                componentHolder.addButton.setOnClickListener { onAddClick(item) }

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

    fun addComponents(component: ComponentEntity) {
        items.add(ListItem.ComponentItem(component))
        notifyItemInserted(items.size - 1)
    }

    fun addBundle(bundle: BundleEntity) {
        items.add(ListItem.BundleItem(bundle))
        notifyItemInserted(items.size - 1)
    }

    fun removeComponentByUrl(url: String) {
        val index = items.indexOfFirst { it is ListItem.ComponentItem && it.component.url == url }
        if (index != -1) {
            items.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun removeBundleAt(position: Int) {
        if (position in items.indices && items[position] is ListItem.BundleItem) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun getComponentAt(position: Int) {
        (items.getOrNull(position) as? ListItem.ComponentItem)?.component
    }

    fun getItemAt(position: Int): ListItem = items[position]

    fun removeItemAt(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }

    fun removeComponentAt(position: Int) {
        if (position in items.indices && items[position] is ListItem.ComponentItem) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun getAllComponents(): List<ComponentEntity> =
        items.mapNotNull { if (it is ListItem.ComponentItem) it.component else null}

    fun getAllItems(): List<ListItem> {
        return items
    }

    fun clearComponents() {
        items.removeAll { it is ListItem.ComponentItem }
        notifyDataSetChanged()
    }

    fun clearItems() {
        items.clear()
        notifyDataSetChanged()
    }
}

