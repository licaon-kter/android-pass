/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.featurehome.impl

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.DrawerValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.ModalDrawer
import androidx.compose.material.rememberDrawerState
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import proton.android.pass.common.api.Option
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonuimodels.api.ItemTypeUiState
import proton.android.pass.composecomponents.impl.bottomsheet.PassModalBottomSheetLayout
import proton.android.pass.composecomponents.impl.item.icon.AliasIcon
import proton.android.pass.composecomponents.impl.item.icon.CreditCardIcon
import proton.android.pass.composecomponents.impl.item.icon.LoginIcon
import proton.android.pass.composecomponents.impl.item.icon.NoteIcon
import proton.android.pass.featurehome.impl.HomeBottomSheetType.AliasOptions
import proton.android.pass.featurehome.impl.HomeBottomSheetType.CreditCardOptions
import proton.android.pass.featurehome.impl.HomeBottomSheetType.LoginOptions
import proton.android.pass.featurehome.impl.HomeBottomSheetType.NoteOptions
import proton.android.pass.featurehome.impl.HomeBottomSheetType.TrashItemOptions
import proton.android.pass.featurehome.impl.HomeBottomSheetType.TrashOptions
import proton.android.pass.featurehome.impl.HomeNavigation.SortingBottomsheet
import proton.android.pass.featurehome.impl.bottomsheet.AliasOptionsBottomSheetContents
import proton.android.pass.featurehome.impl.bottomsheet.CreditCardOptionsBottomSheetContents
import proton.android.pass.featurehome.impl.bottomsheet.LoginOptionsBottomSheetContents
import proton.android.pass.featurehome.impl.bottomsheet.NoteOptionsBottomSheetContents
import proton.android.pass.featurehome.impl.bottomsheet.TrashAllBottomSheetContents
import proton.android.pass.featurehome.impl.saver.HomeBottomSheetTypeSaver
import proton.android.pass.featurehome.impl.trash.ConfirmClearTrashDialog
import proton.android.pass.featurehome.impl.trash.ConfirmRestoreAllDialog
import proton.android.pass.featurehome.impl.vault.VaultDrawerContent
import proton.android.pass.featurehome.impl.vault.VaultDrawerViewModel
import proton.android.pass.featuresearchoptions.api.VaultSelectionOption
import proton.android.pass.featuretrash.impl.ConfirmDeleteItemDialog
import proton.android.pass.featuretrash.impl.ConfirmTrashAliasDialog
import proton.android.pass.featuretrash.impl.TrashItemBottomSheetContents
import proton.pass.domain.ItemContents
import proton.pass.domain.ShareId

@OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class
)
@Suppress("ComplexMethod")
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigateEvent: (HomeNavigation) -> Unit,
    homeViewModel: HomeViewModel = hiltViewModel(),
    routerViewModel: RouterViewModel = hiltViewModel(),
    vaultDrawerViewModel: VaultDrawerViewModel = hiltViewModel()
) {
    val routerEvent by routerViewModel.eventStateFlow.collectAsStateWithLifecycle()
    val homeUiState by homeViewModel.homeUiState.collectAsStateWithLifecycle()
    val drawerUiState by vaultDrawerViewModel.drawerUiState.collectAsStateWithLifecycle()

    var currentBottomSheet by rememberSaveable(stateSaver = HomeBottomSheetTypeSaver) {
        mutableStateOf(TrashOptions)
    }
    var selectedItem by rememberSaveable(stateSaver = ItemUiModelSaver) {
        mutableStateOf(null)
    }
    var shouldShowDeleteItemDialog by rememberSaveable { mutableStateOf(false) }
    var shouldShowRestoreAllDialog by rememberSaveable { mutableStateOf(false) }
    var shouldShowClearTrashDialog by rememberSaveable { mutableStateOf(false) }
    var aliasToBeTrashed by rememberSaveable(stateSaver = ItemUiModelSaver) { mutableStateOf(null) }

    val actionState = homeUiState.homeListUiState.actionState
    LaunchedEffect(actionState) {
        if (actionState == ActionState.Done) {
            shouldShowDeleteItemDialog = false
            shouldShowRestoreAllDialog = false
            shouldShowClearTrashDialog = false
            homeViewModel.restoreActionState()
        }
    }
    LaunchedEffect(routerEvent) {
        when (routerEvent) {
            RouterEvent.OnBoarding -> onNavigateEvent(HomeNavigation.OnBoarding)
            RouterEvent.SyncDialog -> onNavigateEvent(HomeNavigation.SyncDialog)
            RouterEvent.None -> {}
        }
    }

    val bottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val isTrashMode = homeUiState.homeListUiState.homeVaultSelection == VaultSelectionOption.Trash

    BackHandler(drawerState.isOpen || bottomSheetState.isVisible) {
        scope.launch {
            if (drawerState.isOpen) {
                drawerState.close()
            } else if (bottomSheetState.isVisible) {
                bottomSheetState.hide()
            }
        }
    }

    PassModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetContent = {
            when (currentBottomSheet) {
                LoginOptions -> LoginOptionsBottomSheetContents(
                    itemUiModel = selectedItem!!,
                    isRecentSearch = homeUiState.searchUiState.isInSuggestionsMode,
                    canLoadExternalImages = homeUiState.homeListUiState.canLoadExternalImages,
                    onCopyUsername = remember {
                        {
                            scope.launch { bottomSheetState.hide() }
                            homeViewModel.copyToClipboard(it, HomeClipboardType.Username)
                        }
                    },
                    onCopyPassword = remember {
                        {
                            scope.launch { bottomSheetState.hide() }
                            homeViewModel.copyToClipboard(it, HomeClipboardType.Password)
                        }
                    },
                    onEdit = remember {
                        { shareId, itemId ->
                            scope.launch { bottomSheetState.hide() }
                            onNavigateEvent(HomeNavigation.EditLogin(shareId, itemId))
                        }
                    },
                    onMoveToTrash = remember {
                        {
                            scope.launch {
                                bottomSheetState.hide()
                                homeViewModel.sendItemToTrash(it)
                            }
                        }
                    },
                    onRemoveFromRecentSearch = remember {
                        { shareId, itemId ->
                            scope.launch {
                                bottomSheetState.hide()
                                homeViewModel.onClearRecentSearch(shareId, itemId)
                            }
                        }
                    }
                )

                AliasOptions -> AliasOptionsBottomSheetContents(
                    itemUiModel = selectedItem!!,
                    isRecentSearch = homeUiState.searchUiState.isInSuggestionsMode,
                    onCopyAlias = remember {
                        {
                            scope.launch { bottomSheetState.hide() }
                            homeViewModel.copyToClipboard(it, HomeClipboardType.Alias)
                        }
                    },
                    onEdit = remember {
                        { shareId, itemId ->
                            scope.launch { bottomSheetState.hide() }
                            onNavigateEvent(HomeNavigation.EditAlias(shareId, itemId))
                        }
                    },
                    onMoveToTrash = remember {
                        {
                            scope.launch {
                                bottomSheetState.hide()
                                aliasToBeTrashed = it
                            }
                        }
                    },
                    onRemoveFromRecentSearch = remember {
                        { shareId, itemId ->
                            scope.launch {
                                bottomSheetState.hide()
                                homeViewModel.onClearRecentSearch(shareId, itemId)
                            }
                        }
                    }
                )

                NoteOptions -> NoteOptionsBottomSheetContents(
                    itemUiModel = selectedItem!!,
                    isRecentSearch = homeUiState.searchUiState.isInSuggestionsMode,
                    onCopyNote = remember {
                        {
                            scope.launch { bottomSheetState.hide() }
                            homeViewModel.copyToClipboard(it, HomeClipboardType.Note)
                        }
                    },
                    onEdit = remember {
                        { shareId, itemId ->
                            scope.launch { bottomSheetState.hide() }
                            onNavigateEvent(HomeNavigation.EditNote(shareId, itemId))
                        }
                    },
                    onMoveToTrash = remember {
                        {
                            scope.launch {
                                bottomSheetState.hide()
                                homeViewModel.sendItemToTrash(it)
                            }
                        }
                    },
                    onRemoveFromRecentSearch = remember {
                        { shareId, itemId ->
                            scope.launch {
                                bottomSheetState.hide()
                                homeViewModel.onClearRecentSearch(shareId, itemId)
                            }
                        }
                    }
                )

                CreditCardOptions -> CreditCardOptionsBottomSheetContents(
                    itemUiModel = selectedItem!!,
                    isRecentSearch = homeUiState.searchUiState.isInSuggestionsMode,
                    onCopyNumber = remember {
                        {
                            scope.launch { bottomSheetState.hide() }
                            homeViewModel.copyToClipboard(it, HomeClipboardType.CreditCardNumber)
                        }
                    },
                    onCopyCvv = remember {
                        {
                            scope.launch { bottomSheetState.hide() }
                            homeViewModel.copyToClipboard(it, HomeClipboardType.CreditCardCvv)
                        }
                    },
                    onEdit = remember {
                        { shareId, itemId ->
                            scope.launch { bottomSheetState.hide() }
                            onNavigateEvent(HomeNavigation.EditCreditCard(shareId, itemId))
                        }
                    },
                    onMoveToTrash = remember {
                        {
                            scope.launch {
                                bottomSheetState.hide()
                                homeViewModel.sendItemToTrash(it)
                            }
                        }
                    },
                    onRemoveFromRecentSearch = remember {
                        { shareId, itemId ->
                            scope.launch {
                                bottomSheetState.hide()
                                homeViewModel.onClearRecentSearch(shareId, itemId)
                            }
                        }
                    }
                )

                TrashItemOptions -> TrashItemBottomSheetContents(
                    itemUiModel = selectedItem!!,
                    onRestoreItem = remember {
                        { shareId, itemId ->
                            scope.launch {
                                bottomSheetState.hide()
                                homeViewModel.restoreItem(shareId, itemId)
                            }
                        }
                    },
                    onDeleteItem = remember {
                        { _, _ ->
                            scope.launch {
                                bottomSheetState.hide()
                                shouldShowDeleteItemDialog = true
                            }
                        }
                    },
                    icon = {
                        when (val contents = selectedItem!!.contents) {
                            is ItemContents.Login -> {
                                val sortedPackages = contents.packageInfoSet.sortedBy { it.packageName.value }
                                val packageName = sortedPackages.firstOrNull()?.packageName?.value
                                val website = contents.urls.firstOrNull()
                                LoginIcon(
                                    text = selectedItem!!.contents.title,
                                    canLoadExternalImages = homeUiState.homeListUiState.canLoadExternalImages,
                                    website = website,
                                    packageName = packageName,
                                )
                            }

                            is ItemContents.Alias -> AliasIcon()
                            is ItemContents.Note -> NoteIcon()
                            is ItemContents.CreditCard -> CreditCardIcon()
                            is ItemContents.Unknown -> {}
                        }
                    }
                )

                TrashOptions -> TrashAllBottomSheetContents(
                    onEmptyTrash = remember {
                        {
                            scope.launch {
                                bottomSheetState.hide()
                                shouldShowClearTrashDialog = true
                            }
                        }
                    },
                    onRestoreAll = remember {
                        {
                            scope.launch {
                                bottomSheetState.hide()
                                shouldShowRestoreAllDialog = true
                            }
                        }
                    }
                )
            }
        }
    ) {
        ModalDrawer(
            modifier = modifier,
            drawerState = drawerState,
            drawerShape = CutCornerShape(0.dp),
            scrimColor = PassTheme.colors.backdrop,
            drawerContent = {
                VaultDrawerContent(
                    homeVaultSelection = drawerUiState.vaultSelection,
                    list = drawerUiState.shares,
                    totalTrashedItems = drawerUiState.totalTrashedItems,
                    canCreateVault = drawerUiState.canCreateVault,
                    onAllVaultsClick = remember {
                        {
                            scope.launch { drawerState.close() }
                            vaultDrawerViewModel.setVaultSelection(VaultSelectionOption.AllVaults)
                            homeViewModel.setVaultSelection(VaultSelectionOption.AllVaults)
                        }
                    },
                    onVaultClick = remember {
                        {
                            scope.launch { drawerState.close() }
                            vaultDrawerViewModel.setVaultSelection(VaultSelectionOption.Vault(it))
                            homeViewModel.setVaultSelection(VaultSelectionOption.Vault(it))
                        }
                    },
                    onTrashClick = remember {
                        {
                            scope.launch { drawerState.close() }
                            vaultDrawerViewModel.setVaultSelection(VaultSelectionOption.Trash)
                            homeViewModel.setVaultSelection(VaultSelectionOption.Trash)
                        }
                    },
                    onCreateVaultClick = remember { { onNavigateEvent(HomeNavigation.CreateVault) } },
                    onVaultOptionsClick = remember { { onNavigateEvent(HomeNavigation.VaultOptions(it.id)) } }
                )
            }
        ) {
            HomeContent(
                modifier = Modifier.background(PassTheme.colors.backgroundStrong),
                uiState = homeUiState,
                shouldScrollToTop = homeUiState.homeListUiState.shouldScrollToTop,
                onItemClick = remember {
                    { item ->
                        homeViewModel.onItemClicked(item.shareId, item.id)
                        onNavigateEvent(HomeNavigation.ItemDetail(item.shareId, item.id))
                    }
                },
                onSearchQueryChange = remember { { homeViewModel.onSearchQueryChange(it) } },
                onEnterSearch = remember { { homeViewModel.onEnterSearch() } },
                onStopSearch = remember { { homeViewModel.onStopSearching() } },
                onDrawerIconClick = remember { { scope.launch { drawerState.open() } } },
                onSortingOptionsClick = remember {
                    {
                        onNavigateEvent(SortingBottomsheet(homeUiState.homeListUiState.sortingType))
                    }
                },
                onAddItemClick = remember {
                    { shareId: Option<ShareId>, itemTypeUiState: ItemTypeUiState ->
                        onNavigateEvent(HomeNavigation.AddItem(shareId, itemTypeUiState))
                    }
                },
                onItemMenuClick = remember {
                    { item ->
                        selectedItem = item
                        currentBottomSheet =
                            if (isTrashMode) {
                                TrashItemOptions
                            } else {
                                when (item.contents) {
                                    is ItemContents.Alias -> AliasOptions
                                    is ItemContents.Login -> LoginOptions
                                    is ItemContents.Note -> NoteOptions
                                    is ItemContents.CreditCard -> CreditCardOptions
                                    is ItemContents.Unknown -> LoginOptions
                                }
                            }
                        scope.launch { bottomSheetState.show() }
                    }
                },
                onRefresh = remember { { homeViewModel.onRefresh() } },
                onScrollToTop = remember { { homeViewModel.onScrollToTop() } },
                onProfileClick = remember { { onNavigateEvent(HomeNavigation.Profile) } },
                onItemTypeSelected = remember { { homeViewModel.setItemTypeSelection(it) } },
                onTrashActionsClick = remember {
                    {
                        currentBottomSheet = TrashOptions
                        scope.launch { bottomSheetState.show() }
                    }
                },
                onClearRecentSearchClick = homeViewModel::onClearAllRecentSearch,
                onTrialInfoClick = remember { { onNavigateEvent(HomeNavigation.TrialInfo) } },
                onInviteClick = remember { { onNavigateEvent(HomeNavigation.OpenInvite) } }
            )

            ConfirmRestoreAllDialog(
                show = shouldShowRestoreAllDialog,
                isLoading = actionState == ActionState.Loading,
                onDismiss = remember {
                    {
                        shouldShowRestoreAllDialog = false
                    }
                },
                onConfirm = remember {
                    {
                        homeViewModel.restoreItems()
                    }
                }
            )

            ConfirmClearTrashDialog(
                show = shouldShowClearTrashDialog,
                isLoading = actionState == ActionState.Loading,
                onDismiss = remember {
                    {
                        shouldShowClearTrashDialog = false
                    }
                },
                onConfirm = remember {
                    {
                        homeViewModel.clearTrash()
                    }
                }
            )

            ConfirmDeleteItemDialog(
                isLoading = actionState == ActionState.Loading,
                show = shouldShowDeleteItemDialog,
                onConfirm = remember {
                    {
                        selectedItem?.let {
                            homeViewModel.deleteItem(it)
                        }
                    }
                },
                onDismiss = remember { { shouldShowDeleteItemDialog = false } }
            )

            ConfirmTrashAliasDialog(
                show = aliasToBeTrashed != null,
                onConfirm = remember {
                    {
                        homeViewModel.sendItemToTrash(aliasToBeTrashed)
                        aliasToBeTrashed = null
                    }
                },
                onDismiss = remember { { aliasToBeTrashed = null } }
            )
        }
    }
}

object HomeScreenTestTag {
    const val screen = "homeScreen"
}
