package me.proton.pass.presentation.home

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import me.proton.android.pass.commonuimodels.api.ItemUiModel
import me.proton.pass.common.api.None
import me.proton.pass.common.api.Option
import me.proton.pass.domain.ShareId
import me.proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import me.proton.android.pass.composecomponents.impl.uievents.IsProcessingSearchState
import me.proton.android.pass.composecomponents.impl.uievents.IsRefreshingState

@Immutable
data class HomeUiState(
    val homeListUiState: HomeListUiState,
    val searchUiState: SearchUiState
) {
    companion object {
        val Loading = HomeUiState(
            homeListUiState = HomeListUiState.Loading,
            searchUiState = SearchUiState.Initial
        )
    }
}

@Immutable
data class HomeListUiState(
    val isLoading: IsLoadingState,
    val isRefreshing: IsRefreshingState,
    val items: ImmutableList<ItemUiModel>,
    val selectedShare: Option<ShareId> = None,
    val sortingType: SortingType = SortingType.ByName
) {
    companion object {
        val Loading = HomeListUiState(
            isLoading = IsLoadingState.Loading,
            isRefreshing = IsRefreshingState.NotRefreshing,
            items = persistentListOf(),
            selectedShare = None
        )
    }
}

@Immutable
data class SearchUiState(
    val searchQuery: String,
    val isProcessingSearch: IsProcessingSearchState,
    val inSearchMode: Boolean
) {
    companion object {
        val Initial = SearchUiState(
            searchQuery = "",
            isProcessingSearch = IsProcessingSearchState.NotLoading,
            inSearchMode = false
        )
    }
}
