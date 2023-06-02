package proton.pass.domain

import kotlinx.serialization.Serializable
import me.proton.core.crypto.common.keystore.EncryptedString
import proton.pass.domain.entity.PackageInfo

@Serializable
sealed interface CustomFieldContent {
    val label: String

    @Serializable
    data class Text(override val label: String, val value: String) : CustomFieldContent

    @Serializable
    data class Hidden(override val label: String, val value: HiddenState) : CustomFieldContent

    @Serializable
    data class Totp(override val label: String, val value: HiddenState) : CustomFieldContent
}

@Serializable
sealed class HiddenState {
    abstract val encrypted: EncryptedString

    @Serializable
    data class Empty(override val encrypted: EncryptedString) : HiddenState()

    @Serializable
    data class Concealed(override val encrypted: EncryptedString) : HiddenState()

    @Serializable
    data class Revealed(
        override val encrypted: EncryptedString,
        val clearText: String
    ) : HiddenState()
}

@Serializable
sealed class ItemContents {
    abstract val title: String
    abstract val note: String

    @Serializable
    data class Login(
        override val title: String,
        override val note: String,
        val username: String,
        val password: HiddenState,
        val urls: List<String>,
        val packageInfoSet: Set<PackageInfo>,
        val primaryTotp: HiddenState,
        val customFields: List<CustomFieldContent>
    ) : ItemContents() {
        companion object {
            fun create(
                password: HiddenState,
                primaryTotp: HiddenState
            ) = Login(
                title = "",
                username = "",
                password = password,
                urls = listOf(""),
                packageInfoSet = emptySet(),
                primaryTotp = primaryTotp,
                note = "",
                customFields = emptyList()
            )
        }
    }

    @Serializable
    data class Note(
        override val title: String,
        override val note: String
    ) : ItemContents()

    @Serializable
    data class Alias(
        override val title: String,
        override val note: String,
        val aliasEmail: String
    ) : ItemContents()

    @Serializable
    data class Unknown(
        override val title: String,
        override val note: String
    ) : ItemContents()

}
