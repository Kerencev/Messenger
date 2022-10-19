package com.kerencev.messenger.ui.main.settings.changename

import android.util.Log
import com.github.terrakok.cicerone.Router
import com.kerencev.messenger.model.repository.FirebaseAllUsersRepository
import com.kerencev.messenger.model.repository.FirebaseAuthRepository
import com.kerencev.messenger.ui.base.BasePresenter
import com.kerencev.messenger.utils.disposeBy
import com.kerencev.messenger.utils.log
import com.kerencev.messenger.utils.subscribeByDefault

private const val TAG = "ChangeNamePresenter"

class ChangeNamePresenter(
    private val router: Router,
    private val repoAuth: FirebaseAuthRepository,
    private val repoUsers: FirebaseAllUsersRepository
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

    fun checkValidityLogin(text: String) {
        if (text.length < MIN_LOGIN_LETTERS) return
        repoUsers.getAllUsers()
            .flatMap {
                repoUsers.checkValidityLogin(text, it)
            }
            .subscribeByDefault()
            .subscribe(
                { isValid ->
                    viewState.showValidityLoginInfo(isValid = isValid)
                },
                {
                    log(it.message.toString())
                }
            ).disposeBy(bag)
    }

    fun handleActionDoneEvent(text: String) {
        if (text.length < MIN_LOGIN_LETTERS) {
            viewState.highlightError()
            return
        }
        repoUsers.getAllUsers()
            .flatMap {
                repoUsers.checkValidityLogin(text, it)
            }
            .subscribeByDefault()
            .subscribe(
                { isValid ->
                    when (isValid) {
                        true -> {
                            repoUsers.updateUserLogin(text)
                                .subscribeByDefault()
                                .subscribe(
                                    {
                                        viewState.setResultForSettingsFragment(text)
                                        router.exit()
                                    },
                                    {
                                        log(it.message.toString())
                                    }
                                )
                        }
                        false -> {
                            viewState.highlightError()
                        }
                    }
                },
                {
                    log(it.message.toString())
                }
            ).disposeBy(bag)
    }
}