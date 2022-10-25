package com.kerencev.messenger.ui.main.settings.changename

import com.github.terrakok.cicerone.Router
import com.kerencev.messenger.model.entities.User
import com.kerencev.messenger.model.repository.AuthRepository
import com.kerencev.messenger.model.repository.UsersRepository
import com.kerencev.messenger.ui.base.BasePresenter
import com.kerencev.messenger.utils.disposeBy
import com.kerencev.messenger.utils.subscribeByDefault

class ChangeNamePresenter(
    private val router: Router,
    private val repoAuth: AuthRepository,
    private val repoUsers: UsersRepository
) : BasePresenter<ChangeNameView>(router) {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        repoAuth.verifyUserIsLoggedIn()
            .flatMap {
                repoAuth.getUserById(it)
            }
            .subscribeByDefault()
            .subscribe { currentUser ->
                viewState.renderUserLogin(currentUser.login)
            }.disposeBy(bag)
    }

    fun checkValidityLogin(text: String) {
        if (text.length < MIN_LOGIN_LETTERS) return
        repoUsers.getAllUsers()
            .subscribeByDefault()
            .subscribe { listOfAllUsers ->
                viewState.showValidityLoginInfo(
                    isValid = checkExistenceLoginAmongAllUsers(
                        newLogin = text,
                        listOfUsers = listOfAllUsers
                    )
                )
            }.disposeBy(bag)
    }

    fun handleActionDoneEvent(text: String) {
        if (text.length < MIN_LOGIN_LETTERS) {
            viewState.highlightError()
            return
        }
        repoUsers.getAllUsers()
            .map { listOfAllUsers ->
                checkExistenceLoginAmongAllUsers(
                    newLogin = text,
                    listOfUsers = listOfAllUsers
                )
            }
            .subscribeByDefault()
            .subscribe { isLoginAvailable ->
                when (isLoginAvailable) {
                    true -> {
                        updateUserLogin(text)
                    }
                    false -> {
                        viewState.highlightError()
                    }
                }
            }.disposeBy(bag)
    }

    /**
     * return - true, if such a login doesn't exist
     */
    private fun checkExistenceLoginAmongAllUsers(
        newLogin: String,
        listOfUsers: List<User>
    ): Boolean {
        listOfUsers.forEach { user ->
            if (user.login == newLogin) {
                return false
            }
        }
        return true
    }

    /**
     * Update user login For all nodes in the Firebase
     */
    private fun updateUserLogin(newLogin: String) {
        repoUsers.updateUserLoginForUsersNode(newLogin)
            .concatWith(repoUsers.updateUserLoginForAllChatPartners(newLogin))
            .subscribeByDefault()
            .subscribe {
                viewState.setResultForSettingsFragment(newLogin)
                router.exit()
            }.disposeBy(bag)
    }
}