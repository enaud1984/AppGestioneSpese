package com.example.gestionespese

import PazientiDbHelper
import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.gestionespese.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_speseFisse, R.id.navigation_Progressivi,R.id.navigation_Grafici
            )
        )

        navController.addOnDestinationChangedListener { _, destination, _ ->
            Log.d("Navigation", "Destination changed to ${destination.label}")
            if (destination.id == R.id.navigation_speseFisse) {
                // Imposta il titolo della toolbar o esegui altre azioni specifiche per la pagina fragment_spesefisse.xml
                supportActionBar?.title = "Spese fisse"
            }
        }


        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

    }
}