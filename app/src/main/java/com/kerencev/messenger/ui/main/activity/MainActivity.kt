package com.kerencev.messenger.ui.main.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.bumptech.glide.Glide
import com.github.terrakok.cicerone.androidx.AppNavigator
import com.kerencev.messenger.MessengerApp
import com.kerencev.messenger.R
import com.kerencev.messenger.databinding.ActivityMainBinding
import com.kerencev.messenger.model.entities.User
import com.kerencev.messenger.model.repository.impl.FirebaseAuthRepositoryImpl
import com.kerencev.messenger.navigation.OnBackPressedListener
import com.kerencev.messenger.services.FirebaseService
import com.kerencev.messenger.services.FirebaseService.Companion.PUSH_INTENT_PARTNER_AVATAR
import com.kerencev.messenger.services.FirebaseService.Companion.PUSH_INTENT_PARTNER_EMAIL
import com.kerencev.messenger.services.FirebaseService.Companion.PUSH_INTENT_PARTNER_ID
import com.kerencev.messenger.services.FirebaseService.Companion.PUSH_INTENT_PARTNER_LOGIN
import com.kerencev.messenger.services.FirebaseService.Companion.PUSH_INTENT_PARTNER_NOTIFICATION_ID
import com.kerencev.messenger.services.StatusWorkManager
import com.kerencev.messenger.ui.login.loginactivity.LoginActivity
import de.hdodenhof.circleimageview.CircleImageView
import moxy.MvpAppCompatActivity
import moxy.ktx.moxyPresenter

class MainActivity : MvpAppCompatActivity(), MainView {

    private lateinit var binding: ActivityMainBinding

    private val navigator = AppNavigator(this, R.id.activityMainContainer)

    private val presenter by moxyPresenter {
        MainPresenter(
            MessengerApp.instance.router,
            FirebaseAuthRepositoryImpl()
        )
    }

    private var updateStatusRequest: WorkRequest? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Messenger)
        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.navigation.itemIconTintList = null
        setContentView(binding.root)
        presenter.navigateToChatList(getChatPartnerFromPush())
        presenter.verifyUserIsLoggedIn()
        FirebaseService.sharedPref = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
        setNavigationDrawerClicks()
    }

    override fun onStart() {
        updateStatusRequest = OneTimeWorkRequest.Builder(StatusWorkManager::class.java).build()
        updateStatusRequest?.let { WorkManager.getInstance(this).enqueue(it) }
        super.onStart()
    }

    override fun onPause() {
        MessengerApp.instance.navigationHolder.removeNavigator()
        super.onPause()
    }

    override fun onStop() {
        updateStatusRequest?.let { WorkManager.getInstance(this).cancelWorkById(it.id) }
        super.onStop()
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        MessengerApp.instance.navigationHolder.setNavigator(navigator)
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
        val letterView =
            binding.navigation.getHeaderView(0).findViewById<TextView>(R.id.tvNavHeaderLetter)
        when (user.avatarUrl) {
            null -> {
                letterView.visibility = View.VISIBLE
                letterView.text = user.login.first().toString()
            }
            else -> {
                avatarView.visibility = View.VISIBLE
                Glide.with(this)
                    .load(user.avatarUrl)
                    .placeholder(R.drawable.user)
                    .into(avatarView)
            }
        }
    }

    override fun updateUserLogin(newLogin: String) {
        binding.navigation.getHeaderView(0).findViewById<TextView>(R.id.tvNavHeaderLogin).text =
            newLogin
    }

    override fun updateUserAvatar(newAvatarUrl: String) {
        val ivAvatar = binding.navigation.getHeaderView(0)
            .findViewById<CircleImageView>(R.id.ivNavHeaderAvatar)
        Glide.with(this).load(newAvatarUrl).into(ivAvatar)
    }

    override fun setDrawerLockMode(isOpenable: Boolean) {
        binding.drawer.setDrawerLockMode(
            when (isOpenable) {
                true -> DrawerLayout.LOCK_MODE_UNLOCKED
                false -> DrawerLayout.LOCK_MODE_LOCKED_CLOSED
            }
        )
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

    override fun hideStatusBar() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = Color.TRANSPARENT
    }

    override fun showStatusBar() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.statusBarColor = resources.getColor(R.color.primary_background)
    }

    /**
     * Return chatPartner if the user click on the push Notification
     */
    private fun getChatPartnerFromPush(): User? {
        val arguments = intent.extras
        val chatPartnerId = arguments?.getString(PUSH_INTENT_PARTNER_ID)
        val chatPartnerLogin = arguments?.getString(PUSH_INTENT_PARTNER_LOGIN)
        val chatPartnerEmail = arguments?.getString(PUSH_INTENT_PARTNER_EMAIL)
        val chatPartnerAvatarUrl = arguments?.getString(PUSH_INTENT_PARTNER_AVATAR)
        val chatPartnerNotificationId = arguments?.getInt(PUSH_INTENT_PARTNER_NOTIFICATION_ID)
        return if (chatPartnerId.isNullOrEmpty() || chatPartnerLogin.isNullOrEmpty()
            || chatPartnerEmail.isNullOrEmpty() || chatPartnerNotificationId == null
        ) {
            null
        } else {
            User(
                uid = chatPartnerId,
                notificationId = chatPartnerNotificationId,
                login = chatPartnerLogin,
                email = chatPartnerEmail,
                avatarUrl = chatPartnerAvatarUrl,
                wasOnline = -1,
                status = ""
            )
        }
    }

    private fun setNavigationDrawerClicks() = with(binding) {
        navigation.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.settings -> presenter.navigateToSettingsFragment()
            }
            drawer.close()
            true
        }
    }

    companion object {
        const val BUNDLE_KEY_JOB_STATUS = "BUNDLE_KEY_JOB_STATUS"
    }
}