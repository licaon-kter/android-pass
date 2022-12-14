package me.proton.pass.autofill.ui.autofill.select

import androidx.compose.runtime.Immutable
import me.proton.pass.presentation.components.model.ItemUiModel
import me.proton.pass.presentation.uievents.IsLoadingState
import me.proton.pass.presentation.uievents.IsProcessingSearchState
import me.proton.pass.presentation.uievents.IsRefreshingState

@Immutable
data class SelectItemUiState(
    val listUiState: SelectItemListUiState,
    val searchUiState: SearchUiState
) {
    companion object {
        val Loading = SelectItemUiState(
            listUiState = SelectItemListUiState.Loading,
            searchUiState = SearchUiState.Initial
        )
    }
}

data class SelectItemListUiState(
    val isLoading: IsLoadingState,
    val isRefreshing: IsRefreshingState,
    val itemClickedEvent: ItemClickedEvent,
    val items: SelectItemListItems
) {
    companion object {
        val Loading = SelectItemListUiState(
            isLoading = IsLoadingState.Loading,
            isRefreshing = IsRefreshingState.NotRefreshing,
            itemClickedEvent = ItemClickedEvent.None,
            items = SelectItemListItems.Initial
        )
    }
}

data class SelectItemListItems(
    val suggestions: List<ItemUiModel>,
    val items: List<ItemUiModel>,
    val suggestionsForTitle: String
) {
    companion object {
        val Initial = SelectItemListItems(
            suggestions = emptyList(),
            items = emptyList(),
            suggestionsForTitle = ""
        )
    }
}

@Immutable
data class SearchUiState(
    val searchQuery: String,
    val inSearchMode: Boolean,
    val isProcessingSearch: IsProcessingSearchState
) {
    companion object {
        val Initial = SearchUiState(
            searchQuery = "",
            inSearchMode = false,
            isProcessingSearch = IsProcessingSearchState.NotLoading
        )
    }
}
