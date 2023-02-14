package proton.android.pass.data.impl.crypto

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import org.apache.commons.codec.binary.Base64
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import javax.inject.Inject

interface ReencryptShareContents {
    operator fun invoke(contents: String?, key: EncryptionKey): EncryptedByteArray?
}

class ReencryptShareContentsImpl @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider
) : ReencryptShareContents {
    override fun invoke(contents: String?, key: EncryptionKey): EncryptedByteArray? {
        if (contents == null) return null

        val decoded = Base64.decodeBase64(contents)
        val decrypted = encryptionContextProvider.withEncryptionContext(key) {
            decrypt(EncryptedByteArray(decoded))
        }

        // Using our default key
        return encryptionContextProvider.withEncryptionContext { encrypt(decrypted) }
    }
}
