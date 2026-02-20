package pl.msiwak.connection.di

import org.koin.core.module.Module
import org.koin.dsl.module
import pl.msiwak.connection.ElectionService
import pl.msiwak.connection.KtorClient
import pl.msiwak.connection.KLocalNetManager
import pl.msiwak.connection.KLocalNetManagerImpl
import pl.msiwak.connection.engine.EngineProvider

internal expect val platformModule: Module

internal val module = module {
    single { ElectionService(get()) }
    single { KtorClient(get()) }
    single { EngineProvider() }
    single<KLocalNetManager> { KLocalNetManagerImpl(get(), get(), get(), get()) }
    includes(platformModule)
}