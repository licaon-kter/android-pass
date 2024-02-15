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

package proton.android.pass.composecomponents.impl.item.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.item.details.sections.PassItemDetailSections
import proton.android.pass.composecomponents.impl.item.details.rows.PassItemDetailTitleRow
import proton.android.pass.composecomponents.impl.utils.ProtonItemColors

@Composable
fun PassItemDetailsContent(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit,
    itemUiModel: ItemUiModel,
    itemColors: ProtonItemColors,
) {
    Scaffold(
        modifier = modifier,
        topBar = { topBar() },
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(paddingValues = innerPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PassItemDetailTitleRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = Spacing.medium,
                        vertical = Spacing.small,
                    ),
                itemUiModel = itemUiModel,
                itemColors = itemColors,
            )

            PassItemDetailSections(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.medium),
                itemUiModel = itemUiModel,
                itemColors = itemColors,
            )
        }
    }
}
