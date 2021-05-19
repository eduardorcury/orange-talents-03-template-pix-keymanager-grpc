package br.com.zup.pix.consulta

import br.com.zup.ConsultaExternaRequest
import br.com.zup.ConsultaExternaResponse
import br.com.zup.ConsultaInternaRequest
import br.com.zup.ConsultaInternaResponse
import br.com.zup.KeymanagerConsultaGrpcServiceGrpc.*
import br.com.zup.compartilhado.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ErrorHandler
class ConsultaPixEndpoint(
    @Inject val service: ConsultaPixService
) : KeymanagerConsultaGrpcServiceImplBase() {

    override fun consultaInterna(
        request: ConsultaInternaRequest,
        responseObserver: StreamObserver<ConsultaInternaResponse>,
    ) {
        val consulta = service.consultaInterna(pixId = request.pixId, clienteId = request.clienteId)
        responseObserver.onNext(consulta)
        responseObserver.onCompleted()
    }

    override fun consultaExterna(
        request: ConsultaExternaRequest,
        responseObserver: StreamObserver<ConsultaExternaResponse>,
    ) {
        val consulta = service.consultaExterna(valorChave = request.chave)
        responseObserver.onNext(consulta)
        responseObserver.onCompleted()
    }
}