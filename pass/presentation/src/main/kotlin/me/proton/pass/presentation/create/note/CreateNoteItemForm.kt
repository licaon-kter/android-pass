package me.proton.pass.presentation.create.note

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.proton.pass.presentation.components.form.NoteInput
import me.proton.pass.presentation.components.form.TitleInput

@Composable
internal fun CreateNoteItemForm(
    modifier: Modifier = Modifier,
    state: NoteItem,
    enabled: Boolean,
    onTitleRequiredError: Boolean,
    onTitleChange: (String) -> Unit,
    onNoteChange: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        TitleInput(
            enabled = enabled,
            value = state.title,
            onChange = onTitleChange,
            onTitleRequiredError = onTitleRequiredError
        )
        NoteInput(
            contentModifier = Modifier.height(300.dp),
            enabled = enabled,
            value = state.note,
            onChange = onNoteChange
        )
    }
}
