package com.prayogizan.golekpilem.ui.di

import com.prayogizan.golekpilem.ui.main.MainViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Presentation layer dependency injection module.
 *
 * Registers ViewModels and presentation-layer interactors. Feature ViewModels
 * ([MainViewModel] and DetailViewModel) are bound here.
 */
val presentationModule = module {
    viewModel {
        MainViewModel(
            getMoviesUseCase = get(),
            getTvSeriesUseCase = get()
        )
    }
}
