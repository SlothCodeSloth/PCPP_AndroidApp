package com.example.pcpartpicker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

/**
 * NOT USED IN FINAL VERSION
 */
class BundleAdapter (
    private val bundles: List<BundleWithComponents>
) : RecyclerView.Adapter<BundleAdapter.BundleViewHolder>() {

    inner class BundleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bundleName: TextView = itemView.findViewById(R.id.bundleName)
        val bundlePrice: TextView = itemView.findViewById(R.id.bundlePrice)
        val bundleImage: ImageView = itemView.findViewById(R.id.bundleImage)
        val componentsRecyclerView: RecyclerView = itemView.findViewById(R.id.bundleComponentsRecyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BundleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bundle, parent, false)
        return BundleViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: BundleViewHolder,
        position: Int,
    ) {
        val bundleWithComponents = bundles[position]
        val bundle = bundleWithComponents.bundle
        holder.bundleName.text = bundle.name
        holder.bundlePrice.text = SettingsDataManager.formatPrice(holder.itemView.context, bundle.price)
        Glide.with(holder.itemView.context)
            .load(bundle.image)
            .placeholder(R.drawable.ic_launcher_background)
            .into(holder.bundleImage)

        holder.componentsRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.componentsRecyclerView.adapter = BundleComponentAdapter(bundleWithComponents.components)
    }

    override fun getItemCount() = bundles.size
}