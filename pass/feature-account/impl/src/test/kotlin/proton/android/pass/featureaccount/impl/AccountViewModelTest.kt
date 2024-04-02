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

package proton.android.pass.featureaccount.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.UpgradeInfo
import proton.android.pass.data.fakes.usecases.TestObserveCurrentUser
import proton.android.pass.data.fakes.usecases.TestObserveCurrentUserSettings
import proton.android.pass.data.fakes.usecases.TestObserveUpgradeInfo
import proton.android.pass.domain.Plan
import proton.android.pass.domain.PlanLimit
import proton.android.pass.domain.PlanType
import proton.android.pass.notifications.fakes.TestSnackbarDispatcher
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.TestUser

internal class AccountViewModelTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var instance: AccountViewModel
    private lateinit var observeCurrentUser: TestObserveCurrentUser
    private lateinit var observeCurrentUserSettings: TestObserveCurrentUserSettings
    private lateinit var getUpgradeInfo: TestObserveUpgradeInfo
    private lateinit var snackbarDispatcher: TestSnackbarDispatcher

    @Before
    fun setup() {
        observeCurrentUser = TestObserveCurrentUser()
        observeCurrentUserSettings = TestObserveCurrentUserSettings()
        getUpgradeInfo = TestObserveUpgradeInfo()
        snackbarDispatcher = TestSnackbarDispatcher()

        instance = AccountViewModel(
            observeCurrentUser = observeCurrentUser,
            observeUpgradeInfo = getUpgradeInfo,
            observeCurrentUserSettings = observeCurrentUserSettings
        )
    }

    @Test
    fun `emits initial state`() = runTest {
        instance.state.test {
            assertThat(awaitItem()).isEqualTo(AccountUiState.Initial)
        }
    }

    @Test
    fun `emits user email and plan`() = runTest {
        val email = "test@email.local"
        val planType = PlanType.Paid.Plus(
            name = "internal",
            displayName = "testplan"
        )
        val plan = Plan(
            planType = planType,
            vaultLimit = PlanLimit.Unlimited,
            aliasLimit = PlanLimit.Unlimited,
            totpLimit = PlanLimit.Unlimited,
            updatedAt = 0,
            hideUpgrade = false
        )
        val user = TestUser.create(email = email)
        observeCurrentUser.sendUser(user)
        getUpgradeInfo.setResult(
            UpgradeInfo(
                plan = plan,
                isUpgradeAvailable = false,
                isSubscriptionAvailable = false,
                totalVaults = 0,
                totalAlias = 0,
                totalTotp = 0
            )
        )

        instance.state.test {
            val item = awaitItem()
            assertThat(item.isLoadingState).isEqualTo(IsLoadingState.NotLoading)
            assertThat(item.email).isEqualTo(email)
            assertThat(item.plan).isEqualTo(PlanSection.Data(planType.humanReadableName))
        }
    }
}
