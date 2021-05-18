package br.com.zup.pix.deleta

import br.com.zup.DeletaPixRequest
import br.com.zup.KeymanagerDeletaGrpcServiceGrpc.KeymanagerDeletaGrpcServiceImplBase
import br.com.zup.compartilhado.ErrorHandler
import br.com.zup.pix.converter
import com.google.protobuf.Empty
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ErrorHandler
class DeletaPixEndpoint(@Inject val service: DeletaPixService)
    : KeymanagerDeletaGrpcServiceImplBase() {

    override fun deleta(request: DeletaPixRequest, responseObserver: StreamObserver<Empty>) {

        service.deleta(request = request.converter())
        responseObserver.onNext(Empty.newBuilder().build())
        responseObserver.onCompleted()

    }

}