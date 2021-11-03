package com.google.ctlbox

import androidx.lifecycle.LifecycleObserver
import kotlinx.coroutines.flow.Flow

interface AppLifecycleObserver : LifecycleObserver {
    fun observeForeground(): Flow<Boolean>
}
