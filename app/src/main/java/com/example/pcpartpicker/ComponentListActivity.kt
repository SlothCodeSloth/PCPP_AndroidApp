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

class ComponentListActivity : AppCompatActivity() {
// Maybe Remove List ID
    private lateinit var listName: String
    private lateinit var adapter: ComponentAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyText: TextView
    private var listId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_component_list)

        emptyText = findViewById(R.id.emptyTextView)
        recyclerView = findViewById(R.id.componentRecyclerView)
        val listTitle: TextView = findViewById(R.id.listTitleTextView)
        val bundleButton: FloatingActionButton = findViewById(R.id.bundleButton)
        listName = intent.getStringExtra("list_name") ?: ""
        listId = intent.getIntExtra("list_id", -1)
        listTitle.text = listName ?: "List"
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ComponentAdapter(
            mutableListOf(),
            onItemClick = { item ->
                when (item) {
                    is ListItem.ComponentItem -> {
                        val intent = DetailActivity.newIntent(this, item.component, hideListButton = true)
                        startActivity(intent)
                    }

                    is ListItem.BundleItem -> {
                        val intent = BundleActivity.newIntent(this, item.bundle).apply {
                            putExtra("list_name", listName)
                            putExtra("list_id", listId)
                        }
                        startActivity(intent)
                    }
                }
            },

            onAddClick = { selectedItem ->
                if (selectedItem !is ListItem.ComponentItem) return@ComponentAdapter
                val selectedComponent = selectedItem.component

                val dao = (application as MyApplication).database.componentDao()
                lifecycleScope.launch {
                    val allLists = dao.getAllListsWithComponents()
                    val listNames = allLists.map { it.list.name }
                    if (listNames.isEmpty()) {
                        Toast.makeText(this@ComponentListActivity, "No Lists Found", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    SelectListDialog(this@ComponentListActivity, listNames, "Select a List") { selectedListName ->
                        val matchedList = allLists.find { it.list.name == selectedListName }
                        if (matchedList == null) {
                            Toast.makeText(this@ComponentListActivity, "List Not Found", Toast.LENGTH_SHORT).show()
                            return@SelectListDialog
                        }

                        lifecycleScope.launch {
                            dao.insertComponent(selectedComponent)
                            dao.insertCrossRef(ListComponentCrossRef(matchedList.list.id, selectedComponent.url))
                            Toast.makeText(this@ComponentListActivity, "Added to \"$selectedListName\"", Toast.LENGTH_SHORT).show()
                        }
                    }.show()
                }
            },
            showButton = false
        )

        // New
        val itemTouchHelper = ItemTouchHelper(object: ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val item = adapter.getItemAt(position)

                when (item) {
                    is ListItem.ComponentItem -> {
                        val component = item.component
                        val dao = (application as MyApplication).database.componentDao()
                        lifecycleScope.launch {
                            val allLists = dao.getAllListsWithComponents()
                            val matchedList = allLists.find { it.list.name == listName }

                            matchedList?.let {
                                // Delete Cross Reference
                                dao.deleteCrossRef(matchedList.list.id, component.url)

                                // Delete Component if not in other lists
                                val usageCount = dao.getListCountForComponent(component.url)
                                if (usageCount == 0) {
                                    dao.deleteComponent(component.url)
                                }
                                adapter.removeComponentAt(position)
                                updateTotalPrice()
                                Toast.makeText(this@ComponentListActivity, "Component removed from list", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    is ListItem.BundleItem -> {
                        val bundle = item.bundle
                        val bundleDao = (application as MyApplication).database.bundleDao()
                        val componentDao = (application as MyApplication).database.componentDao()
                        lifecycleScope.launch {
                            // Delete the Bundle
                            bundleDao.delete(bundle)

                            // Delete Components in the bundle
                            val componentsInBundle = bundleDao.getBundleWithComponents(bundle.bundleId)?.components ?: emptyList()
                            componentsInBundle.forEach { componentEntity ->
                                val listUsageCount = componentDao.getListCountForComponent(componentEntity.url)
                                val bundleUsageCount = bundleDao.getBundleCountForComponent(componentEntity.url)

                                if (listUsageCount == 0 && bundleUsageCount == 1) {
                                    componentDao.deleteComponent(componentEntity.url)
                                }
                            }

                            runOnUiThread {
                                adapter.removeBundleAt(position)
                                updateTotalPrice()
                                Toast.makeText(this@ComponentListActivity, "Bundle removed from list", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        })

        itemTouchHelper.attachToRecyclerView(recyclerView)
        recyclerView.adapter = adapter

        val listName = intent.getStringExtra("list_name")
        title = listName ?: "List"

        if (listName != null) {
            val dao = (application as MyApplication).database.componentDao()
            lifecycleScope.launch {
                val allLists = dao.getAllListsWithComponents()
                val matchedList = allLists.find { it.list.name == listName}
                matchedList?.components?.let { components ->
                    val parts = components.map {
                        Component.Part(
                            name = it.name,
                            url = it.url,
                            price = it.price,
                            image = it.image,
                            customPrice = it.customPrice
                        )
                    }

                    components.forEach { componentEntity ->
                        adapter.addComponents(componentEntity)
                    }
                    // adapter.addComponents(parts)
                    emptyText.visibility = if (parts.isEmpty()) View.VISIBLE else View.GONE

                    updateTotalPrice()
                }
            }
        }
        else {
            Log.e("ComponentListActivity", "No list_name provided in intent.")
        }

        bundleButton.setOnClickListener {
            showCreateBundleDialog()
        }
    }

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

    private fun showCreateBundleDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_bundle, null)
        val bundleName: EditText = dialogView.findViewById(R.id.bundleNameTextView)
        val bundleVendor: EditText = dialogView.findViewById(R.id.bundleVendorTextView)
        val bundlePrice: EditText = dialogView.findViewById(R.id.bundlePriceTextView)
        val bundleUrl: EditText = dialogView.findViewById(R.id.bundleURLTextView)
        val bundleImage: EditText = dialogView.findViewById(R.id.bundleImageTextView)
        val saveButton: Button = dialogView.findViewById(R.id.saveButton)
        val cancelButton: Button = dialogView.findViewById(R.id.cancelButton)

        saveButton.isEnabled = false
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val nameFilled = bundleName.text.isNotEmpty()
                val priceFilled = bundlePrice.text.isNotEmpty()
                val urlFilled = bundleUrl.text.isNotEmpty()
                val vendorFilled = bundleVendor.text.isNotEmpty()
                val imageFilled = bundleImage.text.isNotEmpty()
                // Image is optional
                saveButton.isEnabled = nameFilled && priceFilled && urlFilled && vendorFilled
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        }

        bundleName.addTextChangedListener(textWatcher)
        bundleVendor.addTextChangedListener(textWatcher)
        bundlePrice.addTextChangedListener(textWatcher)
        bundleUrl.addTextChangedListener(textWatcher)
        bundleImage.addTextChangedListener(textWatcher)

        val dialog = AlertDialog.Builder(this, R.style.RoundCornerDialog)
            .setView(dialogView)
            .create()

        saveButton.setOnClickListener {
            val name = bundleName.text.toString()
            val vendor = bundleVendor.text.toString()
            val price = bundlePrice.text.toString()
            val url = bundleUrl.text.toString()
            val image = bundleImage.text.toString()

            lifecycleScope.launch {
                val bundleDao = (application as MyApplication).database.bundleDao()
                val componentDao = (application as MyApplication).database.componentDao()

                //val allLists = componentDao.getAllListsWithComponents()
                //val matchedList = allLists.find { it.list.name == listName }
                val matchedList = componentDao.getListWithItems(listName)
                if (matchedList == null) {
                    Toast.makeText(this@ComponentListActivity, "List not found", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val listId = matchedList.list.id

                showComponentSelectionDialog(matchedList.components) { selectedComponents ->
                    // Handle no components selected
                    if (selectedComponents.isEmpty()) {
                        Toast.makeText(this@ComponentListActivity, "No components selected", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        return@showComponentSelectionDialog
                    }

                    lifecycleScope.launch {
                        // Create new bundle
                        val newBundle = BundleEntity(
                            name = name,
                            listId = listId,
                            vendor = vendor,
                            price = price,
                            url = url,
                            image = image
                        )

                        val listId = matchedList.list.id
                        val bundleId = bundleDao.insert(newBundle).toInt()
                        val bundleCrossRefs = selectedComponents.map { component ->
                            BundleComponentCrossRef(bundleId, component.url)
                        }
                        bundleCrossRefs.forEach { crossRef ->
                            bundleDao.insertCrossRef(crossRef)
                        }

                        selectedComponents.forEach { component ->
                            componentDao.deleteCrossRef(listId, component.url)
                        }

                        runOnUiThread {
                            selectedComponents.forEach { component ->
                                adapter.removeComponentByUrl(component.url)
                            }
                            adapter.addBundle(newBundle)
                            emptyText.visibility = if (adapter.getAllItems().isEmpty()) View.VISIBLE else View.GONE
                            updateTotalPrice()
                            val intent = BundleActivity.newIntent(this@ComponentListActivity, newBundle).apply {
                                putExtra("list_name", listName)
                                putExtra("list_id", listId)
                            }
                            startActivity(intent)
                        }
                        dialog.dismiss()
                        Toast.makeText(this@ComponentListActivity, "Bundle Created", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()

        val displayMetrics = DisplayMetrics()
        (this as? android.app.Activity)?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels
        val desiredWidth = (screenWidth * 0.9).toInt()
        dialog.window?.setLayout(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT)

    }

    private fun showComponentSelectionDialog(components: List<ComponentEntity>, onConfirm: (List<ComponentEntity>) -> Unit) {
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_select_components, null)
        val recyclerView: RecyclerView = dialogView.findViewById(R.id.componentsRecyclerView)
        val cancelButton: Button = dialogView.findViewById(R.id.cancelButton)
        val saveButton: Button = dialogView.findViewById(R.id.saveButton)

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

    override fun onResume() {
        super.onResume()
        loadComponentsAndBundles()
    }

    private fun loadComponentsAndBundles() {
        val listName = intent.getStringExtra("list_name")
        if (listName != null) {
            val dao = (application as MyApplication).database.componentDao()
            lifecycleScope.launch {
                val allLists = dao.getAllListsWithComponents()
                val matchedList = allLists.find { it.list.name == listName }

                matchedList?.let { listWithItems ->
                    adapter.clearItems()
                    listWithItems.toListItems().forEach { listItem ->
                        when (listItem) {
                            is ListItem.ComponentItem -> adapter.addComponents(listItem.component)
                            is ListItem.BundleItem -> adapter.addBundle(listItem.bundle)
                        }
                    }
                    emptyText.visibility = if (listWithItems.components.isEmpty() && listWithItems.bundles.isEmpty()) View.VISIBLE else View.GONE
                    updateTotalPrice()
                }
            }
        }
        else {
            Log.e("ComponentListActivity", "No List Name provided")
        }
    }
}