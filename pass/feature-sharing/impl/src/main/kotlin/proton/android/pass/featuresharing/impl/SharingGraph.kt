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

package proton.android.pass.featuresharing.impl

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import proton.android.pass.domain.InviteId
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.NewUserInviteId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole
import proton.android.pass.featuresharing.impl.accept.AcceptInviteBottomSheet
import proton.android.pass.featuresharing.impl.confirmed.InviteConfirmedBottomSheet
import proton.android.pass.featuresharing.impl.invitesinfo.InvitesErrorDialog
import proton.android.pass.featuresharing.impl.invitesinfo.InvitesInfoDialog
import proton.android.pass.featuresharing.impl.manage.ManageVaultScreen
import proton.android.pass.featuresharing.impl.manage.bottomsheet.memberOptionsBottomSheetGraph
import proton.android.pass.featuresharing.impl.sharefromitem.ShareFromItemBottomSheet
import proton.android.pass.featuresharing.impl.sharingpermissions.SharingPermissionsScreen
import proton.android.pass.featuresharing.impl.sharingpermissions.bottomsheet.sharingPermissionsBottomsheetGraph
import proton.android.pass.featuresharing.impl.sharingsummary.SharingSummaryScreen
import proton.android.pass.featuresharing.impl.sharingwith.SharingWithScreen
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavItemType
import proton.android.pass.navigation.api.bottomSheet
import proton.android.pass.navigation.api.composable
import proton.android.pass.navigation.api.dialog

object ShowEditVaultArgId : NavArgId {
    override val key: String = "show_edit_vault"
    override val navType: NavType<*> = NavType.BoolType
}

object SharingWith : NavItem(
    baseRoute = "sharing/with/screen",
    navArgIds = listOf(CommonNavArgId.ShareId, ShowEditVaultArgId)
) {
    fun createRoute(shareId: ShareId, showEditVault: Boolean) = "$baseRoute/${shareId.id}/$showEditVault"
}

object SharingPermissions : NavItem(
    baseRoute = "sharing/permissions/screen",
    navArgIds = listOf(CommonNavArgId.ShareId)
) {
    fun createRoute(shareId: ShareId) = "$baseRoute/${shareId.id}"
}

object AcceptInvite : NavItem("sharing/accept", navItemType = NavItemType.Bottomsheet)

object SharingSummary : NavItem(
    baseRoute = "sharing/summary/screen",
    navArgIds = listOf(CommonNavArgId.ShareId)
) {
    fun createRoute(shareId: ShareId) = "$baseRoute/${shareId.id}"
}

object ManageVault : NavItem(
    baseRoute = "sharing/manage/screen",
    navArgIds = listOf(CommonNavArgId.ShareId)
) {
    fun createRoute(shareId: ShareId) = "$baseRoute/${shareId.id}"
}

object InviteConfirmed : NavItem(
    baseRoute = "sharing/confirmed/bottomsheet",
    navItemType = NavItemType.Bottomsheet
)

object InvitesInfoDialog : NavItem(
    baseRoute = "sharing/manage/invites/dialog",
    navArgIds = listOf(CommonNavArgId.ShareId),
    navItemType = NavItemType.Dialog
) {
    fun buildRoute(shareId: ShareId) = "$baseRoute/${shareId.id}"
}

object InvitesErrorDialog : NavItem(
    baseRoute = "sharing/manage/invites/error/dialog",
    navItemType = NavItemType.Dialog
)

object ShareFromItem : NavItem(
    baseRoute = "sharing/fromitem/bottomsheet",
    navArgIds = listOf(CommonNavArgId.ShareId, CommonNavArgId.ItemId),
    navItemType = NavItemType.Bottomsheet
) {
    fun buildRoute(shareId: ShareId, itemId: ItemId) = "$baseRoute/${shareId.id}/${itemId.id}"
}

sealed interface SharingNavigation {
    data object Back : SharingNavigation
    data object BackToHome : SharingNavigation
    data object Upgrade : SharingNavigation

