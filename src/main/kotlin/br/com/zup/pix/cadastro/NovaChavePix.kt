package br.com.zup.pix.cadastro

import br.com.zup.pix.ChavePix
import br.com.zup.pix.conta.Conta
import br.com.zup.pix.enums.TipoDeChave
import br.com.zup.pix.enums.TipoDeConta
import br.com.zup.pix.validacao.UUID
import io.micronaut.core.annotation.Introspected
import io.micronaut.validation.validator.constraints.PatternValidator
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

@Introspected
@ChavePixValida
data class NovaChavePix(

    @field:UUID
    @field:NotBlank
    val idTitular: String?,

    @field:NotNull
    val tipoDeChave: TipoDeChave?,

    @field:Size(max = 77)
    val valor: String?,

    @field:NotNull
    val tipoDeConta: TipoDeConta?

) {

    fun toModel(conta: Conta): ChavePix {
        checkNotNull(idTitular)
        checkNotNull(tipoDeChave)
        checkNotNull(valor)
        return ChavePix(idTitular, tipoDeChave, valor, conta)
    }

}