package pl.msiwak.connection.di

import org.koin.core.module.Module
import org.koin.dsl.module
import pl.msiwak.connection.ConnectionManager
import pl.msiwak.connection.ConnectionManagerImpl
import pl.msiwak.connection.KtorServer
import pl.msiwak.connection.KtorServerImpl

internal actual val platformModule = module {
    single<KtorServer> { KtorServerImpl() }
    single<ConnectionManager> { ConnectionManagerImpl() }
}
