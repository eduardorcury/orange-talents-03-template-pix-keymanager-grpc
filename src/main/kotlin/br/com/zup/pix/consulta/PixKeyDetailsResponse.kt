package br.com.zup.pix.consulta

import br.com.zup.ConsultaExternaResponse
import br.com.zup.Conta
import br.com.zup.Titular
import br.com.zup.pix.cadastro.BankAccount
import br.com.zup.pix.cadastro.Owner
import br.com.zup.pix.enums.TipoDeChave
import br.com.zup.pix.enums.TipoDeConta
import com.google.protobuf.Timestamp
import java.time.LocalDateTime
import java.time.ZoneOffset

data class PixKeyDetailsResponse(
    val keyType: String,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
) {

    fun toResponse(): ConsultaExternaResponse = ConsultaExternaResponse
        .newBuilder()
        .setTipoDeChave(br.com.zup.TipoDeChave.valueOf(TipoDeChave.fromBcb(this.keyType).name))
        .setValor(this.key)
        .setTitular(Titular.newBuilder()
            .setNome(this.owner.name)
            .setCpf(this.owner.taxIdNumber))
        .setConta(Conta.newBuilder()
            .setTipo(br.com.zup.TipoDeConta.valueOf(TipoDeConta.fromBcb(this.bankAccount.accountType).name))
            .setAgencia(this.bankAccount.branch)
            .setNumero(this.bankAccount.accountNumber)
            .setInstituicao("ITAÚ"))
        .setCriadaEm(Timestamp.newBuilder()
            .setSeconds(this.createdAt.toInstant(ZoneOffset.of("Z")).epochSecond)
            .setNanos(this.createdAt.toInstant(ZoneOffset.of("Z")).nano)
        )
        .build()
}
