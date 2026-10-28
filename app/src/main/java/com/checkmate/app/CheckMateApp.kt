package com.checkmate.app

import android.app.Application
import com.checkmate.app.data.CheckMateRepository
import com.checkmate.app.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Owns the single database instance and repository for the whole app. */
class CheckMateApp : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val repository: CheckMateRepository by lazy { CheckMateRepository(AppDatabase.build(this)) }

    override fun onCreate() {
        super.onCreate()
        appScope.launch { repository.seedIfEmpty() }
    }
}
