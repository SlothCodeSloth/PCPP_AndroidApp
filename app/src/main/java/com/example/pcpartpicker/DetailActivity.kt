package com.example.pcpartpicker

import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.util.copy
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch

// Rename to ComponentActivity

class DetailActivity : AppCompatActivity() {

    private val viewModel: PartViewModel by viewModels { PartViewModelFactory((application as MyApplication).api) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContentView(R.layout.activity_detail)

        // Acquire attributes from XML file
        val detailImage: ImageView = findViewById(R.id.detailImage)
        val detailName: TextView = findViewById(R.id.detailName)
        val detailPrice: TextView = findViewById(R.id.detailPrice)
        val detailLink: Button = findViewById(R.id.detailLink)
        val specTable: TableLayout = findViewById(R.id.specTable)
        val listButton: Button = findViewById(R.id.listButton)
        val settingsButton: ImageButton = findViewById(R.id.settingsButton)
        val customLinkButton: Button = findViewById(R.id.customLinkButton)
        specTable.removeAllViews()

        // Get Data from Main Activity (Intent)
        val name = intent.getStringExtra("product_name")
        val price = intent.getStringExtra("product_price")
        val image = intent.getStringExtra("product_image")
        val url = intent.getStringExtra("product_url") ?: return
        val hideListButton = intent.getBooleanExtra(HIDE_LIST_BUTTON_KEY, false)
        val settingsSwitch = SettingsDataManager.getSavedSwitchState(this)
        val dao = (application as MyApplication).database.componentDao()
        val currencySymbol = SettingsDataManager.getCurrencySymbol(this@DetailActivity)

        // Observe the LiveData for the component with the given URL. Fill in all Component Details.
        dao.getComponentByUrl(url).observe(this) { componentEntity ->
            componentEntity?.let { component ->
                val settingsSwitch = SettingsDataManager.getSavedSwitchState(this)
                val displayPrice = SettingsDataManager.getDisplayPrice(
                    this@DetailActivity,
                    component.price,
                    component.customPrice
                )
                detailPrice.text = displayPrice
                customLinkButton.text = component.customVendor ?: "View Store"
                customLinkButton.tag = component.customUrl


                // Show the dialog for setting Custom Details for a Component. These override the
                // information from the API in the UI.
                settingsButton.setOnClickListener {
                    showCustomDetailsDialog(component.customPrice, component.customUrl, component.customVendor) { newPrice, newLink, newVendor ->
                        lifecycleScope.launch {
                            dao.updateComponentCustomData(url, newPrice, newLink, newVendor)
                        }
                    }
                }
            }
        }

        /*
        lifecycleScope.launch {
            val componentEntity = dao.getComponentByUrl(url ?: return@launch)
            settingsButton.setOnClickListener {
                val currentVendor = componentEntity?.customVendor
                val currentPrice = componentEntity?.customPrice
                val currentLink = componentEntity?.customUrl
                showCustomDetailsDialog(currentPrice, currentLink, currentVendor) { newPrice, newLink, newVendor ->
                    lifecycleScope.launch {
                        dao.updateComponentCustomData(url, newPrice, newLink, newVendor)
                        customLinkButton.text = newVendor ?: ""
                        customLinkButton.tag = newLink ?: ""

                        if (!newPrice.isNullOrEmpty()) {
                            detailPrice.text = "${currencySymbol}${newPrice} (${currencySymbol}${price})"
                        }
                        else {
                            detailPrice.text = "${currencySymbol}${price}"
                        }
                    }
                }
            }

            componentEntity?.let {
                if (hideListButton && settingsSwitch && !it.customPrice.isNullOrEmpty()) {
                    detailPrice.text = "${currencySymbol}${it.customPrice} (${currencySymbol}${price})"
                }
                else {
                    detailPrice.text = "${currencySymbol}${price}"
                }
                customLinkButton.text = it.customVendor ?: "View Store"
                customLinkButton.tag = it.customUrl
            }
        }
         */

        // Alter the visibility of the "Add to List" Button and the "customPrice" TextView.
        if (hideListButton) {
            listButton.visibility = View.GONE
            if (settingsSwitch) {
                settingsButton.visibility = View.VISIBLE
                customLinkButton.visibility = View.VISIBLE
            }
            else {
                settingsButton.visibility = View.GONE
                customLinkButton.visibility = View.GONE
            }
        }
        else {
            listButton.visibility = View.VISIBLE
        }


        detailName.text = name
        Glide.with(this)
            .load(image)
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .into(detailImage)

        // Launch a browser with the URL of the component when the Button is pressed.
        detailLink.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(browserIntent)
        }

