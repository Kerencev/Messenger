package com.kerencev.messenger.ui.login.loginactivity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.github.terrakok.cicerone.androidx.AppNavigator
import com.kerencev.messenger.MessengerApp
import com.kerencev.messenger.R
import com.kerencev.messenger.databinding.ActivityLoginContainerBinding
import com.kerencev.messenger.navigation.FinishActivity
import com.kerencev.messenger.navigation.OnBackPressedListener
import com.kerencev.messenger.ui.main.activity.MainActivity
import moxy.MvpAppCompatActivity
import moxy.ktx.moxyPresenter

class LoginActivity : MvpAppCompatActivity(), LoginContainerView, FinishActivity, StatusBarHolder{

    private lateinit var binding: ActivityLoginContainerBinding
    private val navigator = AppNavigator(this, R.id.activityLoginContainer)
    private val presenter by moxyPresenter {
        LoginContainerPresenter(
            MessengerApp.instance.router
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginContainerBinding.inflate(layoutInflater)
        setContentView(binding.root)
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

    override fun startMainActivity() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
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