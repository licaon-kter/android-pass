package proton.android.pass.data.impl.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MigrateItemsResponse(
    @SerialName("Code")
    val code: Int,
    @SerialName("Items")
    val items: List<ItemRevision>
)
