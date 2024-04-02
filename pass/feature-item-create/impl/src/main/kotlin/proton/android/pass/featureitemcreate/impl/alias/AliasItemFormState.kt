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

package proton.android.pass.featureitemcreate.impl.alias

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.commonrust.api.AliasPrefixError
import proton.android.pass.commonrust.api.AliasPrefixValidator
import proton.android.pass.log.api.PassLogger

@Parcelize
@Immutable
data class AliasItemFormState(
    val title: String = "",
    val prefix: String = "",
    val note: String = "",
    val mailboxTitle: String = "",
    val aliasOptions: AliasOptionsUiModel = AliasOptionsUiModel(emptyList(), emptyList()),
    val selectedSuffix: AliasSuffixUiModel? = null,
    val mailboxes: List<SelectedAliasMailboxUiModel> = emptyList(),
    val aliasToBeCreated: String? = null
) : Parcelable {

    fun validate(allowEmptyTitle: Boolean, aliasPrefixValidator: AliasPrefixValidator): Set<AliasItemValidationErrors> {
        val mutableSet = mutableSetOf<AliasItemValidationErrors>()

        if (!allowEmptyTitle) {
            if (title.isBlank()) mutableSet.add(AliasItemValidationErrors.BlankTitle)
        }

        aliasPrefixValidator.validate(prefix).onFailure {
            if (it is AliasPrefixError) {
                mutableSet.add(it.toError())
            } else {
                PassLogger.w(TAG, "Error validating alias prefix")
                PassLogger.w(TAG, it)
            }
        }

        if (mailboxes.count { it.selected } == 0) mutableSet.add(AliasItemValidationErrors.NoMailboxes)

        return mutableSet.toSet()
    }

    companion object {
        private const val TAG = "AliasItemFormState"
        const val MAX_PREFIX_LENGTH: Int = 40

        fun default(title: Option<String>): AliasItemFormState = when (title) {
            None -> AliasItemFormState()
            is Some -> AliasItemFormState(
                title = title.value,
                prefix = AliasUtils.formatAlias(title.value)
            )
        }
    }
}

sealed interface AliasItemValidationErrors {
    data object BlankTitle : AliasItemValidationErrors
    data object BlankPrefix : AliasItemValidationErrors
    data object InvalidAliasContent : AliasItemValidationErrors
    data object NoMailboxes : AliasItemValidationErrors
}

fun AliasPrefixError.toError(): AliasItemValidationErrors = when (this) {
    AliasPrefixError.DotAtTheBeginning -> AliasItemValidationErrors.InvalidAliasContent
    AliasPrefixError.DotAtTheEnd -> AliasItemValidationErrors.InvalidAliasContent
    AliasPrefixError.InvalidCharacter -> AliasItemValidationErrors.InvalidAliasContent
    AliasPrefixError.PrefixEmpty -> AliasItemValidationErrors.BlankPrefix
    AliasPrefixError.PrefixTooLong -> AliasItemValidationErrors.InvalidAliasContent
    AliasPrefixError.TwoConsecutiveDots -> AliasItemValidationErrors.InvalidAliasContent
}
