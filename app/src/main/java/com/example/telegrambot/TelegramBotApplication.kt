package com.example.telegrambot

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.lang.ref.WeakReference

class TelegramBotApplication : Application() {

    companion object {
        private var activeActivityRef: WeakReference<Activity>? = null

        fun getActiveActivity(): Activity? {
            return activeActivityRef?.get()
        }
    }

    override fun onCreate() {
        super.onCreate()
        
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {
                activeActivityRef = WeakReference(activity)
            }
            override fun onActivityPaused(activity: Activity) {
                if (activeActivityRef?.get() == activity) {
                    activeActivityRef = null
                }
            }
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }
}
