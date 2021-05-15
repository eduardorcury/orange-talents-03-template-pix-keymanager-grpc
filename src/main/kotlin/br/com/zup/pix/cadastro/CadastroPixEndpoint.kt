package br.com.zup.pix.cadastro

import br.com.zup.CadastroPixRequest
import br.com.zup.CadastroPixResponse
import br.com.zup.KeymanagerGrpcServiceGrpc
import br.com.zup.pix.ChavePix
import br.com.zup.pix.ChavePixRepository
import br.com.zup.pix.converter
import br.com.zup.pix.enums.TipoDeChave
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CadastroPixEndpoint(@Inject val service: CadastroPixService)
    : KeymanagerGrpcServiceGrpc.KeymanagerGrpcServiceImplBase() {

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