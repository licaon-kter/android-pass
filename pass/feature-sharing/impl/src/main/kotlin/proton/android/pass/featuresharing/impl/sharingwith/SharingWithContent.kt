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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import me.proton.core.compose.theme.subheadlineNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.RequestFocusLaunchedEffect
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.body3Norm
import proton.android.pass.commonui.api.heroNorm
import proton.android.pass.composecomponents.impl.buttons.LoadingCircleButton
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.composecomponents.impl.container.roundedContainer
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.composecomponents.impl.loading.Loading
import proton.android.pass.composecomponents.impl.topbar.BackArrowTopAppBar
import proton.android.pass.featuresharing.impl.R
import proton.android.pass.featuresharing.impl.SharingNavigation
import me.proton.core.presentation.R as CoreR

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SharingWithContent(
    modifier: Modifier = Modifier,
    state: SharingWithUIState,
    editingEmail: String,
    onNavigateEvent: (SharingNavigation) -> Unit,
    onEmailChange: (String) -> Unit,
    onInviteSuggestionToggle: (String, Boolean) -> Unit,
    onEmailSubmit: () -> Unit,
    onContinueClick: () -> Unit,
    onEmailClick: (Int) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            BackArrowTopAppBar(
                title = "",
                onUpClick = { onNavigateEvent(SharingNavigation.Back) },
                actions = {
                    LoadingCircleButton(
                        modifier = Modifier.padding(12.dp, 0.dp),
                        color = PassTheme.colors.interactionNormMajor1,
                        onClick = onContinueClick,
                        isLoading = state.isLoading,
                        text = {
                            Text(
                                text = stringResource(R.string.share_continue),
                                style = PassTheme.typography.body3Norm(),
                                color = PassTheme.colors.textInvert
                            )
                        }
                    )
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.share_with_title),
                style = PassTheme.typography.heroNorm()
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                state.enteredEmails.forEachIndexed { idx, email ->
                    Row(
                        modifier = Modifier
                            .padding(
                                top = Spacing.small,
                                bottom = Spacing.small,
                                end = Spacing.small
                            )
                            .roundedContainer(
                                backgroundColor = PassTheme.colors.interactionNormMinor1,
                                borderColor = Color.Transparent,
                            )
                            .clickable { onEmailClick(idx) }
                            .padding(Spacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = email,
                            style = ProtonTheme.typography.defaultNorm,
                        )

                        if (state.selectedEmailIndex.value() == idx) {
                            Icon(
                                modifier = Modifier.padding(start = Spacing.small),
                                painter = painterResource(id = CoreR.drawable.ic_proton_cross_circle),
                                tint = PassTheme.colors.textNorm,
                                contentDescription = null
                            )
                        }
                    }
                }
            }

            val focusRequester = remember { FocusRequester() }
            ProtonTextField(
                modifier = Modifier.focusRequester(focusRequester),
                value = editingEmail,
                placeholder = {
                    ProtonTextFieldPlaceHolder(
                        text = stringResource(R.string.share_with_email_hint),
                        textStyle = ProtonTheme.typography.subheadlineNorm.copy(color = ProtonTheme.colors.textHint)
                    )
                },
                isError = state.showEmailNotValidError,
                errorMessage = if (state.showEmailNotValidError) {
                    stringResource(R.string.share_with_email_error)
                } else "",
                textStyle = ProtonTheme.typography.subheadlineNorm,
                keyboardOptions = KeyboardOptions.Default.copy(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrect = false,
                    keyboardType = KeyboardType.Email
                ),
                onChange = onEmailChange,
                onDoneClick = onEmailSubmit
            )
            RequestFocusLaunchedEffect(focusRequester)

            if (state.showEditVault && state.vault != null) {
                CustomizeVault(
                    vault = state.vault,
                    onClick = {
                        onNavigateEvent(SharingNavigation.EditVault(shareId = state.vault.shareId))
                    }
                )
            }
            PassDivider()
            when (state.suggestionsUIState) {
                is SuggestionsUIState.Content -> InviteSuggestions(
                    state = state.suggestionsUIState,
                    onItemClicked = onInviteSuggestionToggle
                )

                SuggestionsUIState.Initial -> {}
                SuggestionsUIState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Loading()
                }
            }
        }
    }
}
