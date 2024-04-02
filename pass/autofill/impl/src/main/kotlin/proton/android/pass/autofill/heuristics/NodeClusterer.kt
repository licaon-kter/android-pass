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

package proton.android.pass.autofill.heuristics

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import proton.android.pass.autofill.entities.AssistField
import proton.android.pass.autofill.entities.AutofillFieldId
import proton.android.pass.autofill.entities.FieldType

@Parcelize
sealed interface NodeCluster : Parcelable {

    fun isFocused(): Boolean
    fun fields(): List<AssistField>
    fun type(): String

    fun url(): String? = fields().firstOrNull()?.url

    @Parcelize
    data object Empty : NodeCluster {
        override fun isFocused() = true
        override fun fields(): List<AssistField> = emptyList()
        override fun type() = "NodeCluster.Empty"
    }

    @Parcelize
    sealed interface Login : NodeCluster {

        override fun isFocused() = fields().any { it.isFocused }

        @Parcelize
        @JvmInline
        value class OnlyUsername(val username: AssistField) : Login {
            override fun fields(): List<AssistField> = listOf(username)
            override fun type() = "OnlyUsername"
        }

        @Parcelize
        @JvmInline
        value class OnlyPassword(val password: AssistField) : Login {
            override fun fields(): List<AssistField> = listOf(password)
            override fun type() = "OnlyPassword"
        }

        @Parcelize
        data class UsernameAndPassword(
            val username: AssistField,
            val password: AssistField
        ) : Login {
            override fun fields(): List<AssistField> = listOf(username, password)
            override fun type() = "UsernameAndPassword"
        }
    }

    @Parcelize
    data class SignUp(
        val username: AssistField,
        val password: AssistField,
        val repeatPassword: AssistField
    ) : NodeCluster {
        override fun isFocused() = fields().any { it.isFocused }
        override fun fields(): List<AssistField> = listOf(username, password, repeatPassword)
        override fun type() = "SignUp"
    }

    @Parcelize
    data class CreditCard(
        val cardNumber: AssistField,
        val cardHolder: CardHolder?,
        val cvv: AssistField?,
        val expiration: Expiration?
    ) : NodeCluster {

        sealed interface CardHolder : Parcelable {
            fun fields(): List<AssistField>

            @JvmInline
            @Parcelize
            value class SingleField(val field: AssistField) : CardHolder {
                override fun fields() = listOf(field)
            }

            @Parcelize
            data class FirstNameLastName(
                val firstName: AssistField,
                val lastName: AssistField
            ) : CardHolder {
                override fun fields() = listOf(firstName, lastName)
            }
        }

        @Parcelize
        sealed interface Expiration : Parcelable {

            fun fields(): List<AssistField>

            @JvmInline
            @Parcelize
            value class MmYySameField(val field: AssistField) : Expiration {
                override fun fields() = listOf(field)
            }

            @Parcelize
            data class MmYyDifferentfields(
                val month: AssistField,
                val year: AssistField
            ) : Expiration {
                override fun fields() = listOf(month, year)
            }

            @Parcelize
            data class MmYyyyDifferentfields(
                val month: AssistField,
                val year: AssistField
            ) : Expiration {
                override fun fields() = listOf(month, year)
            }
        }

        override fun isFocused() = fields().any { it.isFocused }
        override fun fields(): List<AssistField> {
            val fields = mutableListOf(cardNumber)
            cardHolder?.let { holder -> holder.fields().forEach { field -> fields.add(field) } }
            cvv?.let { fields.add(it) }
            expiration?.let { fields.addAll(it.fields()) }
            return fields
        }

        override fun type() = "CreditCard"
    }
}

fun List<NodeCluster>.focused(): NodeCluster {
    if (isEmpty()) return NodeCluster.Empty
    return firstOrNull { it.isFocused() } ?: first()
}

interface IdentifiableNode {
    val nodeId: AutofillFieldId?
    val parentPath: List<AutofillFieldId?>
}

object NodeClusterer {

    fun cluster(nodes: List<AssistField>): List<NodeCluster> {
        val clusters = mutableListOf<NodeCluster>()
        val addedNodes = mutableSetOf<AssistField>()

        clusterLogins(nodes, clusters, addedNodes)
        clusterCreditCards(nodes, clusters, addedNodes)

        return clusters
    }

