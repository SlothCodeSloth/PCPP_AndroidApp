package com.example.pcpartpicker

import android.os.Bundle
import android.os.PersistableBundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import org.w3c.dom.Text

/**
 * Main activity for displaying and managing components and bundles within a specific list.
 *
 * This activity provides comprehensive list management functionality including:
 * - Displaying components and bundles in a unified RecyclerView
 * - Swipe to delete functionality for both components and bundles
 * - Creating new bundles from existing components in a list
 * - Adding components to other lists
 * - Real time price calculation and display
 * - Navigation to component detail and bundle detail views
 *
 * The activity handles complex database operations including:
 * - Cross-reference management between lists, components, and bundles
 * - Cascading deletes to maintain data integrity
 * - Component cleanup when no longer referenced by any list or bundle
 */
class ComponentListActivity : AppCompatActivity() {

    /** Name of the list being displayed */
    private lateinit var listName: String

    /** Adapter managing both components and bundles in the RecyclerView */
    private lateinit var adapter: ComponentAdapter

    /** RecyclerView displaying the list items */
    private lateinit var recyclerView: RecyclerView

    /** TextView shown when the list is empty */
    private lateinit var emptyText: TextView

    /** Unique identifier of the list being displayed */
    private var listId: Int = -1

    /**
     * Initializes the activity and sets up all UI components and functionality.
     *
     * This method handles:
     * - UI component initialization
     * - Adapter config with click handlers
     * - Swipe to delete setup
     * - Initial data loading and extraction
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_component_list)

        // Initialize UI elements
        emptyText = findViewById(R.id.emptyTextView)
        recyclerView = findViewById(R.id.componentRecyclerView)
        val listTitle: TextView = findViewById(R.id.listTitleTextView)
        val bundleButton: FloatingActionButton = findViewById(R.id.bundleButton)

        // Extract list information from the launching intent
        listName = intent.getStringExtra("list_name") ?: ""
        listId = intent.getIntExtra("list_id", -1)
        listTitle.text = listName ?: "List"

        recyclerView.layoutManager = LinearLayoutManager(this)

        // Configure adapter with click handling for different item types
        adapter = ComponentAdapter(
            mutableListOf(),
            onItemClick = { item ->
                when (item) {
                    // Navigate to component detail view with list button hidden
                    is ListItem.ComponentItem -> {
                        val intent = DetailActivity.newIntent(this, item.component, hideListButton = true)
                        startActivity(intent)
                    }
                    // Navigate to bundle detail view with context information
                    is ListItem.BundleItem -> {
                        val intent = BundleActivity.newIntent(this, item.bundle).apply {
                            putExtra("list_name", listName)
                            putExtra("list_id", listId)
                        }
                        startActivity(intent)
                    }
                }
            },

            // Handle adding components to other lists
            onAddClick = { selectedItem ->
                // Only allow adding ComponentItems to other lists
                if (selectedItem !is ListItem.ComponentItem) return@ComponentAdapter
                val selectedComponent = selectedItem.component

                val dao = (application as MyApplication).database.componentDao()
                lifecycleScope.launch {
                    // Retrieve all available lists for selection
                    val allLists = dao.getAllListsWithComponents()
                    val listNames = allLists.map { it.list.name }

                    if (listNames.isEmpty()) {
                        Toast.makeText(this@ComponentListActivity, "No Lists Found", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    // Show list selection dialog
                    SelectListDialog(this@ComponentListActivity, listNames, "Select a List") { selectedListName ->
                        val matchedList = allLists.find { it.list.name == selectedListName }
                        if (matchedList == null) {
                            Toast.makeText(this@ComponentListActivity, "List Not Found", Toast.LENGTH_SHORT).show()
                            return@SelectListDialog
                        }

                        // Add component to the selected list
                        lifecycleScope.launch {
                            dao.insertComponent(selectedComponent)
                            dao.insertCrossRef(ListComponentCrossRef(matchedList.list.id, selectedComponent.url))
                            Toast.makeText(this@ComponentListActivity, "Added to \"$selectedListName\"", Toast.LENGTH_SHORT).show()
                        }
                    }.show()
                }
            },
            showButton = false // Hide add buttons
        )

        // Configure swipe to delete for components and bundles
        val itemTouchHelper = ItemTouchHelper(object: ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false // No drag-and-drop reordering
            }

            /**
             * Handles swipe to delete for components and bundles.
             */
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val item = adapter.getItemAt(position)

