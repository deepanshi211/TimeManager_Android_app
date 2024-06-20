package com.example.timemanagementapp
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.example.timemanagementapp.R
import com.example.timemanagementapp.databinding.ActivityMainBinding
import com.example.timemanagementapp.activities.LoginPage
import com.example.timemanagementapp.fragments.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestoreSettings

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firestore: FirebaseFirestore

    private val channelId = "my_channel_id"
    private val notificationId = 1

    companion object {
        var username: String = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        authUser()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createNotificationChannel()

        val intent = intent.getStringExtra("Fragment")
        if (intent != null) {
            openFragment(intent)
        }

        firestore = FirebaseFirestore.getInstance()
        val settings = firestoreSettings { isPersistenceEnabled = true }
        firestore.firestoreSettings = settings

        materialToolBarFunctions()
        bottomNavBarFunctions()
        sideNavBarFunctions()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "My Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "My Channel Description"
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("MissingPermission")
    private fun showNotification() {
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.alarm_icon)
            .setContentTitle("TIME MANAGEMENT APP")
            .setContentText("Hey!!!You are logging out.Hope I helped you in managing yout time.See you tomorrow!!!!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(notificationId, builder.build())
    }

    private fun authUser() {
        val sharedPreferences = getSharedPreferences("UserNameLogin", MODE_PRIVATE)
        username = sharedPreferences.getString("username", "").toString()
        val password = sharedPreferences.getString("password", "")
        if (username.isBlank() && password.isNullOrBlank()) {
            val intent = Intent(this, LoginPage::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun materialToolBarFunctions() {
        val drawerLayout = binding.drawerLayout
        val toolbar = binding.materialToolbar
        toolbar.setNavigationOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }
    }

    private fun bottomNavBarFunctions() {
        val bottomNavigationView = binding.bottomNavBar
        lateinit var fragment: Fragment

        bottomNavigationView.setOnItemSelectedListener { item ->
            item.isChecked = true
            when (item.itemId) {
                R.id.bottomNavBarMenuStopwatch -> { fragment = TimeTrackerFragment() }
                R.id.bottomNavBarMenuTask -> { fragment = TaskFragment() }
                R.id.bottomNavBarMenuHabit -> { fragment = HabitFragment() }
                R.id.bottomNavBarMenuReport -> { fragment = ReportFragment() }
            }

            fragmentTransaction(fragment)
            false
        }
    }

    private fun sideNavBarFunctions() {
        val sideNavigation = binding.sideNavigationBar
        val tvUsername = sideNavigation.getHeaderView(0).findViewById<TextView>(R.id.sideNavUsername)
        if (username.isNotBlank() && username.isNotEmpty()) {
            tvUsername.text = username
        }
        sideNavigation.setNavigationItemSelectedListener { item ->
            lateinit var fragment: Fragment
            when (item.itemId) {
                R.id.sideNavBarMenuTasks -> { fragment = TaskFragment() }
                R.id.sideNavBarMenuHabitTracker -> { fragment = HabitFragment() }
                R.id.sideNavBarMenuExercise -> { fragment = ExerciseFragment() }
                R.id.sideNavBarMenuSettings -> { fragment = SettingsFragment() }
                R.id.sideNavBarMenuLogout -> {
                    startActivity(Intent(applicationContext, LoginPage::class.java))
                    getSharedPreferences("UserNameLogin", MODE_PRIVATE)
                        .edit()
                        .clear()
                        .apply()
                    finish()
                    showNotification()  // Add this line to show a notification when the user logs out
                    return@setNavigationItemSelectedListener true
                }
            }
            fragmentTransaction(fragment)

            binding.drawerLayout.closeDrawer(GravityCompat.START)
            false
        }
    }

    private fun fragmentTransaction(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainerView, fragment).commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.actionbar_options, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                val intent = Intent(this, LoginPage::class.java)
                startActivity(intent)
                val sharedPreferences = getSharedPreferences("UserNameLogin", MODE_PRIVATE)
                sharedPreferences.edit().clear().apply()
                finish()
                showNotification()  // Add this line to show a notification when the "logout" option is selected
            }
            R.id.search -> {
                val fragment = SettingsFragment()
                fragmentTransaction(fragment)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        recreate()
    }

    private fun openFragment(fragmentName: String) {
        lateinit var fragment: Fragment
        when (fragmentName) {
            "taskFragmentIntent" -> { fragment = TaskFragment() }
            "stopwatchFragmentIntent" -> { fragment = TimeTrackerFragment() }
        }
        fragmentTransaction(fragment)
    }
}