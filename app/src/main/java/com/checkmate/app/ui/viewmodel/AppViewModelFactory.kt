package com.checkmate.app.ui.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.checkmate.app.CheckMateApp

/** Creates every ViewModel with the app-wide repository. */
object AppViewModelFactory {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer { DashboardViewModel(app().repository) }
        initializer { SplitViewModel(app().repository) }
    }

    private fun CreationExtras.app(): CheckMateApp = this[APPLICATION_KEY] as CheckMateApp
}
