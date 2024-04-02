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

package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import me.proton.core.payment.domain.PaymentManager
import me.proton.core.usersettings.domain.entity.UserSettings
import me.proton.core.usersettings.domain.usecase.ObserveUserSettings
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.ObserveCurrentUserSettings
import proton.android.pass.data.api.usecases.ObserveItemCount
import proton.android.pass.data.api.usecases.ObserveMFACount
import proton.android.pass.data.api.usecases.ObserveUpgradeInfo
import proton.android.pass.data.api.usecases.ObserveVaultCount
import proton.android.pass.data.api.usecases.UpgradeInfo
import proton.android.pass.data.impl.repositories.PlanRepository
import javax.inject.Inject

class ObserveCurrentUserSettingsImpl @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUser,
    private val observeUserSettings: ObserveUserSettings
) : ObserveCurrentUserSettings {
    override fun invoke(): Flow<UserSettings?> = observeCurrentUser()
        .distinctUntilChanged()
        .flatMapLatest { user ->
            observeUserSettings(user.userId)
        }
        .distinctUntilChanged()
}
