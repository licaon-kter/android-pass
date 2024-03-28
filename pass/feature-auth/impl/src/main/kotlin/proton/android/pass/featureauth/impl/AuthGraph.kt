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

import androidx.activity.compose.BackHandler
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavItemType
import proton.android.pass.navigation.api.bottomSheet
import proton.android.pass.navigation.api.composable

const val AUTH_GRAPH = "auth_graph"

object Auth : NavItem(baseRoute = "auth", noHistory = true)

object EnterPin : NavItem(
    baseRoute = "pin/enter/bottomsheet",
    noHistory = true,
    navItemType = NavItemType.Bottomsheet
)

sealed interface AuthNavigation {

    data object Success : AuthNavigation

    data object Failed : AuthNavigation

    data object Dismissed : AuthNavigation

    data object SignOut : AuthNavigation

    data object ForceSignOut : AuthNavigation

    data object EnterPin : AuthNavigation

    data object Back : AuthNavigation

}

fun NavGraphBuilder.authGraph(canLogout: Boolean, navigation: (AuthNavigation) -> Unit) {
    navigation(
        route = AUTH_GRAPH,
        startDestination = Auth.route
    ) {
        composable(Auth) {
            BackHandler { navigation(AuthNavigation.Back) }
            AuthScreen(
                canLogout = canLogout,
                navigation = navigation
            )
        }

        bottomSheet(EnterPin) {
            EnterPinBottomsheet(
                onNavigate = { destination ->
                    when (destination) {
                        EnterPinNavigation.Success -> navigation(AuthNavigation.Success)
                        EnterPinNavigation.ForceSignOut -> navigation(AuthNavigation.ForceSignOut)
                    }
                }
            )
        }

    }
}
