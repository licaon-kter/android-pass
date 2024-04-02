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

package proton.android.pass.features.upsell.shared.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.collections.immutable.ImmutableList
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.container.RoundedCornersColumn
import proton.android.pass.composecomponents.impl.form.PassDivider

@Composable
internal fun UpsellFeatures(
    modifier: Modifier = Modifier,
    features: ImmutableList<UpsellFeatureModel>
) {
    RoundedCornersColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.medium),
    ) {
        features.forEachIndexed { index, feature ->
            if (index > 0) {
                PassDivider()
            }

            UpsellFeatureRow(feature = feature)
        }
    }
}

@Immutable
internal data class UpsellFeatureModel(
    @DrawableRes internal val iconResId: Int,
    internal val iconColor: Color,
    @StringRes internal val textResId: Int
)
