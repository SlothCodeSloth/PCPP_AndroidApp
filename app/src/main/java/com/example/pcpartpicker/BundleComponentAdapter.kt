package com.example.pcpartpicker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

/**
 * RecyclerView Adapter for displaying a list of [ComponentEntity] items that reside in a [BundleEntity]
 * This adapter is simplified for the [BundleActivity]:
 * - Only displays the name and image of each component.
 * - Hides price, buttons, and other unused UI assets.
 *
 * @property components list of [ComponentEntity] objects to display
 */
class BundleComponentAdapter(
    private val components: List<ComponentEntity>
) : RecyclerView.Adapter<BundleComponentAdapter.ViewHolder>() {

    /**
     * ViewHolder representing a single [ComponentEntity] in the RecyclerView.
     * @property nameText TextView displaying the name of the component.
     * @property imageView ImageView displaying the image of the component.
     */

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.componentName)
        val imageView: ImageView = itemView.findViewById(R.id.componentImage)
    }


    /**
     * Inflates the layout for each [ComponentEntity] item and wraps it in a [ViewHolder].
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_component, parent, false)
        return ViewHolder(view)
    }

    /**
     * Binds a [ComponentEntity] at a given position to a [ViewHolder]
     * - Displays the component's name and image via Glide.
     * - Hides unused elements.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val component = components[position]
        holder.nameText.text = component.name
        Glide.with(holder.itemView.context)
            .load(component.image)
            .placeholder(R.drawable.ic_launcher_background)
            .into(holder.imageView)

        holder.itemView.findViewById<TextView>(R.id.componentPrice)?.visibility = View.GONE
        holder.itemView.findViewById<Button>(R.id.componentButton)?.visibility = View.GONE
        holder.itemView.findViewById<TextView>(R.id.componentPriceAlt)?.visibility = View.GONE
    }

    /**
     * Returns the toal number of [ComponentEntity] items in the list.
     */
    override fun getItemCount(): Int = components.size
}