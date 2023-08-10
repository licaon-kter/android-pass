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

package proton.android.pass.featureitemcreate.impl.common

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import proton.pass.domain.CustomFieldContent

@Immutable
@Parcelize
sealed interface UICustomFieldContent : Parcelable {
    val label: String

    @Immutable
    @Parcelize
    data class Text(override val label: String, val value: String) : UICustomFieldContent

    @Immutable
    @Parcelize
    data class Hidden(override val label: String, val value: UIHiddenState) : UICustomFieldContent

    @Immutable
    @Parcelize
    data class Totp(override val label: String, val value: UIHiddenState) : UICustomFieldContent

    fun toCustomFieldContent() = when (this) {
        is Text -> CustomFieldContent.Text(label, value)
        is Hidden -> CustomFieldContent.Hidden(label, value.toHiddenState())
        is Totp -> CustomFieldContent.Totp(label, value.toHiddenState())
    }

    companion object {
        fun from(state: CustomFieldContent) = when (state) {
            is CustomFieldContent.Text -> Text(state.label, state.value)
            is CustomFieldContent.Hidden -> Hidden(state.label, UIHiddenState.from(state.value))
            is CustomFieldContent.Totp -> Totp(state.label, UIHiddenState.from(state.value))
        }
    }
}
