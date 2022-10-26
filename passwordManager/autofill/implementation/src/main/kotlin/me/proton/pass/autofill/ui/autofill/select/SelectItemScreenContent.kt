package me.proton.pass.autofill.ui.autofill.select

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.android.pass.ui.shared.LoadingDialog
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.autofill.service.R
import me.proton.pass.commonui.api.PairPreviewProvider
import me.proton.pass.commonui.api.ThemePreviewProvider
import me.proton.pass.presentation.components.common.item.ItemsList
import me.proton.pass.presentation.components.model.ItemUiModel
import me.proton.pass.presentation.uievents.IsLoadingState

@Composable
internal fun SelectItemScreenContent(
    modifier: Modifier = Modifier,
    uiState: SelectItemUiState,
    onItemClicked: (ItemUiModel) -> Unit,
    onRefresh: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onEnterSearch: () -> Unit,
    onStopSearching: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            SelectItemTopAppBar(
                searchQuery = uiState.searchUiState.searchQuery,
                inSearchMode = uiState.searchUiState.inSearchMode,
                onSearchQueryChange = onSearchQueryChange,
                onEnterSearch = onEnterSearch,
                onStopSearching = onStopSearching
            )
        }
    ) { padding ->
        when (uiState.listUiState.isLoading) {
            IsLoadingState.Loading -> LoadingDialog()
            IsLoadingState.NotLoading -> {
                ItemsList(
                    modifier = modifier.padding(padding),
                    items = uiState.listUiState.items,
                    emptyListMessage = R.string.error_credentials_not_found,
                    onRefresh = onRefresh,
                    isRefreshing = uiState.listUiState.isRefreshing,
                    onItemClick = onItemClicked
                )
            }
        }
    }
}

class ThemeAndSelectItemUiStateProvider : PairPreviewProvider<Boolean, SelectItemUiState>(
    ThemePreviewProvider() to SelectItemUiStatePreviewProvider()
)

@Preview
@Composable
fun PreviewSelectItemScreenContent(
    @PreviewParameter(ThemeAndSelectItemUiStateProvider::class) input: Pair<Boolean, SelectItemUiState>
) {
    ProtonTheme(isDark = input.first) {
        Surface {
            SelectItemScreenContent(
                uiState = input.second,
                onItemClicked = {},
                onRefresh = {},
                onSearchQueryChange = {},
                onEnterSearch = {},
                onStopSearching = {}
            )
        }
    }
}
