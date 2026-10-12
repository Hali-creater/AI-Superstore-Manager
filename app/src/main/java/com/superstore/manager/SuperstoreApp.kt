package com.superstore.manager

import android.app.Application
import com.superstore.manager.data.local.AppDatabase

class SuperstoreApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
}
