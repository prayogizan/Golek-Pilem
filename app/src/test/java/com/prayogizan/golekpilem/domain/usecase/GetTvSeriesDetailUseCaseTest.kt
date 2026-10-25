package com.prayogizan.golekpilem.domain.usecase

import androidx.paging.PagingData
import com.prayogizan.golekpilem.core.util.Resource
import com.prayogizan.golekpilem.domain.model.TvSeries
import com.prayogizan.golekpilem.domain.model.TvSeriesDetail
import com.prayogizan.golekpilem.domain.repository.TvRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Test

class GetTvSeriesDetailUseCaseTest {

    private class FakeTvRepository : TvRepository {
        var requestedSeriesId: Int? = null
        var requestedForceRefresh: Boolean? = null
        val expectedResourceFlow: Flow<Resource<TvSeriesDetail>> = flowOf(
            Resource.Success(
                TvSeriesDetail(
                    id = 200,
                    name = "Test Series",
                    tagline = "Tagline",
                    overview = "Overview",
                    posterUrl = null,
                    backdropUrl = null,
                    firstAirDate = "2026-01-01",
                    lastAirDate = "2026-12-31",
                    numberOfSeasons = 1,
                    numberOfEpisodes = 10,
                    voteAverage = 7.8,
                    voteCount = 50,
                    genres = emptyList(),
                    networks = emptyList(),
                    status = "Ended"
                )
            )
        )

        override fun getTvSeries(): Flow<PagingData<TvSeries>> = emptyFlow()

        override fun getTvSeriesDetail(seriesId: Int, forceRefresh: Boolean): Flow<Resource<TvSeriesDetail>> {
            requestedSeriesId = seriesId
            requestedForceRefresh = forceRefresh
            return expectedResourceFlow
        }
    }

    @Test
    fun `invoke delegates execution directly to tvRepository getTvSeriesDetail with correct id and default forceRefresh`() {
        val fakeRepository = FakeTvRepository()
        val useCase = GetTvSeriesDetailUseCase(fakeRepository)

        val resultFlow = useCase(seriesId = 200)

        assertEquals(fakeRepository.expectedResourceFlow, resultFlow)
        assertEquals(200, fakeRepository.requestedSeriesId)
        assertEquals(false, fakeRepository.requestedForceRefresh)
    }

    @Test
    fun `invoke delegates execution directly to tvRepository getTvSeriesDetail with explicit forceRefresh true`() {
        val fakeRepository = FakeTvRepository()
        val useCase = GetTvSeriesDetailUseCase(fakeRepository)

        val resultFlow = useCase(seriesId = 200, forceRefresh = true)

        assertEquals(fakeRepository.expectedResourceFlow, resultFlow)
        assertEquals(200, fakeRepository.requestedSeriesId)
        assertEquals(true, fakeRepository.requestedForceRefresh)
    }
}
