package br.com.zup.pix.consulta

import br.com.zup.ConsultaPixResponse
import br.com.zup.Conta
import br.com.zup.Titular
import br.com.zup.compartilhado.exceptions.PermissaoNegadaException
import br.com.zup.compartilhado.exceptions.RecursoNaoEncontradoException
import br.com.zup.pix.BcbClient
import br.com.zup.pix.ChavePix
import br.com.zup.pix.ChavePixRepository
import br.com.zup.pix.validacao.UUID
import com.google.protobuf.Timestamp
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import java.lang.IllegalStateException
import java.time.ZoneId
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
sealed class Consulta() {

    abstract fun consulta(repository: ChavePixRepository, bcbClient: BcbClient): ConsultaPixResponse

    @Introspected
    data class ConsultaInterna(
        @field:NotBlank @UUID val pixId: String,
        @field:NotBlank @UUID val clienteId: String,
    ) : Consulta() {

        override fun consulta(repository: ChavePixRepository, bcbClient: BcbClient): ConsultaPixResponse {

            val possivelChave: Optional<ChavePix> = repository.findById(pixId)

            return if (possivelChave.isPresent) {
                consultaBcb(valorChave = possivelChave.get().valor, bcbClient = bcbClient)
                if (possivelChave.get().idTitular == clienteId)
                    criaConsultaResponse(possivelChave.get())
                    else throw PermissaoNegadaException("Chave de id $pixId não pertence ao cliente de id $clienteId")
            } else {
                throw RecursoNaoEncontradoException("Chave de id $pixId não encontrada no sistema")
            }

        }

    }

    @Introspected
    data class ConsultaPorChave(
        @field:NotBlank @Size(max = 77) val chave: String
    ) : Consulta() {

        override fun consulta(repository: ChavePixRepository, bcbClient: BcbClient): ConsultaPixResponse {

            val possivelChave: Optional<ChavePix> = repository.findByValor(chave)
            return if (possivelChave.isPresent) {
                criaConsultaResponse(possivelChave.get())
            } else {
                val httpResponse = consultaBcb(valorChave = chave, bcbClient = bcbClient)
                val detailsResponse: PixKeyDetailsResponse = httpResponse.body()
                    ?: throw IllegalStateException("Sistema BCB respondeu com status diferente de 404 sem corpo")
                detailsResponse.toResponse()
            }
        }

    }

    fun consultaBcb(valorChave: String, bcbClient: BcbClient): HttpResponse<PixKeyDetailsResponse?> {
        val httpResponse = bcbClient.consulta(valorChave = valorChave)
        if (httpResponse.status == HttpStatus.NOT_FOUND) {
            throw RecursoNaoEncontradoException("Chave de valor $valorChave não consta no sistema BCB")
        }
        return httpResponse
    }

    fun criaConsultaResponse(chavePix: ChavePix) = ConsultaPixResponse
        .newBuilder()
        .setClienteId(chavePix.idTitular)
        .setPixId(chavePix.id)
        .setTipoDeChave(br.com.zup.TipoDeChave.valueOf(chavePix.tipoDeChave.name))
        .setValor(chavePix.valor)
        .setTitular(Titular.newBuilder()
            .setNome(chavePix.conta.titular)
            .setCpf(chavePix.conta.cpf))
        .setConta(Conta.newBuilder()
            .setTipo(br.com.zup.TipoDeConta.valueOf(chavePix.conta.tipo.name))
            .setAgencia(chavePix.conta.agencia)
            .setNumero(chavePix.conta.numero)
            .setInstituicao("ITAÚ"))
        .setCriadaEm(Timestamp.newBuilder()
            .setSeconds(chavePix.criadaEm.atZone(ZoneId.of("UTC")).toInstant().epochSecond)
            .setNanos(chavePix.criadaEm.atZone(ZoneId.of("UTC")).toInstant().nano)
        )
        .build()

}
