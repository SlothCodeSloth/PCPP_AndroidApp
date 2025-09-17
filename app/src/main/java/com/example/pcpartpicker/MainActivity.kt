package com.example.pcpartpicker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    // Oconomowoc
    private val viewModel: PartViewModel by viewModels { PartViewModelFactory((application as MyApplication).api) }
    private lateinit var viewPager: ViewPager2
    private lateinit var pagerAdapter: MainPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyTheme(this)
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        viewPager = findViewById<ViewPager2>(R.id.viewPager)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        val listNames = mutableListOf("List 1", "List 2")

        val pagerAdapter = MainPagerAdapter(this, listNames)
        viewPager.adapter = pagerAdapter
        viewPager.setCurrentItem(1, false)
        bottomNav.selectedItemId = R.id.search

        viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> bottomNav.selectedItemId = R.id.settings
                    1 -> bottomNav.selectedItemId = R.id.search
                    2 -> bottomNav.selectedItemId = R.id.lists
                }
            }
        })

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.settings -> viewPager.setCurrentItem(0, true)
                R.id.search -> viewPager.setCurrentItem(1, true)
                R.id.lists -> viewPager.setCurrentItem(2, true)
            }
            true
        }
    }
}
