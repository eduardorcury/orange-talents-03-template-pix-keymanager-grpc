package br.com.zup.pix.conta

import br.com.zup.pix.enums.TipoDeConta
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank

@Introspected
@JsonDeserialize(using = DadosContaDeserializer::class)
data class DadosContaResponse(
    @NotBlank val tipoDeConta: TipoDeConta?,
    @NotBlank val instituicao: String?,
    @NotBlank val agencia: String?,
    @NotBlank val numero: String?,
    @NotBlank val titular: String?,
) {

    fun toModel(): Conta {
        checkNotNull(tipoDeConta)
        checkNotNull(instituicao)
        checkNotNull(agencia)
        checkNotNull(numero)
        checkNotNull(titular)
        return Conta(tipoDeConta, instituicao, agencia, numero, titular)
    }

}
