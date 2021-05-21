package br.com.zup.pix.listagem

import br.com.zup.KeymanagerListaGrpcServiceGrpc.KeymanagerListaGrpcServiceImplBase
import br.com.zup.ListaPixRequest
import br.com.zup.ListaPixResponse
import br.com.zup.TipoDeChave
import br.com.zup.TipoDeConta
import br.com.zup.compartilhado.ErrorHandler
import br.com.zup.pix.ChavePix
import br.com.zup.pix.ChavePixRepository
import br.com.zup.pix.validacao.UUID
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import io.micronaut.validation.Validated
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Singleton
@ErrorHandler
class ListaPixEndpoint(
    @Inject val repository: ChavePixRepository,
) : KeymanagerListaGrpcServiceImplBase() {

    override fun lista(
        request: ListaPixRequest,
        responseObserver: StreamObserver<ListaPixResponse>,
    ) {

        //val valida: (@NotBlank String) -> (String) = {id -> id}
        val clienteId = validaRequest(request.clienteId)

        val lista = repository.findByIdTitular(clienteId = clienteId)
            .map { chavePix -> converter(chavePix) }.toList()

        responseObserver.onNext(ListaPixResponse
            .newBuilder()
            .addAllChaves(lista)
            .build())
        responseObserver.onCompleted()

    }

    fun validaRequest(@UUID @NotBlank clienteId: String) = clienteId

    private fun converter(chavePix: ChavePix): ListaPixResponse.itemChave =
        with(chavePix) {
            ListaPixResponse.itemChave
                .newBuilder()
                .setPixId(id)
                .setClienteId(idTitular)
                .setTipoDeChave(TipoDeChave.valueOf(tipoDeChave.name))
                .setValor(valor)
                .setTipoDeConta(TipoDeConta.valueOf(conta.tipo.name))
                .setCriadaEm(Timestamp.newBuilder()
                    .setSeconds(criadaEm.atZone(ZoneId.of("UTC")).toInstant().epochSecond)
                    .setNanos(criadaEm.atZone(ZoneId.of("UTC")).toInstant().nano)
                )
                .build()
        }

}