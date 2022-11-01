package com.kerencev.messenger.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.transition.Fade
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.kerencev.messenger.MessengerApp
import com.kerencev.messenger.R
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers

fun <T : Any> Single<T>.subscribeByDefault(): Single<T> {
    return this
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
}

fun Completable.subscribeByDefault(): Completable {
    return this
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
}

fun <T : Any> Observable<T>.subscribeByDefault(): Observable<T> {
    return this
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
}

fun Disposable.disposeBy(bag: CompositeDisposable) {
    bag.add(this)
}

fun ImageView.load(url: String) {
    Glide.with(context)
        .load(url)
        .placeholder(R.drawable.ic_user_place_holder)
        .into(this)
}

@SuppressLint("ResourceType")
fun ImageView.load(url: String, @StringRes placeHolder: Int) {
    Glide.with(context)
        .load(url)
        .placeholder(placeHolder)
        .into(this)
}

fun View.showComingSoonSnack() {
    Snackbar.make(this, R.string.coming_soon, Snackbar.LENGTH_SHORT).show()
}

fun Activity.showKeyBoard(editText: EditText) {
    editText.requestFocus()
    editText.isFocusableInTouchMode = true
    val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
}

fun Activity.hideKeyboard(editText: EditText) {
    val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(editText.windowToken, 0)
}

fun Any.log(message: String) {
    Log.d(this::class.java.simpleName, message)
}

fun View.makeVisible() {
    this.visibility = View.VISIBLE
}

fun View.makeInvisible() {
    this.visibility = View.INVISIBLE
}

fun View.makeGone() {
    this.visibility = View.GONE
}

fun View.reverseVisibility() {
    if (this.isVisible) {
        this.makeInvisible()
    } else {
        this.makeVisible()
    }
}

fun ViewGroup.animateWithDelayedFading() {
    TransitionManager.beginDelayedTransition(this, Fade())
}

fun ViewGroup.finishAnimation() {
    TransitionManager.endTransitions(this)
}

fun ViewGroup.finishAndAnimateWithDelayedFading() {
    this.finishAnimation()
    this.animateWithDelayedFading()
}

fun postDelayed(delay: Long, function: () -> Unit) {
    Handler(Looper.getMainLooper()).postDelayed({
        function()
    }, delay)
}

val Context.app: MessengerApp get() = applicationContext as MessengerApp
val Fragment.app: MessengerApp get() = requireContext().applicationContext as MessengerApp