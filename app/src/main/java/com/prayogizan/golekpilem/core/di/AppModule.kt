package com.prayogizan.golekpilem.core.di

import com.prayogizan.golekpilem.data.di.dataModule
import org.koin.dsl.module

/**
 * Root Koin dependency injection module aggregating all architectural layer modules.
 *
 * Utilizes Koin's [includes] DSL to compose child modules into a unified dependency graph.
 */
val appModule = module {
    includes(
        networkModule,
        dataModule
    )
}

