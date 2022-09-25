package com.kerencev.messenger.ui.main.settings.changename

import android.util.Log
import com.github.terrakok.cicerone.Router
import com.kerencev.messenger.model.repository.FirebaseAuthRepository
import com.kerencev.messenger.ui.base.BasePresenter
import com.kerencev.messenger.utils.disposeBy
import com.kerencev.messenger.utils.subscribeByDefault

private const val TAG = "ChangeNamePresenter"

class ChangeNamePresenter(
    router: Router,
    private val repoAuth: FirebaseAuthRepository
) : BasePresenter<ChangeNameView>(router) {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        repoAuth.verifyUserIsLoggedIn()
            .flatMap {
                repoAuth.getUserById(it)
            }
            .subscribeByDefault()
            .subscribe(
                {
                    viewState.renderUserLogin(it.login)
                },
                {
                    Log.d(TAG, "${it.message}")
                }
            )
            .disposeBy(bag)
    }
}