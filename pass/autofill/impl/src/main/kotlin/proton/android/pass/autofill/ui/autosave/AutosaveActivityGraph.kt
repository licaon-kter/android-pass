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

package proton.android.pass.autofill.ui.autosave

import androidx.navigation.NavGraphBuilder
import proton.android.pass.autofill.entities.usernamePassword
import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.featureauth.impl.AuthNavigation
import proton.android.pass.featureauth.impl.EnterPin
import proton.android.pass.featureauth.impl.authGraph
import proton.android.pass.featureitemcreate.impl.alias.CreateAliasBottomSheet
import proton.android.pass.featureitemcreate.impl.bottomsheets.customfield.AddCustomFieldBottomSheet
import proton.android.pass.featureitemcreate.impl.bottomsheets.customfield.CustomFieldOptionsBottomSheet
import proton.android.pass.featureitemcreate.impl.common.KEY_VAULT_SELECTED
import proton.android.pass.featureitemcreate.impl.dialogs.CustomFieldNameDialog
import proton.android.pass.featureitemcreate.impl.dialogs.EditCustomFieldNameDialog
import proton.android.pass.featureitemcreate.impl.login.BaseLoginNavigation
import proton.android.pass.featureitemcreate.impl.login.CreateLogin
import proton.android.pass.featureitemcreate.impl.login.CreateLoginNavigation
import proton.android.pass.featureitemcreate.impl.login.InitialCreateLoginUiState
import proton.android.pass.featureitemcreate.impl.login.createUpdateLoginGraph
import proton.android.pass.featureitemcreate.impl.totp.CameraTotp
import proton.android.pass.featureitemcreate.impl.totp.PhotoPickerTotp
import proton.android.pass.featurepassword.impl.GeneratePasswordBottomsheet
import proton.android.pass.featurepassword.impl.GeneratePasswordBottomsheetModeValue
import proton.android.pass.featurepassword.impl.GeneratePasswordNavigation
import proton.android.pass.featurepassword.impl.dialog.mode.PasswordModeDialog
import proton.android.pass.featurepassword.impl.dialog.separator.WordSeparatorDialog
import proton.android.pass.featurepassword.impl.generatePasswordBottomsheetGraph
import proton.android.pass.featurevault.impl.VaultNavigation
import proton.android.pass.featurevault.impl.bottomsheet.select.SelectVaultBottomsheet
import proton.android.pass.featurevault.impl.vaultGraph
import proton.android.pass.navigation.api.AppNavigator