    private fun clusterLogins(
        nodes: List<AssistField>,
        clusters: MutableList<NodeCluster>,
        addedNodes: MutableSet<AssistField>
    ) {
        detectSignupForm(nodes, clusters, addedNodes)
        detectCardNumberAsUsername(nodes, clusters, addedNodes)

        val usernameFields = nodes.getUsernames(addedNodes)
        val passwordFields = nodes.getNodesForType(FieldType.Password, addedNodes)

        for (usernameField in usernameFields) {
            val candidatePasswordFields = passwordFields.filter { !addedNodes.contains(it) }
            val nearestPasswordField = HeuristicsUtils.findNearestNodeByParentId(
                currentField = usernameField,
                fields = candidatePasswordFields
            )

            if (nearestPasswordField != null) {
                val remainingUsernames = usernameFields.filter { !addedNodes.contains(it) }
                val nearestUsernameToPassword = HeuristicsUtils.findNearestNodeByParentId(
                    currentField = nearestPasswordField,
                    fields = remainingUsernames
                )

                if (nearestUsernameToPassword == usernameField) {
                    clusters.add(
                        NodeCluster.Login.UsernameAndPassword(
                            username = usernameField,
                            password = nearestPasswordField
                        )
                    )
                    addedNodes.add(nearestPasswordField)
                } else {
                    clusters.add(NodeCluster.Login.OnlyUsername(usernameField))
                }
            } else {
                clusters.add(NodeCluster.Login.OnlyUsername(usernameField))
            }
            addedNodes.add(usernameField)
        }

        val remainingUsernameFields = usernameFields.filter { !addedNodes.contains(it) }
        val remainingPasswordFields = passwordFields.filter { !addedNodes.contains(it) }

        remainingUsernameFields.forEach { usernameField ->
            if (usernameField !in addedNodes) {
                clusters.add(NodeCluster.Login.OnlyUsername(usernameField))
                addedNodes.add(usernameField)
            }
        }

        remainingPasswordFields.forEach { passwordField ->
            if (passwordField !in addedNodes) {
                clusters.add(NodeCluster.Login.OnlyPassword(passwordField))
                addedNodes.add(passwordField)
            }
        }
    }

    @Suppress("ComplexCondition")
    private fun detectCardNumberAsUsername(
        nodes: List<AssistField>,
        clusters: MutableList<NodeCluster>,
        addedNodes: MutableSet<AssistField>
    ) {
        val usernameFields = nodes.getUsernames(addedNodes)
        val passwordFields = nodes.getNodesForType(FieldType.Password, addedNodes)
        if (
            usernameFields.isEmpty() &&
            passwordFields.size == 1 &&
            nodes.size == 2 &&
            nodes.count { it.type == FieldType.CardNumber } == 1
        ) {
            clusters.add(
                NodeCluster.Login.UsernameAndPassword(
                    username = nodes.first { it.type == FieldType.CardNumber }.also {
                        addedNodes.add(it)
                    },
                    password = passwordFields.first().also {
                        addedNodes.add(it)
                    }
                )
            )
        }
    }

    private fun detectSignupForm(
        nodes: List<AssistField>,
        clusters: MutableList<NodeCluster>,
        addedNodes: MutableSet<AssistField>
    ) {
        val usernameFields = nodes.getUsernames(addedNodes)
        val passwordFields = nodes.getNodesForType(FieldType.Password, addedNodes)
        if (usernameFields.size == 1 && passwordFields.size == 2) {
            val username = usernameFields.first()
            val password = passwordFields.first()
            val repeatPassword = passwordFields.last()
            clusters.add(NodeCluster.SignUp(username, password, repeatPassword))
            addedNodes.add(username)
            addedNodes.add(password)
            addedNodes.add(repeatPassword)
        }
    }

    private fun clusterCreditCards(
        nodes: List<AssistField>,
        clusters: MutableList<NodeCluster>,
        addedNodes: MutableSet<AssistField>
    ) {
        val cardNumberNode = nodes.getNodeOfType(FieldType.CardNumber, addedNodes) ?: return
        val cardCvvNode = nodes.getNodeOfType(FieldType.CardCvv, addedNodes)
        val cardHolder = getCardHolder(nodes, addedNodes)
        val expiration = getExpiration(nodes, addedNodes)

        val cluster = NodeCluster.CreditCard(
            cardNumber = cardNumberNode.also { addedNodes.add(it) },
            cardHolder = cardHolder,
            cvv = cardCvvNode?.also { addedNodes.add(it) },
            expiration = expiration
        )

        clusters.add(cluster)
    }

