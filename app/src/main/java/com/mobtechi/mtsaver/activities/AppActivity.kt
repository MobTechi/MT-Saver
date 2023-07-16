package com.mobtechi.mtsaver.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.mobtechi.mtsaver.Constants.higherSdkStoragePermissionCode
import com.mobtechi.mtsaver.Functions.saveStoragePathPref
import com.mobtechi.mtsaver.Functions.toast
import com.mobtechi.mtsaver.R
import com.mobtechi.mtsaver.databinding.AppActivityBinding


@Suppress("DEPRECATION")
class AppActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: AppActivityBinding
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = AppActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        initAppPage()
    }

    private fun initAppPage() {
        val toolbar = binding.bottomNavigation.toolbar
        setSupportActionBar(toolbar)
        drawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        navView.setNavigationItemSelectedListener(this)
        val bottomNavView: BottomNavigationView = findViewById(R.id.bottom_nav_view)
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        bottomNavView.setupWithNavController(navController)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_drawer)
        ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.rateApp -> {
                try {
                    val url = "market://details?id=$packageName"
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                } catch (e: ActivityNotFoundException) {
                    val url = "https://play.google.com/store/apps/details?id=$packageName"
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            }
            R.id.moreApp -> {
                try {
                    val developerPageLink =
                        "https://play.google.com/store/apps/dev?id=7579037844327234040"
                    val developerPageIntent =
                        Intent(Intent.ACTION_VIEW, Uri.parse(developerPageLink))
                    ContextCompat.startActivity(this, developerPageIntent, null)
                } catch (_: Exception) {
                }
            }
            R.id.privacy_policy -> {
                showPrivacyPolicyDialog()
            }
            R.id.exit -> {
                finish()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun showPrivacyPolicyDialog() {
        val policyURL = "https://mobtechi.com/$packageName/privacy-policy"
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.privacy_policy)
        builder.setMessage(R.string.privacy_description)
        builder.setIcon(R.mipmap.ic_launcher)
        builder.setPositiveButton("yes") { dialogInterface, _ ->
            dialogInterface.dismiss()
            try {
                val policyIntent = Intent(Intent.ACTION_VIEW, Uri.parse(policyURL))
                ContextCompat.startActivity(this, policyIntent, null)
            } catch (e: Exception) {
                toast(this, "Something wrong! visit our privacy policy page url: $policyURL")
            }
        }
        builder.setNegativeButton("No") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == higherSdkStoragePermissionCode && resultCode == RESULT_OK) {
            val uri = data?.data
            if (uri != null) {
                val uriPath = uri.path
                if (uriPath != null && uriPath.endsWith(".Statuses")) {
                    val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    contentResolver.takePersistableUriPermission(uri, flag)
                    // after permission store into preference when android 30 or above
                    saveStoragePathPref(this, uri.toString())
                    // after permission given load the status fragment
                    val navController = findNavController(R.id.nav_host_fragment_activity_main)
                    navController.navigate(R.id.navigation_status)
                } else {
                    // dialog when user gave wrong path
                    val builder = AlertDialog.Builder(this)
                    builder.setCancelable(false).setTitle(getString(R.string.storage_permission))
                        .setMessage(getString(R.string.wrong_storage_permission_description))
                        .setPositiveButton(getString(R.string.exit)) { dialogInterface, _ ->
                            dialogInterface.dismiss()
                        }
                    builder.show()
                }
            }
        }
    }
}