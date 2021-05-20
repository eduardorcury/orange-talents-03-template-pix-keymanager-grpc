package br.com.zup.pix.consulta

import br.com.zup.ConsultaPixResponse
import br.com.zup.Conta
import br.com.zup.Titular
import br.com.zup.pix.Instituicao
import br.com.zup.pix.cadastro.BankAccount
import br.com.zup.pix.cadastro.Owner
import br.com.zup.pix.enums.TipoDeChave
import br.com.zup.pix.enums.TipoDeConta
import com.google.protobuf.Timestamp
import java.time.LocalDateTime
import java.time.ZoneId
import br.com.zup.TipoDeChave as ChaveProto
import br.com.zup.TipoDeConta as ContaProto

data class PixKeyDetailsResponse(
    val keyType: String,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
) {

    fun toResponse(): ConsultaPixResponse = ConsultaPixResponse
        .newBuilder()
        .setTipoDeChave(ChaveProto.valueOf(TipoDeChave.fromBcb(this.keyType).name))
        .setValor(this.key)
        .setTitular(Titular.newBuilder()
            .setNome(this.owner.name)
            .setCpf(this.owner.taxIdNumber))
        .setConta(Conta.newBuilder()
            .setTipo(ContaProto.valueOf(TipoDeConta.fromBcb(this.bankAccount.accountType).name))
            .setAgencia(this.bankAccount.branch)
            .setNumero(this.bankAccount.accountNumber)
            .setInstituicao(Instituicao.fromIspb(this.bankAccount.participant)))
        .setCriadaEm(Timestamp.newBuilder()
            .setSeconds(this.createdAt.atZone(ZoneId.of("UTC")).toInstant().epochSecond)
            .setNanos(this.createdAt.atZone(ZoneId.of("UTC")).toInstant().nano)
        )
        .build()
}