@Suppress("ComplexMethod", "LongMethod")
fun NavGraphBuilder.autosaveActivityGraph(
    appNavigator: AppNavigator,
    arguments: AutoSaveArguments,
    onNavigate: (AutosaveNavigation) -> Unit,
    dismissBottomSheet: (() -> Unit) -> Unit
) {
    authGraph(
        canLogout = false,
        navigation = {
            when (it) {
                AuthNavigation.Back -> onNavigate(AutosaveNavigation.Cancel)
                AuthNavigation.Success -> appNavigator.navigate(CreateLogin)
                AuthNavigation.Dismissed -> onNavigate(AutosaveNavigation.Cancel)
                AuthNavigation.Failed -> onNavigate(AutosaveNavigation.Cancel)
                AuthNavigation.SignOut -> {}
                AuthNavigation.ForceSignOut -> onNavigate(AutosaveNavigation.ForceSignOut)
                AuthNavigation.EnterPin -> appNavigator.navigate(EnterPin)
            }
        }
    )
    createUpdateLoginGraph(
        initialCreateLoginUiState = getInitialState(arguments),
        showCreateAliasButton = false,
        onNavigate = {
            when (it) {
                BaseLoginNavigation.Close -> dismissBottomSheet {
                    onNavigate(AutosaveNavigation.Cancel)
                }

                is BaseLoginNavigation.CreateAlias -> appNavigator.navigate(
                    destination = CreateAliasBottomSheet,
                    route = CreateAliasBottomSheet.createNavRoute(
                        it.shareId,
                        it.showUpgrade,
                        it.title
                    )
                )

                BaseLoginNavigation.GeneratePassword -> appNavigator.navigate(
                    destination = GeneratePasswordBottomsheet,
                    route = GeneratePasswordBottomsheet.buildRoute(
                        mode = GeneratePasswordBottomsheetModeValue.CancelConfirm
                    )
                )

                is BaseLoginNavigation.OnCreateLoginEvent -> when (val event = it.event) {
                    is CreateLoginNavigation.LoginCreated -> onNavigate(AutosaveNavigation.Success)

                    is CreateLoginNavigation.SelectVault -> {
                        appNavigator.navigate(
                            destination = SelectVaultBottomsheet,
                            route = SelectVaultBottomsheet.createNavRoute(event.shareId)
                        )
                    }
                }

                is BaseLoginNavigation.ScanTotp -> appNavigator.navigate(
                    destination = CameraTotp,
                    route = CameraTotp.createNavRoute(it.index)
                )

                BaseLoginNavigation.Upgrade -> onNavigate(AutosaveNavigation.Upgrade)

                BaseLoginNavigation.AddCustomField -> appNavigator.navigate(
                    destination = AddCustomFieldBottomSheet
                )

                is BaseLoginNavigation.CustomFieldTypeSelected -> dismissBottomSheet {
                    appNavigator.navigate(
                        destination = CustomFieldNameDialog,
                        route = CustomFieldNameDialog.buildRoute(it.type),
                        backDestination = CreateLogin
                    )
                }

                is BaseLoginNavigation.CustomFieldOptions -> appNavigator.navigate(
                    destination = CustomFieldOptionsBottomSheet,
                    route = CustomFieldOptionsBottomSheet.buildRoute(it.index, it.currentValue)
                )

                is BaseLoginNavigation.EditCustomField -> dismissBottomSheet {
                    appNavigator.navigate(
                        destination = EditCustomFieldNameDialog,
                        route = EditCustomFieldNameDialog.buildRoute(it.index, it.currentValue),
                        backDestination = CreateLogin
                    )
                }

                BaseLoginNavigation.RemovedCustomField -> dismissBottomSheet {
                    appNavigator.navigateBack(comesFromBottomsheet = true)
                }

                // Updates cannot happen
                is BaseLoginNavigation.OnUpdateLoginEvent -> {}
                // Aliases not allowed
                is BaseLoginNavigation.AliasOptions -> {}
                BaseLoginNavigation.DeleteAlias -> {}
                is BaseLoginNavigation.EditAlias -> {}
                is BaseLoginNavigation.OpenImagePicker -> appNavigator.navigate(
                    destination = PhotoPickerTotp,
                    route = PhotoPickerTotp.createNavRoute(it.index),
                    backDestination = CreateLogin
                )

                BaseLoginNavigation.TotpCancel -> appNavigator.navigateBack()
                is BaseLoginNavigation.TotpSuccess ->
                    appNavigator.navigateBackWithResult(it.results)
            }
        }
    )
    generatePasswordBottomsheetGraph(
        onNavigate = {
            when (it) {
                GeneratePasswordNavigation.CloseDialog -> appNavigator.navigateBack()
                GeneratePasswordNavigation.DismissBottomsheet -> dismissBottomSheet {
                    appNavigator.navigateBack(comesFromBottomsheet = true)
                }

                GeneratePasswordNavigation.OnSelectWordSeparator -> appNavigator.navigate(
                    destination = WordSeparatorDialog
                )

                GeneratePasswordNavigation.OnSelectPasswordMode -> appNavigator.navigate(
                    destination = PasswordModeDialog
                )
            }
        }
    )
    vaultGraph(
        onNavigate = {
            when (it) {
                VaultNavigation.Close -> appNavigator.navigateBack()
                VaultNavigation.Upgrade -> onNavigate(AutosaveNavigation.Upgrade)
                is VaultNavigation.VaultSelected -> dismissBottomSheet {
                    appNavigator.navigateBackWithResult(
                        key = KEY_VAULT_SELECTED,
                        value = it.shareId.id,
                        comesFromBottomsheet = true
                    )
                }

                is VaultNavigation.VaultEdit,
                is VaultNavigation.VaultMigrate,
                is VaultNavigation.VaultRemove,
                is VaultNavigation.VaultShare,
                is VaultNavigation.VaultLeave,
                is VaultNavigation.VaultAccess -> {
                }
            }
        }
    )
}

private fun getInitialState(arguments: AutoSaveArguments): InitialCreateLoginUiState {
    val saveInformation = arguments.saveInformation
    val (username, password) = saveInformation.usernamePassword()
    val packageInfoUi = arguments.linkedAppInfo?.let {
        PackageInfoUi(packageName = it.packageName, appName = it.appName)
    }
    return InitialCreateLoginUiState(
        username = username,
        password = password,
        url = arguments.website,
        title = arguments.title,
        packageInfoUi = packageInfoUi
    )
}
