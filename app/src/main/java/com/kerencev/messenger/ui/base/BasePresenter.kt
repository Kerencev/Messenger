package com.kerencev.messenger.ui.base

import com.github.terrakok.cicerone.Router
import io.reactivex.rxjava3.disposables.CompositeDisposable
import moxy.MvpPresenter
import moxy.MvpView

abstract class BasePresenter<T : MvpView>(
    private val router: Router
) : MvpPresenter<T>() {

    protected val bag = CompositeDisposable()

    fun onBackPressed(): Boolean {
        router.exit()
        return true
    }

    override fun onDestroy() {
        bag.dispose()
        super.onDestroy()
    }
}