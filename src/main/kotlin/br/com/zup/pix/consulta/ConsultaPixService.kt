package br.com.zup.pix.consulta

import br.com.zup.ConsultaExternaResponse
import br.com.zup.ConsultaInternaResponse
import br.com.zup.Conta
import br.com.zup.Titular
import br.com.zup.compartilhado.exceptions.PermissaoNegadaException
import br.com.zup.compartilhado.exceptions.RecursoNaoEncontradoException
import br.com.zup.pix.BcbClient
import br.com.zup.pix.ChavePix
import br.com.zup.pix.ChavePixRepository
import br.com.zup.pix.enums.TipoDeChave
import br.com.zup.pix.enums.TipoDeConta
import com.google.protobuf.Timestamp
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.validation.Validated
import java.lang.IllegalStateException
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Singleton
@Validated
class ConsultaPixService(
    @Inject val repository: ChavePixRepository,
    @Inject val bcbClient: BcbClient
) {

    fun consultaInterna(@NotBlank pixId: String, @NotBlank clienteId: String): ConsultaInternaResponse {

        val possivelChave = repository.findById(pixId)

        if (possivelChave.isEmpty) {
            throw RecursoNaoEncontradoException("Chave de id $pixId não encontrada no sistema")
        }

        val chave = possivelChave.get()
        consultaBcb(valorChave = chave.valor)

        return if (chave.idTitular == clienteId) ConsultaInternaResponse.getDefaultInstance().fromModel(chave)
               else throw PermissaoNegadaException("Chave de id $pixId não pertence ao cliente de id $clienteId")

    }

    fun consultaExterna(@NotBlank @Size(max = 77) valorChave: String): ConsultaExternaResponse {

        val chave = repository.findByValor(valor = valorChave)
        val httpResponse = consultaBcb(valorChave = valorChave)

        return if (chave != null) {
            ConsultaExternaResponse.getDefaultInstance().fromModel(chavePix = chave)
        } else {
            val detailsResponse: PixKeyDetailsResponse = httpResponse.body()
                ?: throw IllegalStateException("Sistema BCB respondeu com status diferente de 404 sem corpo")
            detailsResponse.toResponse()
        }

    }

    private fun consultaBcb(valorChave: String): HttpResponse<PixKeyDetailsResponse?> {
        val httpResponse = bcbClient.consulta(valorChave = valorChave)
        if (httpResponse.status == HttpStatus.NOT_FOUND) {
            throw RecursoNaoEncontradoException("Chave de valor $valorChave não consta no sistema BCB")
        }
        return httpResponse
    }

    fun ConsultaInternaResponse.fromModel(chavePix: ChavePix) = ConsultaInternaResponse
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
            .setSeconds(chavePix.criadaEm.toInstant(ZoneOffset.of("Z")).epochSecond)
            .setNanos(chavePix.criadaEm.toInstant(ZoneOffset.of("Z")).nano)
        )
        .build()

    fun ConsultaExternaResponse.fromModel(chavePix: ChavePix) = ConsultaExternaResponse
        .newBuilder()
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
            .setSeconds(chavePix.criadaEm.toInstant(ZoneOffset.of("Z")).epochSecond)
            .setNanos(chavePix.criadaEm.toInstant(ZoneOffset.of("Z")).nano)
        )
        .build()

}