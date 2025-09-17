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

class ComponentListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ComponentAdapter
    private lateinit var listName: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_component_list, container, false)
        recyclerView = view.findViewById(R.id.componentListRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = ComponentAdapter(
            mutableListOf(),
            onItemClick = { item ->
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
            showButton = false
        )

        recyclerView.adapter = adapter
        loadListItems()
        return view
    }

    private fun loadListItems() {
        val dao = (requireActivity().application as MyApplication).database.componentDao()
        lifecycleScope.launch {
            val lists = dao.getAllListsWithComponents()
            val matched = lists.find { it.list.name == listName }
            val componentEntities = matched?.components ?: emptyList()

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
                componentEntities.forEach { component ->
                    adapter.addComponents(component)
                }
            }
        }
    }

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