package com.kerencev.messenger.ui.base

import io.reactivex.rxjava3.disposables.CompositeDisposable
import moxy.MvpPresenter
import moxy.MvpView

abstract class BasePresenter<T : MvpView> : MvpPresenter<T>() {

    protected val bag = CompositeDisposable()

    override fun onDestroy() {
        bag.dispose()
        super.onDestroy()
    }
}