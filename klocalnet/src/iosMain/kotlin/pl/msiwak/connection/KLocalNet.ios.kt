package pl.msiwak.connection

import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication
import pl.msiwak.connection.di.module

actual object KLocalNet {
    lateinit var koinApp: KoinApplication

    fun init() {
        koinApp = koinApplication {
            modules(module)
        }
    }

    actual fun getKLocalNetManager(): KLocalNetManager {
        return koinApp.koin.get()
    }
}