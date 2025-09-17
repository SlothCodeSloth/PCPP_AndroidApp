package com.example.pcpartpicker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import androidx.core.view.isVisible

class MainSearchFragment : Fragment() {
    private val viewModel: PartViewModel by activityViewModels {
        PartViewModelFactory((requireActivity().application as MyApplication).api)
    }

    private val productTypes = mapOf(
        "Case" to "case",
        "CPU" to "cpu",
        "Video Card" to "video-card",
        "Memory" to "memory",
        "Monitor" to "monitor",
        "Motherboard" to "motherboard",
        "Keyboard" to "keyboard",
        "Speaker" to "speaker",
        "Thermal Paste" to "thermal-paste",
        "Case Fan" to "case-fan",
        "OS" to "os",
        "CPU Cooler" to "cpu-cooler",
        "Fan Controller" to "fan-controller",
        "UPS" to "ups",
        "Wired Network Card" to "wired-network-card",
        "Headphones" to "headphones",
        "Sound Card" to "sound-card",
        "Internal Hard Drive" to "internal-hard-drive",
        "Mouse" to "mouse",
        "Wireless Network Card" to "wireless-network-card",
        "Power Supply" to "power-supply",
        "Webcam" to "Webcam",
        "External Hard Drive" to "external-hard-drive",
        "Optical Drive" to "optical-drive"
    )

    private lateinit var adapter: ComponentAdapter
    private lateinit var leaveTop: Animation
    private lateinit var arriveTop: Animation

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main_search, container, false)
        val searchText = view.findViewById<EditText>(R.id.searchText)
        val searchButton = view.findViewById<Button>(R.id.searchButton)
        val filterButton = view.findViewById<Button>(R.id.filterButton)
        val toggleButton = view.findViewById<Button>(R.id.toggleButton)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val initialCount = 5
        val allProducts = productTypes.keys.toList()
        val initialList = if(allProducts.size > initialCount) {
            allProducts.take(initialCount) + "Show More..."
        }
        else {
            allProducts
        }
        arriveTop = AnimationUtils.loadAnimation(requireContext(), R.anim.button_enter_top)


        val animationListener = object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {
            }

            override fun onAnimationEnd(p0: Animation?) {
                toggleButton.isEnabled = true
            }

            override fun onAnimationRepeat(p0: Animation?) {
            }
        }

        arriveTop.setAnimationListener(animationListener)

        adapter = ComponentAdapter(
            mutableListOf(),
            onItemClick = { item ->
                if (item is ListItem.ComponentItem) {
                    val component = item.component
                    val intent = DetailActivity.newIntent(requireContext(), component)
                    startActivity(intent)
                }

            },
            onAddClick = { item ->
                if (item !is ListItem.ComponentItem) return@ComponentAdapter
                val component = item.component

                val dao = (requireActivity().application as MyApplication).database.componentDao()
                viewLifecycleOwner.lifecycleScope.launch {
                    val allLists = dao.getAllListsWithComponents()
                    val listNames = allLists.map { it.list.name }
                    if (listNames.isEmpty()) {
                        Toast.makeText(requireContext(), "No Lists Found.", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    SelectListDialog(requireContext(), listNames, "Select a List") { selectedListName ->
                        val matchedList = allLists.find { it.list.name == selectedListName }
                        if (matchedList == null) {
                            Toast.makeText(requireContext(), "List Not Found", Toast.LENGTH_SHORT).show()
                            return@SelectListDialog
                        }
                        /*
                        val componentEntity = ComponentEntity(
                            url = selectedProduct.url,
                            name = selectedProduct.name ?: "Unknown",
                            price = selectedProduct.price ?:"N/A?",
                            image = selectedProduct.image
                        )
                         */

                        viewLifecycleOwner.lifecycleScope.launch {
                            dao.insertComponent(component)
                            dao.insertCrossRef(ListComponentCrossRef(matchedList.list.id, component.url))
                            Toast.makeText(requireContext(), "Added to \"$selectedListName\"", Toast.LENGTH_SHORT).show()
                        }
                    }.show()
                }
            }
        )

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        searchButton.setOnClickListener {
            val query = searchText.text.toString()
            if (query.isNotEmpty()) {
                adapter.clearComponents()
                viewModel.startSearch(query, null, requireContext())
            }
        }

        toggleButton.setOnClickListener {
            toggleButton.isEnabled = false
            if (searchText.isVisible) {
                // searchText.setAnimation(btnLeaveTop)
                searchText.visibility = View.INVISIBLE
                // filter.setAnimation(btnArriveTop)
                filterButton.startAnimation(arriveTop)
                filterButton.visibility = View.VISIBLE
            }
            else {
                // filter.setAnimation(btnLeaveTop)
                filterButton.visibility = View.INVISIBLE
                // searchText(btnArriveTop)
                searchText.startAnimation(arriveTop)
                searchText.visibility = View.VISIBLE
            }
        }

        filterButton.setOnClickListener {
            SelectListDialog(requireContext(), initialList, "Select a Filter") { selectedItem ->
                if (selectedItem == "Show More...") {
                    SelectListDialog(requireContext(), allProducts, "Select a Filter") { fullListItem ->
                        filterButton.text = fullListItem
                        val productType = productTypes[fullListItem] ?: ""
                        adapter.clearComponents()
                        viewModel.startSearch(productType, null, requireContext())
                    }.show()
                }
                else {
                    filterButton.text = selectedItem
                    val query = searchText.text.toString()
                    val productType = productTypes[selectedItem] ?: ""
                    adapter.clearComponents()
                    viewModel.startSearch(productType, null, requireContext())
                }
            }.show()
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                if (viewModel.isLoading.value != true &&
                    layoutManager.findLastVisibleItemPosition() >= adapter.itemCount - 1) {
                    viewModel.loadPage(requireContext())
                }
            }
        })

        viewModel.newParts.observe(viewLifecycleOwner, Observer { newItems ->
            newItems.forEach { part ->
                val entity = ComponentEntity(
                    url = part.url,
                    name = part.name,
                    price = part.price,
                    image = part.image,
                    customPrice = part.customPrice
                )
                adapter.addComponents(entity)
            }
        })

        return view
    }
}