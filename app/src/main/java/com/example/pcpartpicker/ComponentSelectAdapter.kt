package com.example.pcpartpicker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

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

        holder.checkbox.setOnCheckedChangeListener{ _, isChecked ->
            if (isChecked) {
                selectedItems.add(component)
            }
            else {
                selectedItems.remove(component)
            }
        }

        holder.itemView.setOnClickListener {
            holder.checkbox.isChecked = !holder.checkbox.isChecked
        }
    }

    override fun getItemCount() = components.size
}