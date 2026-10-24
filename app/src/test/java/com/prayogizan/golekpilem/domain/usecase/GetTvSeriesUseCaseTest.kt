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

class GetTvSeriesUseCaseTest {

    private class FakeTvRepository(
        val pagingDataFlow: Flow<PagingData<TvSeries>> = flowOf(PagingData.empty())
    ) : TvRepository {
        var getTvSeriesCalled = false

        override fun getTvSeries(): Flow<PagingData<TvSeries>> {
            getTvSeriesCalled = true
            return pagingDataFlow
        }

        override fun getTvSeriesDetail(seriesId: Int): Flow<Resource<TvSeriesDetail>> {
            return emptyFlow()
        }
    }

    @Test
    fun `invoke delegates execution directly to tvRepository getTvSeries`() {
        val fakeRepository = FakeTvRepository()
        val useCase = GetTvSeriesUseCase(fakeRepository)

        val resultFlow = useCase()

        assertEquals(fakeRepository.pagingDataFlow, resultFlow)
        assertEquals(true, fakeRepository.getTvSeriesCalled)
    }
}
