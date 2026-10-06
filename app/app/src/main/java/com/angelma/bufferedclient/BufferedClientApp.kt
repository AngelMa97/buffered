package com.angelma.bufferedclient

import android.app.Application
import com.angelma.bufferedclient.di.appModule
import com.angelma.core.data.di.coreDataModule
import com.angelma.feature.catalog.data.di.catalogDataModule
import com.angelma.feature.catalog.presentation.di.catalogModule
import com.angelma.feature.player.presentation.di.playerModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class BufferedClientApp() : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@BufferedClientApp)
            modules(
                appModule,
                catalogModule,
                playerModule,
                coreDataModule,
                catalogDataModule
            )
        }
    }
}