    @JvmInline
    value class ShowInvitesInfo(val shareId: ShareId) : SharingNavigation

    @JvmInline
    value class CloseBottomSheet(val refresh: Boolean) : SharingNavigation

    @JvmInline
    value class Permissions(val shareId: ShareId) : SharingNavigation

    @JvmInline
    value class Summary(val shareId: ShareId) : SharingNavigation

    @JvmInline
    value class ShareVault(val shareId: ShareId) : SharingNavigation

    @JvmInline
    value class ManageVault(val shareId: ShareId) : SharingNavigation

    data class MemberOptions(
        val shareId: ShareId,
        val memberRole: ShareRole,
        val destShareId: ShareId,
        val destEmail: String
    ) : SharingNavigation

    data class ExistingUserInviteOptions(
        val shareId: ShareId,
        val inviteId: InviteId
    ) : SharingNavigation

    data class NewUserInviteOptions(
        val shareId: ShareId,
        val inviteId: NewUserInviteId
    ) : SharingNavigation

    data class TransferOwnershipConfirm(
        val shareId: ShareId,
        val destShareId: ShareId,
        val destEmail: String
    ) : SharingNavigation

    data object MoveItemToSharedVault : SharingNavigation

    data class CreateVaultAndMoveItem(
        val shareId: ShareId,
        val itemId: ItemId
    ) : SharingNavigation

    @JvmInline
    value class EditVault(val shareId: ShareId) : SharingNavigation

    @JvmInline
    value class ViewVault(val shareId: ShareId) : SharingNavigation

    data class InviteToVaultEditPermissions(
        val email: String,
        val permission: ShareRole
    ) : SharingNavigation

    data object InviteToVaultEditAllPermissions : SharingNavigation

    data object InviteError : SharingNavigation
}

fun NavGraphBuilder.sharingGraph(onNavigateEvent: (SharingNavigation) -> Unit) {
    composable(SharingWith) {
        SharingWithScreen(onNavigateEvent = onNavigateEvent)
    }

    composable(SharingPermissions) {
        SharingPermissionsScreen(onNavigateEvent = onNavigateEvent)
    }

    composable(SharingSummary) {
        SharingSummaryScreen(onNavigateEvent = onNavigateEvent)
    }

    bottomSheet(AcceptInvite) {
        BackHandler { onNavigateEvent(SharingNavigation.BackToHome) }
        AcceptInviteBottomSheet(onNavigateEvent = onNavigateEvent)
    }

    composable(ManageVault) {
        val refresh by it.savedStateHandle
            .getStateFlow(REFRESH_MEMBER_LIST_FLAG, false)
            .collectAsStateWithLifecycle()
        ManageVaultScreen(
            refresh = refresh,
            onNavigateEvent = onNavigateEvent,
            clearRefreshFlag = { it.savedStateHandle.remove<String>(REFRESH_MEMBER_LIST_FLAG) }
        )
    }

    bottomSheet(InviteConfirmed) {
        BackHandler { onNavigateEvent(SharingNavigation.BackToHome) }
        InviteConfirmedBottomSheet(onNavigateEvent = onNavigateEvent)
    }

    dialog(InvitesInfoDialog) {
        BackHandler { onNavigateEvent(SharingNavigation.CloseBottomSheet(false)) }
        InvitesInfoDialog(onNavigateEvent = onNavigateEvent)
    }

    dialog(InvitesErrorDialog) {
        InvitesErrorDialog(onNavigateEvent = onNavigateEvent)
    }

    bottomSheet(ShareFromItem) {
        ShareFromItemBottomSheet(onNavigateEvent = onNavigateEvent)
    }

    memberOptionsBottomSheetGraph(onNavigateEvent)
    sharingPermissionsBottomsheetGraph(onNavigateEvent)
}

const val REFRESH_MEMBER_LIST_FLAG = "refreshMemberList"
