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

package proton.android.pass.featuresharing.impl.sharingwith

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.key.domain.repository.PublicAddressRepository
import me.proton.core.key.domain.repository.getPublicAddressOrNull
import proton.android.pass.common.api.CommonRegex.EMAIL_VALIDATION_REGEX
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.getOrNull
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.GetVaultById
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.pass.domain.ShareId
import javax.inject.Inject

@HiltViewModel
class SharingWithViewModel @Inject constructor(
    private val publicAddressRepository: PublicAddressRepository,
    private val accountManager: AccountManager,
    getVaultById: GetVaultById,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val shareId: ShareId =
        ShareId(savedStateHandleProvider.get().require(CommonNavArgId.ShareId.key))

    private val isEmailNotValidState: MutableStateFlow<EmailNotValidReason?> =
        MutableStateFlow(null)
    private val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)
    private val emailState: MutableStateFlow<String> =
        MutableStateFlow("")
    private val eventState: MutableStateFlow<SharingWithEvents> =
        MutableStateFlow(SharingWithEvents.Unknown)

    val state: StateFlow<SharingWithUIState> = combine(
        emailState,
        isEmailNotValidState,
        getVaultById(shareId = shareId).asLoadingResult(),
        isLoadingState,
        eventState
    ) { email, isEmailNotValid, vault, isLoading, event ->
        SharingWithUIState(
            email = email,
            vaultName = vault.getOrNull()?.name,
            emailNotValidReason = isEmailNotValid,
            isVaultNotFound = vault is LoadingResult.Error,
            isLoading = isLoading.value() || vault is LoadingResult.Loading,
            event = event
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SharingWithUIState()
    )

    fun onEmailChange(value: String) {
        val sanitised = value.replace(" ", "").replace("\n", "")
        emailState.update { sanitised }
        isEmailNotValidState.update { null }
    }

    fun onEmailSubmit() = viewModelScope.launch {
        isLoadingState.update { IsLoadingState.Loading }
        val email = emailState.value
        val userId = accountManager.getPrimaryUserId().firstOrNull()
        userId ?: run {
            PassLogger.i(TAG, "User id not found")
            isEmailNotValidState.update { EmailNotValidReason.UserIdNotFound }
            isLoadingState.update { IsLoadingState.NotLoading }
            return@launch
        }
        val publicAddress = publicAddressRepository.getPublicAddressOrNull(userId, email)
        when {
            email.isBlank() || !EMAIL_VALIDATION_REGEX.matches(email) -> {
                PassLogger.i(TAG, "Email not valid")
                isEmailNotValidState.update { EmailNotValidReason.NotValid }
            }

            publicAddress?.keys?.isEmpty() ?: true -> {
                PassLogger.i(TAG, "Cannot share vault with email")
                isEmailNotValidState.update { EmailNotValidReason.NotShareable }
            }

            else -> eventState.update { SharingWithEvents.NavigateToPermissions(shareId, email) }
        }
        isLoadingState.update { IsLoadingState.NotLoading }
    }

    fun clearEvent() {
        eventState.update { SharingWithEvents.Unknown }
    }

    companion object {
        private const val TAG = "SharingWithViewModel"
    }
}

enum class EmailNotValidReason {
    NotValid,
    NotShareable,
    UserIdNotFound
}
