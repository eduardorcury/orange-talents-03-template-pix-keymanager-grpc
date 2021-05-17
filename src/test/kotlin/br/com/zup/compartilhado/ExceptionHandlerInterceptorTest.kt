package br.com.zup.compartilhado

import io.grpc.BindableService
import io.grpc.Status
import io.grpc.StatusException
import io.grpc.stub.StreamObserver
import io.micronaut.aop.MethodInvocationContext
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class ExceptionHandlerInterceptorTest {

    @Mock
    lateinit var context: MethodInvocationContext<BindableService, Any?>

    val interceptor = ExceptionHandlerInterceptor(resolver = ExceptionHandlerResolver(emptyList()))

    @Test
    internal fun `deve resolver a excecao pelo DefaultExceptionHandler`(
        @Mock streamObserver: StreamObserver<*>
    ) {

        `when`(context.proceed()).thenThrow(RuntimeException("Exceção teste"))
        `when`(context.parameterValues).thenReturn(arrayOf(null, streamObserver))
        interceptor.intercept(context)
        verify(streamObserver).onError(notNull())

    }

}