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

package proton.android.pass.autofill.extensions

import android.os.Build
import android.os.Bundle
import android.service.autofill.FillResponse
import android.service.autofill.SaveInfo
import proton.android.pass.autofill.entities.AssistField
import proton.android.pass.autofill.entities.asAndroid
import proton.android.pass.autofill.extensions.MultiStepUtils.addPasswordToState
import proton.android.pass.autofill.extensions.MultiStepUtils.addUsernameToState
import proton.android.pass.autofill.extensions.MultiStepUtils.getUsernameFromState
import proton.android.pass.autofill.heuristics.NodeCluster
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.log.api.PassLogger

const val TAG = "FillResponseExt"

sealed interface SaveSessionType {
    @JvmInline
    value class Username(val field: AssistField) : SaveSessionType

    data class Password(
        val storedUsername: Option<MultiStepUtils.SaveFieldInfo>,
        val passwordField: AssistField
    ) : SaveSessionType

    data class UsernameAndPassword(
        val usernameField: AssistField,
        val passwordField: AssistField
    ) : SaveSessionType

    data object NotAutoSaveable : SaveSessionType
}

internal fun FillResponse.Builder.addSaveInfo(
    autofillSessionId: Int,
    cluster: NodeCluster,
    currentClientState: Bundle,
    isBrowser: Boolean
) {
    val saveSessionType = getSaveSessionType(cluster, currentClientState, isBrowser)
    val saveInfo = when (saveSessionType) {
        SaveSessionType.NotAutoSaveable -> return
        is SaveSessionType.UsernameAndPassword -> {
            PassLogger.d(TAG, "UsernameAndPassword session")

            val usernameFieldId = saveSessionType.usernameField.id.asAndroid().autofillId
            val passwordFieldId = saveSessionType.passwordField.id.asAndroid().autofillId
            val ids = listOf(usernameFieldId, passwordFieldId)
            currentClientState.addUsernameToState(autofillSessionId, usernameFieldId)
            currentClientState.addPasswordToState(autofillSessionId, passwordFieldId)
            setClientState(currentClientState)
            SaveInfo.Builder(
                SaveInfo.SAVE_DATA_TYPE_USERNAME or SaveInfo.SAVE_DATA_TYPE_PASSWORD,
                ids.toTypedArray()
            ).setFlags(SaveInfo.FLAG_SAVE_ON_ALL_VIEWS_INVISIBLE)
        }

        is SaveSessionType.Username -> {
            PassLogger.d(TAG, "Only username session")

            val usernameFieldId = saveSessionType.field.id.asAndroid().autofillId
            val ids = listOf(usernameFieldId)
            currentClientState.addUsernameToState(autofillSessionId, usernameFieldId)
            setClientState(currentClientState)

            val builder = SaveInfo.Builder(
                SaveInfo.SAVE_DATA_TYPE_USERNAME or SaveInfo.SAVE_DATA_TYPE_EMAIL_ADDRESS,
                ids.toTypedArray()
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PassLogger.d(TAG, "Adding FLAG_DELAY_SAVE")
                builder.setFlags(SaveInfo.FLAG_DELAY_SAVE)
            }
            builder
        }

        is SaveSessionType.Password -> {
            PassLogger.d(TAG, "Only password session")

            val passwordFieldId = saveSessionType.passwordField.id.asAndroid().autofillId
            val ids = mutableListOf(passwordFieldId)
            saveSessionType.storedUsername.map { usernameInfo ->
                ids.add(usernameInfo.fieldId)
                currentClientState.addUsernameToState(usernameInfo.sessionId, usernameInfo.fieldId)
                PassLogger.d(TAG, "Also added username to state")
            }
            currentClientState.addPasswordToState(autofillSessionId, passwordFieldId)

            // Determine which flags to send to SaveInfo. These flags are used for the system
            // message that prompts the user to save their credentials.
            val saveInfoType = if (saveSessionType.storedUsername.isEmpty()) {
                SaveInfo.SAVE_DATA_TYPE_PASSWORD
            } else {
                SaveInfo.SAVE_DATA_TYPE_USERNAME or SaveInfo.SAVE_DATA_TYPE_PASSWORD
            }

            setClientState(currentClientState)
            SaveInfo.Builder(
                saveInfoType,
                ids.toTypedArray()
            ).setFlags(SaveInfo.FLAG_SAVE_ON_ALL_VIEWS_INVISIBLE)
        }
    }

    setSaveInfo(saveInfo.build())
}

private fun getSaveSessionType(
    cluster: NodeCluster,
    currentClientState: Bundle,
    isBrowser: Boolean
): SaveSessionType = when (cluster) {
    NodeCluster.Empty -> SaveSessionType.NotAutoSaveable
    is NodeCluster.CreditCard -> SaveSessionType.NotAutoSaveable
    is NodeCluster.SignUp -> {
        SaveSessionType.UsernameAndPassword(
            usernameField = cluster.username,
            passwordField = cluster.password
        )
    }

    is NodeCluster.Login -> when (cluster) {
        is NodeCluster.Login.OnlyPassword -> if (isBrowser) {
            PassLogger.d(TAG, "Not adding saveInfo because is only password and is browser")
            SaveSessionType.NotAutoSaveable
        } else {
            val usernameField = currentClientState.getUsernameFromState().toOption()
            SaveSessionType.Password(
                storedUsername = usernameField,
                passwordField = cluster.password
            )
        }

        is NodeCluster.Login.OnlyUsername -> if (isBrowser) {
            PassLogger.d(TAG, "Not adding saveInfo because is only username and is browser")
            SaveSessionType.NotAutoSaveable
        } else {
            SaveSessionType.Username(cluster.username)
        }

        is NodeCluster.Login.UsernameAndPassword -> {
            SaveSessionType.UsernameAndPassword(
                usernameField = cluster.username,
                passwordField = cluster.password
            )
        }
    }
}
