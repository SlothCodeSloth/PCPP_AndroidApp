package com.example.pcpartpicker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

/**
 * Fragment that displays a read-only list of components for a specific list.
 *
 * This fragment provides a simplified view of components without the full functionality
 * of ComponentListActivity (no swipe to delete, no bundle creation, no add-to-list functionality).
 *
 * Key features:
 * - Displays components from a specified list
 * - Allows navigation to component detail view on item click
 * - Uses the same ComponentAdapter but with limited functionality
 */
class ComponentListFragment : Fragment() {

    /** RecyclerView that displays the list of components */
    private lateinit var recyclerView: RecyclerView

    /** Adapter for managing component display in the RecyclerView */
    private lateinit var adapter: ComponentAdapter

    /** Name of the list whose components should be displayed */
    private lateinit var listName: String

    /**
     * Creates and initializes the fragment's view hierarchy.
     * Sets up the RecyclerView with a ComponentAdapter configured to be read only.
     *
     * @param inflater The LayoutInflater object that can be used to inflate views
     * @param container The parent view that the fragment's UI should be attached to
     * @param savedInstanceState Previously saved state of the fragment
     * @return The root view for the fragment's UI
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_component_list, container, false)

        // Initialize RecyclerView with linear layout
        recyclerView = view.findViewById(R.id.componentListRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Configure adapter for read-only display
        adapter = ComponentAdapter(
            mutableListOf(),
            onItemClick = { item ->
                // Handle component item clicks by navigating to detail view
                if (item is ListItem.ComponentItem) {
                    val component = item.component
                    val intent = DetailActivity.newIntent(requireContext(), component)
                    startActivity(intent)
                }
                // val intent = DetailActivity.newIntent(requireContext(), part)
                // startActivity(intent)
            },
            onAddClick = {
            },
            // Hide add buttons since this is read-only
            showButton = false
        )

        recyclerView.adapter = adapter
        loadListItems()
        return view
    }

    /**
     * Loads and displays components from the specified list.
     *
     * This method:
     * 1. Retrieves all lists with their components from the database
     * 2. Finds the list matching the fragment's listName
     * 3. Converts ComponentEntity objects to the format expected by the adapter
     * 4. Populates the adapter with the component data
     */
    private fun loadListItems() {
        val dao = (requireActivity().application as MyApplication).database.componentDao()
        lifecycleScope.launch {
            // Retrieve all lists with their associated components
            val lists = dao.getAllListsWithComponents()

            // Find the specific list we want to display
            val matched = lists.find { it.list.name == listName }
            val componentEntities = matched?.components ?: emptyList()

            // Process and display the components if the list was found
            matched?.components?.let { components: List<ComponentEntity> ->
                val parts = components.map {
                    Component.Part (
                        name = it.name,
                        url = it.url,
                        price = it.price,
                        image = it.image
                    )
                }
                //adapter.addComponents(parts)
                // Add ComponentEntity objects directly to adapter
                componentEntities.forEach { component ->
                    adapter.addComponents(component)
                }
            }
        }
    }

    /**
     * Companion object containing factory method and constants for fragment creation.
     */
    companion object {
        private const val ARG_LIST_NAME = "list_name"

        fun newInstance(listName: String): ComponentListFragment {
            val fragment = ComponentListFragment()
            val args = Bundle()
            args.putString(ARG_LIST_NAME, listName)
            fragment.arguments = args
            return fragment
        }
    }
}