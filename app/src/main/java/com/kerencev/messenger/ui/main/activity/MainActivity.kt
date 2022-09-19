package com.kerencev.messenger.ui.main.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.github.terrakok.cicerone.androidx.AppNavigator
import com.kerencev.messenger.MessengerApp
import com.kerencev.messenger.R
import com.kerencev.messenger.databinding.ActivityMainBinding
import com.kerencev.messenger.model.entities.User
import com.kerencev.messenger.model.repository.impl.FirebaseAuthRepositoryImpl
import com.kerencev.messenger.navigation.OnBackPressedListener
import com.kerencev.messenger.services.TestService
import com.kerencev.messenger.ui.login.loginactivity.LoginActivity
import moxy.MvpAppCompatActivity
import moxy.ktx.moxyPresenter

private const val TAG = "MainActivity"

class MainActivity : MvpAppCompatActivity(), MainView{

    private lateinit var binding: ActivityMainBinding
    private val navigator = AppNavigator(this, R.id.activityMainContainer)
    private val presenter by moxyPresenter {
        MainPresenter(
            MessengerApp.instance.router,
            FirebaseAuthRepositoryImpl()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Messenger)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        presenter.verifyUserIsLoggedIn()
        binding.navigation.itemIconTintList = null
        hideStatusBar()
        setNavigationDrawerClicks()
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        MessengerApp.instance.navigationHolder.setNavigator(navigator)
    }

    override fun onPause() {
        MessengerApp.instance.navigationHolder.removeNavigator()
        super.onPause()
    }

    override fun onBackPressed() {
        supportFragmentManager.fragments.forEach { currentFragment ->
            if (currentFragment is OnBackPressedListener && currentFragment.onBackPressed()) {
                return
            }
        }
        presenter.onBackPressed()
    }

    override fun startLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun setUserData(user: User) {
        binding.navigation.getHeaderView(0).findViewById<TextView>(R.id.tvNavHeaderLogin).text =
            user.login
        binding.navigation.getHeaderView(0).findViewById<TextView>(R.id.tvNavHeaderEmail).text =
            user.email
        val avatarView =
            binding.navigation.getHeaderView(0).findViewById<ImageView>(R.id.ivNavHeaderAvatar)
        Glide.with(this)
            .load(user.avatarUrl)
            .placeholder(R.drawable.user)
            .into(avatarView)
    }

    override fun setToolbar(toolbar: Toolbar) {
        val actionBarDrawerToggle = ActionBarDrawerToggle(
            this,
            binding.drawer,
            toolbar,
            R.string.drawer_open,
            R.string.drawer_close
        )
        binding.drawer.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
    }

    private fun setNavigationDrawerClicks() = with(binding) {
        navigation.setNavigationItemSelectedListener { menuItem ->
            when(menuItem.itemId) {
                R.id.settings -> presenter.navigateToSettingsFragment()
            }
            drawer.close()
            true
        }
    }

    override fun hideStatusBar() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = Color.TRANSPARENT
    }

    override fun showStatusBar() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.statusBarColor = resources.getColor(R.color.primary_background)
    }
}