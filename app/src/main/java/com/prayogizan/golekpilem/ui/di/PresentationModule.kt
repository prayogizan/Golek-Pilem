package com.prayogizan.golekpilem.ui.di

import androidx.lifecycle.SavedStateHandle
import com.prayogizan.golekpilem.ui.detail.DetailViewModel
import com.prayogizan.golekpilem.ui.main.MainViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Presentation layer dependency injection module.
 *
 * Registers ViewModels and presentation-layer interactors. Feature ViewModels
 * ([MainViewModel] and [DetailViewModel]) are bound here.
 */
val presentationModule = module {
    viewModel {
        MainViewModel(
            getMoviesUseCase = get(),
            getTvSeriesUseCase = get()
        )
    }

    viewModel {
        DetailViewModel(
            savedStateHandle = it.getOrNull() ?: SavedStateHandle(),
            getMovieDetailUseCase = get(),
            getTvSeriesDetailUseCase = get()
        )
    }
}

