package br.com.zup.pix.cadastro

import br.com.zup.pix.ChavePix
import br.com.zup.pix.conta.Conta
import br.com.zup.pix.deleta.DeletaChaveData
import br.com.zup.pix.enums.TipoDeChave
import br.com.zup.pix.enums.TipoDeConta
import java.time.LocalDateTime

const val PARTICIPANT: String = "60701190"

data class CreatePixKeyRequest(
    val chavePix: NovaChavePix,
    val conta: Conta,
) {
    val keyType: String = chavePix.tipoDeChave!!.nomeBCB
    val key: String? = if (this.keyType != "RANDOM") chavePix.valor else null
    val bankAccount: BankAccount = BankAccount(conta)
    val owner: Owner = Owner(name = conta.titular,
        taxIdNumber = conta.cpf)
}

data class BankAccount(
    val participant: String = PARTICIPANT,
    val branch: String,
    val accountNumber: String,
    val accountType: String,
) {
    constructor(conta: Conta) : this(
        participant = "60701190",
        branch = conta.agencia,
        accountNumber = conta.numero,
        accountType = conta.tipo.nomeBCB
    )
}

data class Owner(
    val type: String = "NATURAL_PERSON",
    val name: String,
    val taxIdNumber: String,
)

data class CreatePixKeyResponse(
    val keyType: String,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime,
) {

    fun toModel(idTitular: String): ChavePix {
        return ChavePix(
            idTitular = idTitular,
            tipoDeChave = TipoDeChave.fromBcb(this.keyType),
            valor = this.key,
            conta = Conta(
                tipo = TipoDeConta.fromBcb(this.bankAccount.accountType),
                instituicao = "ITAÚ",
                agencia = this.bankAccount.branch,
                numero = this.bankAccount.accountNumber,
                titular = this.owner.name,
                cpf = this.owner.taxIdNumber
            )
        )
    }

    fun dadosConsistentes(novaChave: NovaChavePix, conta: Conta): Boolean =
        this.keyType == novaChave.tipoDeChave!!.nomeBCB &&
                this.bankAccount.accountType == novaChave.tipoDeConta!!.nomeBCB &&
                this.bankAccount.accountNumber == conta.numero &&
                this.bankAccount.branch == conta.agencia &&
                this.owner.name == conta.titular &&
                this.owner.taxIdNumber == conta.cpf

}

data class DeletePixKeyRequest(
    val chavePix: ChavePix
) {
    val key: String = chavePix.valor
    val participant: String = PARTICIPANT
}

data class DeletePixKeyResponse(
    val key: String,
    val participant: String = PARTICIPANT,
    val deletedAt: LocalDateTime
)
