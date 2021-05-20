package br.com.zup.pix.consulta

import br.com.zup.*
import br.com.zup.KeymanagerConsultaGrpcServiceGrpc.*
import br.com.zup.compartilhado.ErrorHandler
import br.com.zup.pix.BcbClient
import br.com.zup.pix.ChavePixRepository
import br.com.zup.pix.toModel
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Validator

@Singleton
@ErrorHandler
class ConsultaPixEndpoint(
    @Inject val repository: ChavePixRepository,
    @Inject val bcbClient: BcbClient,
    @Inject val validator: Validator
) : KeymanagerConsultaGrpcServiceImplBase() {

    override fun consulta(
        request: ConsultaPixRequest,
        responseObserver: StreamObserver<ConsultaPixResponse>,
    ) {

        val consulta: Consulta = request.toModel(validator)
        val chave: ConsultaPixResponse = consulta.consulta(repository = repository, bcbClient = bcbClient)

        responseObserver.onNext(chave)
        responseObserver.onCompleted()
    }
}