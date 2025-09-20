package com.example.pcpartpicker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

/**
 * RecyclerView adapter for displaying a selectable list of components.
 *
 * This adapter is specifically designed for choosing components to include in a new bundle,
 * offered through a checkbox based selection.
 *
 * @param components The list of ComponentEntity objects to display
 */
class ComponentSelectAdapter(
    private val components: List<ComponentEntity>
) : RecyclerView.Adapter<ComponentSelectAdapter.ViewHolder>() {

    val selectedItems = mutableSetOf<ComponentEntity>()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.componentImage)
        val name: TextView = itemView.findViewById(R.id.componentName)
        val price: TextView = itemView.findViewById(R.id.componentPrice)
        val checkbox: CheckBox = itemView.findViewById(R.id.componentCheckBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_component_select, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val component = components[position]

        // Load text and images
        holder.name.text = component.name
        holder.price.text = component.price

        if (!component.image.isNullOrEmpty()) {
            Glide.with(holder.image.context)
                .load(component.image)
                .into(holder.image)
        }
        else {
            holder.image.setImageResource(R.drawable.ic_launcher_background)
        }

        // Toggle the checkbox when tapped
        holder.checkbox.setOnCheckedChangeListener{ _, isChecked ->
            if (isChecked) {
                selectedItems.add(component)
            }
            else {
                selectedItems.remove(component)
            }
        }

        // Toggle the checkbox when the item is tapped
        holder.itemView.setOnClickListener {
            holder.checkbox.isChecked = !holder.checkbox.isChecked
        }
    }

    override fun getItemCount() = components.size
}