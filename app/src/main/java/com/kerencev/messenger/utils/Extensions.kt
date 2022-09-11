package com.kerencev.messenger.utils

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.kerencev.messenger.R
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers

fun <T> Single<T>.subscribeByDefault(): Single<T> {
    return this
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
}

fun Completable.subscribeByDefault(): Completable {
    return this
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
}

fun <T> Observable<T>.subscribeByDefault(): Observable<T> {
    return this
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
}

fun Disposable.disposeBy(bag: CompositeDisposable) {
    bag.add(this)
}

fun ImageView.loadUserImageWithGlide(url: String) {
    Glide.with(context)
        .load(url)
        .placeholder(R.drawable.ic_user_place_holder)
        .into(this)
}