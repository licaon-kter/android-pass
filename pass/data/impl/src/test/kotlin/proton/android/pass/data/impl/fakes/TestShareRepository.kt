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

package proton.android.pass.data.impl.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.FlowUtils.testFlow
import proton.android.pass.data.api.repositories.RefreshSharesResult
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.repositories.UpdateShareEvent
import proton.pass.domain.Share
import proton.pass.domain.ShareId
import proton.pass.domain.entity.NewVault

class TestShareRepository : ShareRepository {

    private var createVaultResult: Result<Share> =
        Result.failure(IllegalStateException("CreateVaultResult not set"))
    private var refreshSharesResult: RefreshSharesResult =
        RefreshSharesResult(emptySet(), emptySet())
    private val observeSharesFlow = testFlow<Result<List<Share>>>()
    private val observeVaultCountFlow = testFlow<Result<Int>>()

    private var deleteVault: Result<Unit> =
        Result.failure(IllegalStateException("DeleteVaultResult not set"))
    private var getByIdResult: Result<Share> =
        Result.failure(IllegalStateException("GetByIdResult not set"))

    private var updateVaultResult: Result<Share> =
        Result.failure(IllegalStateException("UpdateVaultResult not set"))

    private var markAsPrimaryResult: Result<Share> =
        Result.failure(IllegalStateException("MarkAsPrimaryResult not set"))

    private var deleteSharesResult: Result<Unit> = Result.success(Unit)

    private val deleteVaultMemory: MutableList<ShareId> = mutableListOf()
    fun deleteVaultMemory(): List<ShareId> = deleteVaultMemory

    fun setCreateVaultResult(result: Result<Share>) {
        createVaultResult = result
    }

    fun setRefreshSharesResult(result: RefreshSharesResult) {
        refreshSharesResult = result
    }

    fun setDeleteVaultResult(result: Result<Unit>) {
        deleteVault = result
    }

    fun setGetByIdResult(result: Result<Share>) {
        getByIdResult = result
    }

    fun emitObserveShares(value: Result<List<Share>>) {
        observeSharesFlow.tryEmit(value)
    }

    fun emitObserveShareCount(value: Result<Int>) {
        observeVaultCountFlow.tryEmit(value)
    }

    fun setUpdateVaultResult(value: Result<Share>) {
        updateVaultResult = value
    }

    fun setMarkAsPrimaryResult(value: Result<Share>) {
        markAsPrimaryResult = value
    }

    fun setDeleteSharesResult(value: Result<Unit>) {
        deleteSharesResult = value
    }

    override suspend fun createVault(userId: SessionUserId, vault: NewVault): Share =
        createVaultResult.getOrThrow()

    override suspend fun deleteVault(userId: UserId, shareId: ShareId) {
        deleteVaultMemory.add(shareId)
        deleteVault.getOrThrow()
    }

    override suspend fun refreshShares(userId: UserId): RefreshSharesResult =
        refreshSharesResult

    override fun observeAllShares(userId: SessionUserId): Flow<List<Share>> =
        observeSharesFlow.map { it.getOrThrow() }

    override fun observeVaultCount(userId: UserId): Flow<Int> =
        observeVaultCountFlow.map { it.getOrThrow() }

    override suspend fun getById(userId: UserId, shareId: ShareId): Share =
        getByIdResult.getOrThrow()

    override suspend fun updateVault(userId: UserId, shareId: ShareId, vault: NewVault): Share =
        updateVaultResult.getOrThrow()

    override suspend fun markAsPrimary(userId: UserId, shareId: ShareId): Share =
        markAsPrimaryResult.getOrThrow()

    override suspend fun deleteSharesForUser(userId: UserId) = deleteSharesResult.getOrThrow()

    override suspend fun leaveVault(userId: UserId, shareId: ShareId) {

    }

    override suspend fun applyUpdateShareEvent(
        userId: UserId,
        shareId: ShareId,
        event: UpdateShareEvent
    ) {

    }
}
