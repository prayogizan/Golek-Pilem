package com.prayogizan.golekpilem.ui.di

import org.koin.dsl.module

/**
 * Presentation layer dependency injection module.
 *
 * Registers ViewModels and presentation-layer interactors. Feature ViewModels
 * ([MainViewModel] and [DetailViewModel]) will be bound here during their respective
 * milestone implementations.
 */
val presentationModule = module {
    // Feature ViewModels will be injected as tickets #6 and #7 are introduced
}
