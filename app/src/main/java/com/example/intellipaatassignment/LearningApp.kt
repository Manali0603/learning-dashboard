package com.example.intellipaatassignment

import android.app.Application
import com.example.intellipaatassignment.di.AppContainer

class LearningApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
