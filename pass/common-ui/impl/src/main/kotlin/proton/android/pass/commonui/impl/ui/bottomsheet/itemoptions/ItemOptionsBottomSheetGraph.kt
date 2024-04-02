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

package proton.android.pass.commonui.impl.ui.bottomsheet.itemoptions

import androidx.activity.compose.BackHandler
import androidx.navigation.NavGraphBuilder
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavItemType
import proton.android.pass.navigation.api.bottomSheet

sealed interface ItemOptionsNavigation {
    data object Close : ItemOptionsNavigation
}

object ItemOptionsBottomSheet : NavItem(
    baseRoute = "itemoptions/bottomsheet",
    navArgIds = listOf(CommonNavArgId.ShareId, CommonNavArgId.ItemId),
    navItemType = NavItemType.Bottomsheet
) {
    fun createRoute(shareId: ShareId, itemId: ItemId) = "$baseRoute/${shareId.id}/${itemId.id}"
}

fun NavGraphBuilder.itemOptionsGraph(onNavigate: (ItemOptionsNavigation) -> Unit) {
    bottomSheet(ItemOptionsBottomSheet) {
        BackHandler {
            onNavigate(ItemOptionsNavigation.Close)
        }
        ItemOptionsBottomSheet(
            onNavigate = onNavigate
        )
    }
}
