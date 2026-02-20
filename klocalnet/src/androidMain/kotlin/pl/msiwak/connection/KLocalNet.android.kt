package pl.msiwak.connection

import android.content.Context
import org.koin.dsl.koinApplication
import pl.msiwak.connection.di.module

actual object KLocalNet {
    private val koinApp = koinApplication {
        modules(module)
    }

    fun init(context: Context) {
        AppContext.setUp(context)
    }

    actual fun getKLocalNetManager(): KLocalNetManager {
        return koinApp.koin.get()
    }
}