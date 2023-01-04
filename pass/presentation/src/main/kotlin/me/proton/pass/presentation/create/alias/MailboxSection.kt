package me.proton.pass.presentation.create.alias

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.android.pass.composecomponents.impl.form.ProtonTextTitle
import me.proton.pass.presentation.R

@Composable
internal fun MailboxSection(
    modifier: Modifier = Modifier,
    contentText: String,
    isEditAllowed: Boolean,
    onMailboxClick: () -> Unit
) {
    Column(modifier = modifier) {
        ProtonTextTitle(stringResource(id = R.string.field_mailboxes_title))
        MailboxSelector(
            modifier = Modifier.padding(top = 8.dp),
            contentText = contentText,
            isEditAllowed = isEditAllowed,
            onClick = onMailboxClick
        )
    }
}
