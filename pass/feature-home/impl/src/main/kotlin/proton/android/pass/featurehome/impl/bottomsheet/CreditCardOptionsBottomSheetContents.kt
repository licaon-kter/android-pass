package proton.android.pass.featurehome.impl.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.Clock
import me.proton.core.crypto.common.keystore.EncryptedString
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemRow
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemSubtitle
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.composecomponents.impl.item.icon.CreditCardIcon
import proton.android.pass.featurehome.impl.R
import proton.pass.domain.CreditCardType
import proton.pass.domain.HiddenState
import proton.pass.domain.ItemContents
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

@ExperimentalMaterialApi
@Composable
fun CreditCardOptionsBottomSheetContents(
    modifier: Modifier = Modifier,
    itemUiModel: ItemUiModel,
    isRecentSearch: Boolean = false,
    onCopyNumber: (String) -> Unit,
    onCopyPin: (EncryptedString) -> Unit,
    onCopyCvv: (EncryptedString) -> Unit,
    onEdit: (ShareId, ItemId) -> Unit,
    onMoveToTrash: (ItemUiModel) -> Unit,
    onRemoveFromRecentSearch: (ShareId, ItemId) -> Unit
) {
    val contents = itemUiModel.contents as ItemContents.CreditCard
    Column(modifier.bottomSheet()) {
        BottomSheetItemRow(
            title = { BottomSheetItemTitle(text = contents.title) },
            subtitle = {
                BottomSheetItemSubtitle(text = contents.cardHolder)
            },
            leftIcon = { CreditCardIcon() }
        )
        val list = mutableListOf(
            copyNumber { onCopyNumber(contents.number) },
            copyPin { onCopyPin(contents.pin.encrypted) },
            copyCvv { onCopyCvv(contents.cvv.encrypted) },
            edit(itemUiModel, onEdit),
            moveToTrash(itemUiModel, onMoveToTrash)
        )
        if (isRecentSearch) {
            list.add(removeFromRecentSearch(itemUiModel, onRemoveFromRecentSearch))
        }
        BottomSheetItemList(
            items = list.withDividers().toPersistentList()
        )
    }
}

@Composable
private fun copyNumber(onClick: () -> Unit) = copyItem(
    stringResource(id = R.string.bottomsheet_copy_number),
    onClick
)

@Composable
private fun copyPin(onClick: () -> Unit) = copyItem(
    stringResource(id = R.string.bottomsheet_copy_pin),
    onClick
)

@Composable
private fun copyCvv(onClick: () -> Unit) = copyItem(
    stringResource(id = R.string.bottomsheet_copy_cvv),
    onClick
)

private fun copyItem(text: String, onClick: () -> Unit): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = { BottomSheetItemTitle(text = text) }
    override val subtitle: (@Composable () -> Unit)?
        get() = null
    override val leftIcon: (@Composable () -> Unit)
        get() = { BottomSheetItemIcon(iconId = R.drawable.ic_squares) }
    override val endIcon: (@Composable () -> Unit)?
        get() = null
    override val onClick = onClick
    override val isDivider = false
}


@Suppress("FunctionMaxLength")
@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun CreditCardOptionsBottomSheetContentsPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    PassTheme(isDark = input.first) {
        Surface {
            CreditCardOptionsBottomSheetContents(
                itemUiModel = ItemUiModel(
                    id = ItemId(id = ""),
                    shareId = ShareId(id = ""),
                    contents = ItemContents.CreditCard(
                        title = "A credit card",
                        note = "Credit card note",
                        cardHolder = "Card holder",
                        type = CreditCardType.Visa,
                        number = "1234123412341234",
                        cvv = HiddenState.Concealed(""),
                        pin = HiddenState.Concealed(""),
                        expirationDate = "2030-01"

                    ),
                    state = 0,
                    createTime = Clock.System.now(),
                    modificationTime = Clock.System.now(),
                    lastAutofillTime = Clock.System.now()
                ),
                isRecentSearch = input.second,
                onCopyNumber = {},
                onCopyPin = {},
                onCopyCvv = {},
                onEdit = { _, _ -> },
                onMoveToTrash = {},
                onRemoveFromRecentSearch = { _, _ -> }
            )
        }
    }
}