    @Suppress("ComplexMethod", "CyclomaticComplexMethod")
    private fun getCardHolder(
        nodes: List<AssistField>,
        addedNodes: MutableSet<AssistField>
    ): NodeCluster.CreditCard.CardHolder? {
        val cardHolderNode = nodes.getNodeOfType(FieldType.CardholderName, addedNodes)
        val cardHolderFirstNameNode = nodes.getNodeOfType(FieldType.CardholderFirstName, addedNodes)
        val cardHolderLastNameNode = nodes.getNodeOfType(FieldType.CardholderLastName, addedNodes)

        return when {
            cardHolderLastNameNode != null -> when {
                cardHolderFirstNameNode != null -> NodeCluster.CreditCard.CardHolder.FirstNameLastName(
                    firstName = cardHolderFirstNameNode.also { addedNodes.add(it) },
                    lastName = cardHolderLastNameNode.also { addedNodes.add(it) }
                )

                cardHolderNode != null -> NodeCluster.CreditCard.CardHolder.FirstNameLastName(
                    firstName = cardHolderNode.also { addedNodes.add(it) },
                    lastName = cardHolderLastNameNode.also { addedNodes.add(it) }
                )

                else -> NodeCluster.CreditCard.CardHolder.SingleField(
                    field = cardHolderLastNameNode.also { addedNodes.add(it) }
                )
            }

            cardHolderFirstNameNode != null -> when {
                cardHolderNode != null -> NodeCluster.CreditCard.CardHolder.FirstNameLastName(
                    firstName = cardHolderFirstNameNode.also { addedNodes.add(it) },
                    lastName = cardHolderNode.also { addedNodes.add(it) }
                )

                else -> NodeCluster.CreditCard.CardHolder.SingleField(
                    field = cardHolderFirstNameNode.also { addedNodes.add(it) }
                )
            }

            // At this point we know cardHolderFirstName and cardHolderLastName are null
            cardHolderNode != null -> NodeCluster.CreditCard.CardHolder.SingleField(
                field = cardHolderNode.also { addedNodes.add(it) }
            )

            else -> null
        }
    }

    private fun getExpiration(
        nodes: List<AssistField>,
        addedNodes: MutableSet<AssistField>
    ): NodeCluster.CreditCard.Expiration? {
        val expirationMMNode = nodes.firstOrNull { it.type == FieldType.CardExpirationMM }
        val expirationMMYYNode = nodes
            .firstOrNull { it.type == FieldType.CardExpirationMMYY }

        val expirationYYNode = nodes.firstOrNull { it.type == FieldType.CardExpirationYY }
        val expirationYYYYNode = nodes
            .firstOrNull { it.type == FieldType.CardExpirationYYYY }

        return when {
            expirationMMYYNode != null -> {
                addedNodes.add(expirationMMYYNode)
                NodeCluster.CreditCard.Expiration.MmYySameField(expirationMMYYNode)
            }

            expirationMMNode != null && expirationYYNode != null -> {
                addedNodes.add(expirationMMNode)
                addedNodes.add(expirationYYNode)
                NodeCluster.CreditCard.Expiration.MmYyDifferentfields(
                    month = expirationMMNode,
                    year = expirationYYNode
                )
            }

            expirationMMNode != null && expirationYYYYNode != null -> {
                addedNodes.add(expirationMMNode)
                addedNodes.add(expirationYYYYNode)
                NodeCluster.CreditCard.Expiration.MmYyyyDifferentfields(
                    month = expirationMMNode,
                    year = expirationYYYYNode
                )
            }

            else -> null
        }
    }

    private fun List<AssistField>.getUsernames(addedNodes: Set<AssistField>): List<AssistField> = filter {
        !addedNodes.contains(it) && (it.type == FieldType.Username || it.type == FieldType.Email)
    }

    private fun List<AssistField>.getNodesForType(type: FieldType, addedNodes: Set<AssistField>): List<AssistField> =
        filter {
            !addedNodes.contains(it) && it.type == type
        }

    private fun List<AssistField>.getNodeOfType(type: FieldType, addedNodes: Set<AssistField>): AssistField? =
        getNodesForType(type, addedNodes).firstOrNull()
}
