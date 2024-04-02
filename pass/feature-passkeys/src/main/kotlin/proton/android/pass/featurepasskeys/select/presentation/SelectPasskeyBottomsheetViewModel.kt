/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.featurepasskeys.select.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.commonui.api.toItemContents
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.GetItemById
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.Passkey
import proton.android.pass.domain.ShareId
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import javax.annotation.concurrent.Immutable
import javax.inject.Inject

@Immutable
sealed interface SelectPasskeyBottomsheetEvent {
    @Immutable
    data object Idle : SelectPasskeyBottomsheetEvent

    @Immutable
    data object Close : SelectPasskeyBottomsheetEvent

    @Immutable
    @JvmInline
    value class OnSelected(val passkey: Passkey) : SelectPasskeyBottomsheetEvent
}

@Immutable
data class SelectPasskeyBottomsheetState(
    val event: SelectPasskeyBottomsheetEvent,
    val isLoading: IsLoadingState,
    val passkeys: ImmutableList<Passkey>
)

@HiltViewModel
class SelectPasskeyBottomsheetViewModel @Inject constructor(
    private val getItemById: GetItemById,
    private val encryptionContextProvider: EncryptionContextProvider,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val shareId: ShareId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ShareId.key)
        .let { ShareId(it) }
    private val itemId: ItemId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ItemId.key)
        .let { ItemId(it) }

    private val eventFlow: MutableStateFlow<SelectPasskeyBottomsheetEvent> =
        MutableStateFlow(SelectPasskeyBottomsheetEvent.Idle)

    private val itemFlow: Flow<LoadingResult<ImmutableList<Passkey>>> = oneShot {
        getPasskeys().fold(
            onSuccess = { it },
            onFailure = { throw it }
        )
    }.asLoadingResult()

    val state: StateFlow<SelectPasskeyBottomsheetState> = combine(
        eventFlow,
        itemFlow
    ) { event, itemResult ->
        when (itemResult) {
            is LoadingResult.Loading -> {
                SelectPasskeyBottomsheetState(
                    event = event,
                    isLoading = IsLoadingState.Loading,
                    passkeys = persistentListOf()
                )
            }

            is LoadingResult.Success -> {
                SelectPasskeyBottomsheetState(
                    event = event,
                    isLoading = IsLoadingState.NotLoading,
                    passkeys = itemResult.data
                )
            }

            is LoadingResult.Error -> {
                PassLogger.w(TAG, "Error loading passkeys")
                PassLogger.w(TAG, itemResult.exception)

                SelectPasskeyBottomsheetState(
                    event = SelectPasskeyBottomsheetEvent.Close,
                    isLoading = IsLoadingState.NotLoading,
                    passkeys = persistentListOf()
                )
            }
        }

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SelectPasskeyBottomsheetState(
            event = SelectPasskeyBottomsheetEvent.Idle,
            isLoading = IsLoadingState.Loading,
            passkeys = persistentListOf()
        )
    )

    fun onPasskeySelected(passkey: Passkey) = viewModelScope.launch {
        eventFlow.update { SelectPasskeyBottomsheetEvent.OnSelected(passkey) }
    }

    fun clearEvent() {
        eventFlow.update { SelectPasskeyBottomsheetEvent.Idle }
    }

    private suspend fun getPasskeys(): Result<ImmutableList<Passkey>> = runCatching {
        getItemById(shareId, itemId)
    }.fold(
        onSuccess = { item ->
            val itemContents = encryptionContextProvider.withEncryptionContext {
                item.toItemContents(this@withEncryptionContext)
            }

            when (itemContents) {
                is ItemContents.Login -> {
                    Result.success(itemContents.passkeys.toPersistentList())
                }

                else -> {
                    Result.failure(IllegalStateException("Received ItemContents that are not ItemContents.Login"))
                }
            }
        },
        onFailure = { Result.failure(it) }
    )

    companion object {
        private const val TAG = "SelectPasskeyBottomsheetViewModel"
    }

}
