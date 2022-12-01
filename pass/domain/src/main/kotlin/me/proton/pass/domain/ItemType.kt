package me.proton.pass.domain

import androidx.annotation.StringRes
import me.proton.core.crypto.common.keystore.EncryptedString

const val ITEM_TYPE_LOGIN = 0
const val ITEM_TYPE_ALIAS = 1
const val ITEM_TYPE_NOTE = 2
const val ITEM_TYPE_PASSWORD = 3

sealed interface ItemType {

    data class Login(
        val username: String,
        val password: EncryptedString,
        val websites: List<String>
    ) : ItemType

    data class Note(val text: String) : ItemType
    data class Alias(val aliasEmail: String) : ItemType
    object Password : ItemType

    @Suppress("MagicNumber")
    fun toWeightedInt(): Int = when (this) {
        is Login -> ITEM_TYPE_LOGIN
        is Alias -> ITEM_TYPE_ALIAS
        is Note -> ITEM_TYPE_NOTE
        is Password -> ITEM_TYPE_PASSWORD
    }

    @StringRes
    fun toStringRes(): Int = when (this) {
        is Login -> R.string.item_type_login
        is Note -> R.string.item_type_note
        is Password -> R.string.item_type_password
        is Alias -> R.string.item_type_alias
    }

    companion object // Needed for being able to define static extension functions
}
