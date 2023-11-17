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

package proton.android.pass.data.impl.usecases

import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.extension.primary
import me.proton.core.user.domain.repository.UserAddressRepository
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.PendingEventList
import proton.android.pass.data.api.errors.ShareNotAvailableError
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.repositories.ItemSyncStatus
import proton.android.pass.data.api.repositories.ItemSyncStatusRepository
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.repositories.SyncMode
import proton.android.pass.data.api.usecases.ApplyPendingEvents
import proton.android.pass.data.api.usecases.CreateVault
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.impl.extensions.toDomain
import proton.android.pass.data.impl.extensions.toPendingEvent
import proton.android.pass.data.impl.repositories.EventRepository
import proton.android.pass.data.impl.responses.EventList
import proton.android.pass.data.impl.work.FetchItemsWorker
import proton.android.pass.log.api.PassLogger
import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareIcon
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.entity.NewVault
import javax.inject.Inject

class ApplyPendingEventsImpl @Inject constructor(
    private val eventRepository: EventRepository,
    private val addressRepository: UserAddressRepository,
    private val itemRepository: ItemRepository,
    private val observeCurrentUser: ObserveCurrentUser,
    private val shareRepository: ShareRepository,
    private val createVault: CreateVault,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val workManager: WorkManager,
    private val itemSyncStatusRepository: ItemSyncStatusRepository
) : ApplyPendingEvents {

    override suspend fun invoke() {
        withContext(Dispatchers.IO) {
            val user = observeCurrentUser().first()
            val address = requireNotNull(addressRepository.getAddresses(user.userId).primary())
            val refreshSharesResult = shareRepository.refreshShares(user.userId)
            if (refreshSharesResult.allShareIds.isEmpty()) {
                PassLogger.d(TAG, "Received an empty list of shares, creating default vault")
                itemSyncStatusRepository.setMode(SyncMode.Background)
                createDefaultVault(user.userId)
                itemSyncStatusRepository.emit(ItemSyncStatus.CompletedSyncing(false))
            } else {
                enqueueRefreshItems(refreshSharesResult.newShareIds)
                refreshSharesResult.allShareIds.subtract(refreshSharesResult.newShareIds)
                    .map { share ->
                        async {
                            try {
                                applyPendingEvents(address.addressId, user.userId, share)
                            } catch (e: ShareNotAvailableError) {
                                PassLogger.d(TAG, e, "Deleting share not available")
                                shareRepository.deleteVault(user.userId, share)
                            }
                        }
                    }
                    .awaitAll()
            }
        }
    }

    private suspend fun createDefaultVault(userId: UserId) {
        val vault = encryptionContextProvider.withEncryptionContext {
            NewVault(
                name = encrypt("Personal"),
                description = encrypt("Personal vault"),
                icon = ShareIcon.Icon1,
                color = ShareColor.Color1
            )
        }
        runCatching {
            createVault(userId, vault)
        }.onSuccess {
            PassLogger.d(TAG, "Default vault created")
        }.onFailure {
            PassLogger.d(TAG, it, "Error creating default vault")
        }
    }

    private suspend fun applyPendingEvents(
        addressId: AddressId,
        userId: UserId,
        shareId: ShareId
    ) {
        while (true) {
            val events = eventRepository.getEvents(userId, addressId, shareId)
            if (events.shareResponse != null) {
                shareRepository.applyUpdateShareEvent(
                    userId = userId,
                    shareId = shareId,
                    event = events.shareResponse.toDomain()
                )
            }

            PassLogger.d(TAG, "Applying events with share id: $shareId")
            itemRepository.applyEvents(
                userId = userId,
                addressId = addressId,
                shareId = shareId,
                events = events.toDomain()
            )
            PassLogger.d(
                TAG,
                "Applied events with share id: $shareId. Storing latest event ID"
            )
            eventRepository.storeLatestEventId(
                userId = userId,
                addressId = addressId,
                shareId = shareId,
                eventId = events.latestEventId
            )

            if (!events.eventsPending) break
        }
    }

    private fun enqueueRefreshItems(shares: Set<ShareId>) {
        if (shares.isNotEmpty()) {
            val request = FetchItemsWorker.getRequestFor(shares.toList())
            workManager.enqueue(request)
        }
    }

    private fun EventList.toDomain(): PendingEventList = PendingEventList(
        updatedItems = updatedItems.map { it.toPendingEvent() },
        deletedItemIds = deletedItemIds
    )

    companion object {
        private const val TAG = "ApplyPendingEventsImpl"
    }
}
