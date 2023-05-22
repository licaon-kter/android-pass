package proton.android.pass.autofill.ui.autofill.select

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import proton.android.pass.commonui.api.GroupedItemList
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.commonuimodels.api.ShareUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.composecomponents.impl.uievents.IsProcessingSearchState
import proton.android.pass.composecomponents.impl.uievents.IsRefreshingState
import proton.android.pass.featuresearchoptions.api.SearchSortingType
import proton.pass.domain.ShareId

sealed interface SearchInMode {
    object PrimaryVault : SearchInMode
    object AllVaults : SearchInMode
}

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
    val itemClickedEvent: AutofillItemClickedEvent,
    val items: SelectItemListItems,
    val shares: PersistentMap<ShareId, ShareUiModel>,
    val sortingType: SearchSortingType,
    val shouldScrollToTop: Boolean,
    val canLoadExternalImages: Boolean,
    val displayOnlyPrimaryVaultMessage: Boolean,
) {
    companion object {
        val Loading = SelectItemListUiState(
            isLoading = IsLoadingState.Loading,
            isRefreshing = IsRefreshingState.NotRefreshing,
            itemClickedEvent = AutofillItemClickedEvent.None,
            items = SelectItemListItems.Initial,
            shares = persistentMapOf(),
            sortingType = SearchSortingType.MostRecent,
            shouldScrollToTop = false,
            canLoadExternalImages = false,
            displayOnlyPrimaryVaultMessage = false,
        )
    }
}

data class SelectItemListItems(
    val suggestions: ImmutableList<ItemUiModel>,
    val items: ImmutableList<GroupedItemList>,
    val suggestionsForTitle: String
) {
    companion object {
        val Initial = SelectItemListItems(
            suggestions = persistentListOf(),
            items = persistentListOf(),
            suggestionsForTitle = ""
        )
    }
}

@Immutable
data class SearchUiState(
    val searchQuery: String,
    val inSearchMode: Boolean,
    val isProcessingSearch: IsProcessingSearchState,
    val searchInMode: SearchInMode

) {
    companion object {
        val Initial = SearchUiState(
            searchQuery = "",
            inSearchMode = false,
            isProcessingSearch = IsProcessingSearchState.NotLoading,
            searchInMode = SearchInMode.AllVaults
        )
    }
}