        lifecycleScope.launch {
            try {
                val product = viewModel.fetchProduct(url ?: return@launch)
                //val specs = product.specs.entries.joinToString ("\n"){ "${it.key}: ${it.value}" }
                //detailSpecs.text = specs
                for ((key ,value) in product.specs.entries) {
                    val row = TableRow(this@DetailActivity)

                    val keyText = TextView(this@DetailActivity)
                    keyText.text = key
                    keyText.setPadding(8, 8, 16, 8)
                    keyText.setTypeface(null, Typeface.BOLD)

                    val valueText = TextView(this@DetailActivity)
                    valueText.text = value
                    valueText.setPadding(8, 8, 8, 8)

                    row.addView(keyText)
                    row.addView(valueText)

                    specTable.addView(row)
                }
            }
            catch (e: Exception) {
                Log.e("DetailActivity", "Error fetching product details: ${e.message}")
            }
        }

        listButton.setOnClickListener {
            lifecycleScope.launch {
                val allLists = dao.getAllListsWithComponents()
                val listNames = allLists.map { it.list.name }

                if (listNames.isEmpty()) {
                    Toast.makeText(this@DetailActivity, "No lists found.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                SelectListDialog(this@DetailActivity, listNames, "Select a List") { selectedListName ->
                    val matchedList = allLists.find { it.list.name == selectedListName }
                    if (matchedList == null) {
                        Toast.makeText(this@DetailActivity, "Selected list not found.", Toast.LENGTH_SHORT).show()
                        return@SelectListDialog
                    }

                    val componentEntity = ComponentEntity(
                        url = url!!,
                        name = name ?: "Unknown",
                        price = price ?: "N/A",
                        image = image
                    )

                    lifecycleScope.launch {
                        dao.insertComponent(componentEntity)
                        dao.insertCrossRef(ListComponentCrossRef(matchedList.list.id, url))

                        Toast.makeText(this@DetailActivity, "Added to \"$selectedListName\"", Toast.LENGTH_SHORT).show()
                    }
                }.show()
            }
        }

        // Add clickable button
        customLinkButton.setOnClickListener {
            val link = it.tag as? String
            if (!link.isNullOrEmpty()) {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                startActivity(browserIntent)
            }
            else {
                Toast.makeText(this, "No custom link set.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val HIDE_LIST_BUTTON_KEY = "hide_list_button"

        /**
         * Creates a new [Intent] to start [DetailActivity]
         * This method allows for passing necessary data to the Activity.
         * @param context The context to start the Activity from.
         * @param component The [ComponentEntity] to display in the Activity.
         * @param hideListButton A boolean denoting whether to hide the "Add to List" button.
         *                       Defaults to 'false' and only true when called from the Search Fragment.
         * @return An [Intent] configured to start [DetailActivity] with the necessary data.
         */
        fun newIntent(context: android.content.Context, component: ComponentEntity, hideListButton: Boolean = false): Intent {
            return Intent(context, DetailActivity::class.java).apply {
                putExtra("product_name", component.name)
                putExtra("product_url", component.url)
                putExtra("product_price", component.price)
                putExtra("product_image", component.image)
                putExtra(HIDE_LIST_BUTTON_KEY, hideListButton)
            }
        }
    }

    /**
     * Shows a dialog for the user to input or edit custom component details
     * This dialog allows for setting a custom Vendor, Price, and URL for a component.
     *
     * @param currentCustomPrice The initial custom price to display in the dialog. Null if not set.
     * @param currentCustomLink The initial custom URL to display in the dialog. Null if not set.
     * @param currentCustomVendor The initial custom Vendor to display in the dialog. Null if not set.
     * @param onSave A callback function to be invoked when the user is done editing the details.
     */
    private fun showCustomDetailsDialog(currentCustomPrice: String?, currentCustomLink: String?, currentCustomVendor: String?, onSave: (String?, String?, String?) -> Unit) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_custom, null)
        val customVendor: EditText = dialogView.findViewById(R.id.customStoreTextView)
        val customPrice : EditText = dialogView.findViewById(R.id.customPriceTextView)
        val customLink: EditText = dialogView.findViewById(R.id.customURLTextView)
        val saveButton: Button = dialogView.findViewById(R.id.saveButton)
        val cancelButton: Button = dialogView.findViewById(R.id.cancelButton)

        customVendor.setText(currentCustomVendor)
        customPrice.setText(currentCustomPrice)
        customLink.setText(currentCustomLink)

        saveButton.isEnabled = false
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val priceFilled = customPrice.text.isNotEmpty()
                val linkFilled = customLink.text.isNotEmpty()
                saveButton.isEnabled = priceFilled && linkFilled
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }

        customVendor.addTextChangedListener(textWatcher)
        customPrice.addTextChangedListener(textWatcher)
        customLink.addTextChangedListener(textWatcher)

        val dialog = AlertDialog.Builder(this, R.style.RoundCornerDialog)
            .setView(dialogView)
            .create()

        saveButton.setOnClickListener {
            val newVendor = customVendor.text.toString().ifEmpty { null }
            val newPrice = customPrice.text.toString().ifEmpty { null }
            val newLink = customLink.text.toString().ifEmpty { null }
            onSave(newPrice, newLink, newVendor)
            dialog.dismiss()
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
}