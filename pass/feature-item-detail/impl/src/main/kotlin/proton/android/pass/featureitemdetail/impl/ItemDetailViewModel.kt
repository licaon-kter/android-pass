package proton.android.pass.featureitemdetail.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Clock
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.commonuimodels.api.ItemTypeUiState
import proton.android.pass.data.api.usecases.GetItemById
import proton.android.pass.featureitemdetail.impl.common.MoreInfoUiState
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryManager
import proton.pass.domain.Item
import proton.pass.domain.ItemId
import proton.pass.domain.ItemType
import proton.pass.domain.ShareId
import javax.inject.Inject

@HiltViewModel
class ItemDetailViewModel @Inject constructor(
    private val snackbarDispatcher: SnackbarDispatcher,
    private val clock: Clock,
    private val telemetryManager: TelemetryManager,
    getItemById: GetItemById,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val shareId: ShareId =
        ShareId(requireNotNull(savedStateHandle.get<String>(CommonNavArgId.ShareId.key)))
    private val itemId: ItemId =
        ItemId(requireNotNull(savedStateHandle.get<String>(CommonNavArgId.ItemId.key)))

    val uiState: StateFlow<ItemDetailScreenUiState> = getItemById(shareId, itemId)
        .distinctUntilChanged()
        .map { result ->
            when (result) {
                is LoadingResult.Error -> {
                    PassLogger.e(TAG, result.exception, "Get by id error")
                    snackbarDispatcher(DetailSnackbarMessages.InitError)
                    ItemDetailScreenUiState.Initial
                }
                LoadingResult.Loading -> ItemDetailScreenUiState.Initial
                is LoadingResult.Success -> ItemDetailScreenUiState(
                    itemTypeUiState = when (result.data.itemType) {
                        is ItemType.Login -> ItemTypeUiState.Login
                        is ItemType.Note -> ItemTypeUiState.Note
                        is ItemType.Alias -> ItemTypeUiState.Alias
                        ItemType.Password -> ItemTypeUiState.Password
                    },
                    moreInfoUiState = getMoreInfoUiState(result.data)
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ItemDetailScreenUiState.Initial
        )

    fun sendItemReadEvent(itemTypeUiState: ItemTypeUiState) {
        val eventItemType: EventItemType? = when (itemTypeUiState) {
            ItemTypeUiState.Login -> EventItemType.Login
            ItemTypeUiState.Note -> EventItemType.Note
            ItemTypeUiState.Alias -> EventItemType.Alias
            ItemTypeUiState.Password -> EventItemType.Password
            ItemTypeUiState.Unknown -> null
        }
        eventItemType?.let {
            telemetryManager.sendEvent(ItemRead(eventItemType))
        }
    }

    private fun getMoreInfoUiState(item: Item): MoreInfoUiState = MoreInfoUiState(
        now = clock.now(),
        createdTime = item.createTime,
        lastAutofilled = item.lastAutofillTime,
        lastModified = item.modificationTime,
        numRevisions = item.revision
    )

    companion object {
        private const val TAG = "ItemDetailViewModel"
    }
}
