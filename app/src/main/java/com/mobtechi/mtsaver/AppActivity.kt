package com.mobtechi.mtsaver

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.mobtechi.mtsaver.Functions.checkStoragePermission
import com.mobtechi.mtsaver.Functions.getAppPath
import com.mobtechi.mtsaver.databinding.AppActivityBinding
import java.io.File

@Suppress("DEPRECATION")
class AppActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: AppActivityBinding
    private lateinit var drawerLayout: DrawerLayout
    private val storagePermissionCode = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = AppActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        val toolbar = binding.appMainPage.toolbar
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
        val id = item.itemId
        if (id != R.id.appVersion) {
            when (item.itemId) {
                R.id.rateApp -> {

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
                            Toast.makeText(
                                this,
                                "Something wrong! visit our privacy policy page url: $policyURL",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    builder.setNegativeButton("No") { dialogInterface, _ ->
                        dialogInterface.dismiss()
                    }
                    // Create the AlertDialog
                    val alertDialog: AlertDialog = builder.create()
                    alertDialog.show()
                }
                R.id.exit -> {
                    finish()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        return true
    }

    @SuppressLint("DetachAndAttachSameFragment")
    override fun onResume() {
        super.onResume()
        // storagePermissionLayout
        val storagePermissionLayout: LinearLayout = findViewById(R.id.storage_permission)
        // checkStoragePermission
        if (checkStoragePermission(this)) {
            storagePermissionLayout.visibility = View.GONE
            // create our app directory
            val appFolder = File(getAppPath())
            if (!appFolder.exists()) {
                appFolder.mkdir()
            }
        } else {
            storagePermissionLayout.visibility = View.VISIBLE
        }

        // grant access permission button
        val grantAccessBtn = findViewById<Button>(R.id.grant_access)
        grantAccessBtn.setOnClickListener {
            askStoragePermission()
        }
    }

    private fun askStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //request for the all file access permission
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.fromParts("package", packageName, null)
            startActivityForResult(intent, storagePermissionCode)

        } else {
            //below android 11
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), storagePermissionCode
            )
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == storagePermissionCode) {
            // after permission given load the status fragment
            val navController = findNavController(R.id.nav_host_fragment_activity_main)
            navController.navigate(R.id.navigation_status)
        }
    }
}