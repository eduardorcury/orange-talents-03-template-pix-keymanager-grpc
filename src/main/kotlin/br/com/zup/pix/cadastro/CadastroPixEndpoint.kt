package br.com.zup.pix.cadastro

import br.com.zup.CadastroPixRequest
import br.com.zup.CadastroPixResponse
import br.com.zup.KeymanagerCadastraGrpcServiceGrpc
import br.com.zup.KeymanagerCadastraGrpcServiceGrpc.*
import br.com.zup.compartilhado.ErrorHandler
import br.com.zup.pix.ChavePix
import br.com.zup.pix.converter
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ErrorHandler
class CadastroPixEndpoint(@Inject val service: CadastroPixService)
    : KeymanagerCadastraGrpcServiceImplBase() {

    private val LOGGER = LoggerFactory.getLogger(this::class.java)

    override fun cadastro(request: CadastroPixRequest, responseObserver: StreamObserver<CadastroPixResponse>) {

        val chaveCadastrada: ChavePix = service.cadastrar(request.converter())

        responseObserver.onNext(CadastroPixResponse
            .newBuilder()
            .setPixId(chaveCadastrada.id)
            .build())
        responseObserver.onCompleted()

    }

}