                when (item) {
                    is ListItem.ComponentItem -> {
                        handleComponentSwipeDelete(item.component, position)
                    }
                    is ListItem.BundleItem -> {
                        handleBundleSwipeDelete(item.bundle, position)
                    }
                }
            }
        })

        itemTouchHelper.attachToRecyclerView(recyclerView)
        recyclerView.adapter = adapter

        // Set activity title from intent data
        val listName = intent.getStringExtra("list_name")
        title = listName ?: "List"

        // Load initial data
        if (listName != null) {
            loadInitialData(listName)
        } else {
            Log.e("ComponentListActivity", "No list_name provided in intent.")
        }

        // Set up bundle creation button
        bundleButton.setOnClickListener {
            showCreateBundleDialog()
        }
    }

    /**
     * Handles the deletion of a component when swiped.
     *
     * Process:
     * 1. Remove cross-reference between list and component
     * 2. Check if component is used in other lists
     * 3. Delete component entirely if not used elsewhere
     * 4. Update UI and recalculate total price
     *
     * @param component The component being deleted
     * @param position The position in the adapter
     */
    private fun handleComponentSwipeDelete(component: ComponentEntity, position: Int) {
        val dao = (application as MyApplication).database.componentDao()
        lifecycleScope.launch {
            val allLists = dao.getAllListsWithComponents()
            val matchedList = allLists.find { it.list.name == listName }

            matchedList?.let {
                // Remove the cross reference
                dao.deleteCrossRef(matchedList.list.id, component.url)

                // Check if component is referenced by other lists
                val usageCount = dao.getListCountForComponent(component.url)
                if (usageCount == 0) {
                    // Delete component if it is not referenced elsewhere
                    dao.deleteComponent(component.url)
                }

                // Update UI
                adapter.removeComponentAt(position)
                updateTotalPrice()
                Toast.makeText(this@ComponentListActivity, "Component removed from list", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Handles the deletion of a bundle when swiped.
     *
     * Complex process involving:
     * 1. Delete the bundle entity
     * 2. Evaluate each component in the bundle for cleanup
     * 3. Delete components that are only used by this bundle
     * 4. Update UI on main thread
     *
     * @param bundle The bundle being deleted
     * @param position The position in the adapter
     */
    private fun handleBundleSwipeDelete(bundle: BundleEntity, position: Int) {
        val bundleDao = (application as MyApplication).database.bundleDao()
        val componentDao = (application as MyApplication).database.componentDao()

        lifecycleScope.launch {
            // Delete the bundle entity
            bundleDao.delete(bundle)

            // Handle component cleanup for bundle components
            val componentsInBundle = bundleDao.getBundleWithComponents(bundle.bundleId)?.components ?: emptyList()
            componentsInBundle.forEach { componentEntity ->
                val listUsageCount = componentDao.getListCountForComponent(componentEntity.url)
                val bundleUsageCount = bundleDao.getBundleCountForComponent(componentEntity.url)

                // Only delete component if this was its last reference
                if (listUsageCount == 0 && bundleUsageCount == 1) {
                    componentDao.deleteComponent(componentEntity.url)
                }
            }

            // Update UI on main thread
            runOnUiThread {
                adapter.removeBundleAt(position)
                updateTotalPrice()
                Toast.makeText(this@ComponentListActivity, "Bundle removed from list", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Loads initial component and bundle data for the specified list.
     *
     * @param listName The name of the list to load data for
     */
    private fun loadInitialData(listName: String) {
        val dao = (application as MyApplication).database.componentDao()
        lifecycleScope.launch {
            val allLists = dao.getAllListsWithComponents()
            val matchedList = allLists.find { it.list.name == listName}

            matchedList?.components?.let { components ->
                // Legacy code: Convert to Component.Part (currently unused)
                val parts = components.map {
                    Component.Part(
                        name = it.name,
                        url = it.url,
                        price = it.price,
                        image = it.image,
                        customPrice = it.customPrice
                    )
                }

                // Add components to adapter
                components.forEach { componentEntity ->
                    adapter.addComponents(componentEntity)
                }

                // Show/hide empty state based on data presence
                emptyText.visibility = if (parts.isEmpty()) View.VISIBLE else View.GONE
                updateTotalPrice()
            }
        }
    }

    /**
     * Calculates and displays the total price of all items (components and bundles) in the list.
     *
     * Handles:
     * - Custom pricing for components and bundles
     * - Displaying currency symbol based on user preferences
     */
    private fun updateTotalPrice() {
        val totalPrice = findViewById<TextView>(R.id.priceTextView)
        val total = adapter.getAllItems().sumOf { item ->
            when (item) {
                is ListItem.ComponentItem -> {
                    SettingsDataManager.getTotalPrice(this, item.component.price, item.component.customPrice)
                }
                is ListItem.BundleItem -> {
                    item.bundle.price?.replace(Regex("[^\\d.]"), "")?.toDoubleOrNull() ?: 0.0
                }
            }
        }
        val currencySymbol = SettingsDataManager.getCurrencySymbol(this)
        totalPrice.text = "Total: %s%.2f".format(currencySymbol, total)
    }

    /**
     * Displays a dialog for creating a new bundle from existing components in the list.
     *
     * Features:
     * - Ensures required data is gathered by disabling save button if not given.
     * - Component selection provided from current list
     * - Database operations for bundle creation
     */
    private fun showCreateBundleDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_bundle, null)

        // Initialize dialog UI elements
        val bundleName: EditText = dialogView.findViewById(R.id.bundleNameTextView)
        val bundleVendor: EditText = dialogView.findViewById(R.id.bundleVendorTextView)
        val bundlePrice: EditText = dialogView.findViewById(R.id.bundlePriceTextView)
        val bundleUrl: EditText = dialogView.findViewById(R.id.bundleURLTextView)
        val bundleImage: EditText = dialogView.findViewById(R.id.bundleImageTextView)
        val saveButton: Button = dialogView.findViewById(R.id.saveButton)
        val cancelButton: Button = dialogView.findViewById(R.id.cancelButton)

        // Disable save button if required fields are empty
        saveButton.isEnabled = false
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val nameFilled = bundleName.text.isNotEmpty()
                val priceFilled = bundlePrice.text.isNotEmpty()
                val urlFilled = bundleUrl.text.isNotEmpty()
                val vendorFilled = bundleVendor.text.isNotEmpty()
                // Note: Image is optional as mentioned in the comment
                saveButton.isEnabled = nameFilled && priceFilled && urlFilled && vendorFilled
            }

            override fun afterTextChanged(p0: Editable?) {}
        }

        // Attach text watchers to all required fields
        bundleName.addTextChangedListener(textWatcher)
        bundleVendor.addTextChangedListener(textWatcher)
        bundlePrice.addTextChangedListener(textWatcher)
        bundleUrl.addTextChangedListener(textWatcher)
        bundleImage.addTextChangedListener(textWatcher)

        // Create and configure dialog
        val dialog = AlertDialog.Builder(this, R.style.RoundCornerDialog)
            .setView(dialogView)
            .create()

        // Handle save button click
        saveButton.setOnClickListener {
            val name = bundleName.text.toString()
            val vendor = bundleVendor.text.toString()
            val price = bundlePrice.text.toString()
            val url = bundleUrl.text.toString()
            val image = bundleImage.text.toString()

            lifecycleScope.launch {
                val bundleDao = (application as MyApplication).database.bundleDao()
                val componentDao = (application as MyApplication).database.componentDao()

                // Find the current list for bundle creation
                val matchedList = componentDao.getListWithItems(listName)
                if (matchedList == null) {
                    Toast.makeText(this@ComponentListActivity, "List not found", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val listId = matchedList.list.id

                // Show component selection dialog
                showComponentSelectionDialog(matchedList.components) { selectedComponents ->
                    handleBundleCreation(selectedComponents, name, vendor, price, url, image, matchedList, dialog)
                }
            }
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

        // Set dialog width to 90% of screen width for better UX
        val displayMetrics = DisplayMetrics()
        (this as? android.app.Activity)?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels
        val desiredWidth = (screenWidth * 0.9).toInt()
        dialog.window?.setLayout(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    /**
     * Handles the creation of a bundle from selected components. Removed selected components
     * from the list.
     *
     * @param selectedComponents Components to include in the bundle
     * @param name Bundle name
     * @param vendor Bundle vendor
     * @param price Bundle price
     * @param url Bundle URL
     * @param image Bundle image URL
     * @param matchedList The current list context
     * @param dialog The dialog to dismiss
     */
    private fun handleBundleCreation(
        selectedComponents: List<ComponentEntity>,
        name: String,
        vendor: String,
        price: String,
        url: String,
        image: String,
        matchedList: ListWithItems,
        dialog: AlertDialog
    ) {
        // Validate selection
        if (selectedComponents.isEmpty()) {
            Toast.makeText(this@ComponentListActivity, "No components selected", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            return
        }

        lifecycleScope.launch {
            val bundleDao = (application as MyApplication).database.bundleDao()
            val componentDao = (application as MyApplication).database.componentDao()

            // Create new bundle entity
            val newBundle = BundleEntity(
                name = name,
                listId = matchedList.list.id,
                vendor = vendor,
                price = price,
                url = url,
                image = image
            )

            // Insert bundle and get generated ID
            val bundleId = bundleDao.insert(newBundle).toInt()

            // Create cross references between bundle and selected components
            val bundleCrossRefs = selectedComponents.map { component ->
                BundleComponentCrossRef(bundleId, component.url)
            }
            bundleCrossRefs.forEach { crossRef ->
                bundleDao.insertCrossRef(crossRef)
            }

            // Remove components from the original list
            selectedComponents.forEach { component ->
                componentDao.deleteCrossRef(matchedList.list.id, component.url)
            }

            // Update UI
            runOnUiThread {
                // Remove components from adapter and add the new bundle
                selectedComponents.forEach { component ->
                    adapter.removeComponentByUrl(component.url)
                }
                adapter.addBundle(newBundle)
                emptyText.visibility = if (adapter.getAllItems().isEmpty()) View.VISIBLE else View.GONE
                updateTotalPrice()

                // Navigate to the new bundle view
                val intent = BundleActivity.newIntent(this@ComponentListActivity, newBundle).apply {
                    putExtra("list_name", listName)
                    putExtra("list_id", matchedList.list.id)
                }
                startActivity(intent)
            }

            dialog.dismiss()
            Toast.makeText(this@ComponentListActivity, "Bundle Created", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Displays a dialog allowing users to select components from the list for bundle creation.
     *
     * @param components Available components to select from
     * @param onConfirm Callback with selected components
     */
    private fun showComponentSelectionDialog(components: List<ComponentEntity>, onConfirm: (List<ComponentEntity>) -> Unit) {
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_select_components, null)
        val recyclerView: RecyclerView = dialogView.findViewById(R.id.componentsRecyclerView)
        val cancelButton: Button = dialogView.findViewById(R.id.cancelButton)
        val saveButton: Button = dialogView.findViewById(R.id.saveButton)

        // Set up selection adapter
        val adapter = ComponentSelectAdapter(components)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        saveButton.setOnClickListener {
            onConfirm(adapter.selectedItems.toList())
            dialog.dismiss()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    /**
     * Called when the activity becomes visible to the user.
     * Reloads data to show changes made in other activities.
     */
    override fun onResume() {
        super.onResume()
        loadComponentsAndBundles()
    }

    /**
     * Reloads all components and bundles for the current list (updates changes).
     */
    private fun loadComponentsAndBundles() {
        val listName = intent.getStringExtra("list_name")
        if (listName != null) {
            val dao = (application as MyApplication).database.componentDao()
            lifecycleScope.launch {
                val allLists = dao.getAllListsWithComponents()
                val matchedList = allLists.find { it.list.name == listName }

                matchedList?.let { listWithItems ->
                    // Clear existing items and reload from database
                    adapter.clearItems()

                    // Use extension method to convert to ListItem objects
                    listWithItems.toListItems().forEach { listItem ->
                        when (listItem) {
                            is ListItem.ComponentItem -> adapter.addComponents(listItem.component)
                            is ListItem.BundleItem -> adapter.addBundle(listItem.bundle)
                        }
                    }

                    // Update empty state visibility
                    emptyText.visibility = if (listWithItems.components.isEmpty() && listWithItems.bundles.isEmpty()) View.VISIBLE else View.GONE
                    updateTotalPrice()
                }
            }
        } else {
            Log.e("ComponentListActivity", "No List Name provided")
        }
    }
}