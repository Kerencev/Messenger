package com.kerencev.messenger.services

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.kerencev.messenger.MessengerApp
import com.kerencev.messenger.model.repository.impl.FirebaseAuthRepositoryImpl
import io.reactivex.rxjava3.schedulers.Schedulers

private const val TAG = "TestService"

class StatusWorkManager(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        val repo = FirebaseAuthRepositoryImpl()
        repo.refreshUserStatus(MessengerApp.instance.user.uid)
            .subscribeOn(Schedulers.io())
            .subscribe()
        return Result.success()
    }
}