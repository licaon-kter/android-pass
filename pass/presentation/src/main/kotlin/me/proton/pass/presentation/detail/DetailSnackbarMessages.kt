package me.proton.pass.presentation.detail

import androidx.annotation.StringRes
import me.proton.android.pass.notifications.api.SnackbarMessage
import me.proton.android.pass.notifications.api.SnackbarType
import me.proton.pass.presentation.R

enum class DetailSnackbarMessages(
    @StringRes override val id: Int,
    override val type: SnackbarType
) : SnackbarMessage {
    InitError(R.string.detail_init_error, SnackbarType.ERROR),
    SendToTrashError(R.string.detail_send_to_trash_error, SnackbarType.ERROR),
    ItemNotLoadedError(R.string.detail_item_not_loaded_error, SnackbarType.ERROR),
    AliasCopiedToClipboard(R.string.alias_copied_to_clipboard, SnackbarType.NORM),
    NoteCopiedToClipboard(R.string.note_copied_to_clipboard, SnackbarType.NORM)
}
