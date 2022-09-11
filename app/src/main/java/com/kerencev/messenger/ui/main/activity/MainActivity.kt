package com.kerencev.messenger.ui.main.activity

import android.content.Intent
import android.os.Bundle
import com.github.terrakok.cicerone.androidx.AppNavigator
import com.kerencev.messenger.MessengerApp
import com.kerencev.messenger.R
import com.kerencev.messenger.model.FirebaseRepositoryImpl
import com.kerencev.messenger.navigation.OnBackPressedListener
import com.kerencev.messenger.ui.login.loginactivity.LoginActivity
import moxy.MvpAppCompatActivity
import moxy.ktx.moxyPresenter

class MainActivity : MvpAppCompatActivity(), MainView {

    private val navigator = AppNavigator(this, R.id.activityMainContainer)
    private val presenter by moxyPresenter {
        MainPresenter(
            MessengerApp.instance.router,
            FirebaseRepositoryImpl()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Messenger)
        setContentView(R.layout.activity_main)
        presenter.verifyUserIsLoggedIn()
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
}