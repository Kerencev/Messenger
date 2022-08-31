package com.kerencev.messenger.navigation.login

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.github.terrakok.cicerone.androidx.FragmentScreen
import com.kerencev.messenger.ui.login.LoginFragment
import com.kerencev.messenger.ui.login.signin.SignInFragment
import com.kerencev.messenger.ui.login.signup.SignUpFragment

object LoginScreen : FragmentScreen {
    override fun createFragment(factory: FragmentFactory): Fragment {
        return LoginFragment.getInstance()
    }
}

object SignInScreen : FragmentScreen {
    override fun createFragment(factory: FragmentFactory): Fragment {
        return SignInFragment.getInstance()
    }
}

object SignUpScreen : FragmentScreen {
    override fun createFragment(factory: FragmentFactory): Fragment {
        return SignUpFragment.getInstance()
    }
}