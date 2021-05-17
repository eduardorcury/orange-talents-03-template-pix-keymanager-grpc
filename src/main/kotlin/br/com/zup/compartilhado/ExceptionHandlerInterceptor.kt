package br.com.zup.compartilhado

import io.grpc.BindableService
import io.grpc.protobuf.StatusProto
import io.grpc.stub.StreamObserver
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@InterceptorBean(ErrorHandler::class)
class ExceptionHandlerInterceptor(
    @Inject val resolver: ExceptionHandlerResolver
) : MethodInterceptor<BindableService, Any?> {

    private val LOGGER = LoggerFactory.getLogger(this::class.java)

    override fun intercept(context: MethodInvocationContext<BindableService, Any?>): Any? {

        try {
            return context.proceed()
        } catch (e: Exception) {
            resolver.resolve(e)
                .handle(e)
                .let { status ->  StatusProto.toStatusException(status) }
                .run { GrpcEndpointArguments(context).response().onError(this) }
                .also { LOGGER.error("""
                    Handling the exception ${e.javaClass.name} 
                    while processing the call: ${context.targetMethod}
                    """.trimIndent()) }
            return null
        }

    }

    private class GrpcEndpointArguments(val context : MethodInvocationContext<BindableService, Any?>) {
        fun response(): StreamObserver<*> {
            return context.parameterValues[1] as StreamObserver<*>
        }
    }

}