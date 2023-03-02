package proton.android.pass.featurehome.impl

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import proton.android.pass.common.api.Option
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.buttons.PassFloatingActionButton
import proton.android.pass.composecomponents.impl.dialogs.ConfirmItemDeletionDialog
import proton.android.pass.composecomponents.impl.item.EmptySearchResults
import proton.android.pass.composecomponents.impl.item.ItemsList
import proton.android.pass.featurehome.impl.onboardingtips.OnBoardingTips
import proton.pass.domain.ShareId

@Suppress("LongParameterList")
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
internal fun HomeContent(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    homeFilter: HomeItemTypeSelection,
    shouldScrollToTop: Boolean,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    homeScreenNavigation: HomeScreenNavigation,
    onSearchQueryChange: (String) -> Unit,
    onEnterSearch: () -> Unit,
    onStopSearching: () -> Unit,
    sendItemToTrash: (ItemUiModel) -> Unit,
    onDrawerIconClick: () -> Unit,
    onMoreOptionsClick: () -> Unit,
    onAddItemClick: (Option<ShareId>) -> Unit,
    onItemMenuClick: (ItemUiModel) -> Unit,
    onRefresh: () -> Unit,
    onScrollToTop: () -> Unit
) {
    // Only enable the backhandler if we are in search mode
    BackHandler(enabled = uiState.searchUiState.inSearchMode) {
        onStopSearching()
    }

    Scaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        floatingActionButton = {
            PassFloatingActionButton(
                onClick = { onAddItemClick(uiState.homeListUiState.selectedShare) }
            )
        },
        topBar = {
            HomeTopBar(
                searchQuery = uiState.searchUiState.searchQuery,
                inSearchMode = uiState.searchUiState.inSearchMode,
                homeFilter = homeFilter,
                onSearchQueryChange = onSearchQueryChange,
                onEnterSearch = onEnterSearch,
                onStopSearching = onStopSearching,
                onDrawerIconClick = onDrawerIconClick,
                onMoreOptionsClick = onMoreOptionsClick
            )
        }
    ) { contentPadding ->
        var itemToDelete by rememberSaveable { mutableStateOf<ItemUiModel?>(null) }
        val keyboardController = LocalSoftwareKeyboardController.current
        ItemsList(
            modifier = Modifier.padding(contentPadding),
            items = uiState.homeListUiState.items,
            shouldScrollToTop = shouldScrollToTop,
            highlight = uiState.searchUiState.searchQuery,
            onItemClick = { item ->
                keyboardController?.hide()
                homeScreenNavigation.toItemDetail(item.shareId, item.id)
            },
            onItemMenuClick = onItemMenuClick,
            isLoading = uiState.homeListUiState.isLoading,
            isProcessingSearch = uiState.searchUiState.isProcessingSearch,
            isRefreshing = uiState.homeListUiState.isRefreshing,
            onRefresh = onRefresh,
            onScrollToTop = onScrollToTop,
            emptyContent = {
                if (uiState.searchUiState.inSearchMode) {
                    EmptySearchResults()
                } else {
                    HomeEmptyList(
                        onCreateItemClick = { onAddItemClick(uiState.homeListUiState.selectedShare) }
                    )
                }
            },
            header = {
                item { OnBoardingTips() }
            },
            footer = {
                item { Spacer(Modifier.height(64.dp)) }
            }
        )
        ConfirmItemDeletionDialog(
            state = itemToDelete,
            onDismiss = { itemToDelete = null },
            title = R.string.alert_confirm_item_send_to_trash_title,
            message = R.string.alert_confirm_item_send_to_trash_message,
            onConfirm = sendItemToTrash
        )
    }
}
