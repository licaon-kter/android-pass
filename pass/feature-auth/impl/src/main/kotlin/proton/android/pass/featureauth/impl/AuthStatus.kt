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

package proton.android.pass.featureauth.impl

import androidx.compose.runtime.Stable
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState

sealed interface AuthEvent {
    data object Success : AuthEvent
    data object Failed : AuthEvent
    data object Canceled : AuthEvent
    data object SignOut : AuthEvent
    data object ForceSignOut : AuthEvent
    data object EnterPin : AuthEvent
    data object EnterBiometrics : AuthEvent
    data object Unknown : AuthEvent
}

enum class AuthMethod {
    Pin,
    Fingerprint
}

sealed interface AuthError {
    @JvmInline
    value class WrongPassword(val remainingAttempts: Int) : AuthError
    data object UnknownError : AuthError
}

sealed interface PasswordError {
    data object EmptyPassword : PasswordError
}

data class AuthContent(
    val password: String,
    val address: Option<String>,
    val isLoadingState: IsLoadingState,
    val isPasswordVisible: Boolean,
    val error: Option<AuthError>,
    val passwordError: Option<PasswordError>,
    val authMethod: Option<AuthMethod>
) {
    companion object {
        fun default(address: Option<String>) = AuthContent(
            password = "",
            address = address,
            isLoadingState = IsLoadingState.NotLoading,
            isPasswordVisible = false,
            error = None,
            passwordError = None,
            authMethod = None
        )
    }
}

@Stable
data class AuthState(
    val event: Option<AuthEvent>,
    val content: AuthContent
) {

    companion object {
        val Initial = AuthState(
            event = None,
            content = AuthContent.default(None)
        )
    }

}
