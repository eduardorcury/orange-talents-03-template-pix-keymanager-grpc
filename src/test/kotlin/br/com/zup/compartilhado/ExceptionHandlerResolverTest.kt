package br.com.zup.compartilhado

import br.com.zup.compartilhado.handlers.DefaultExceptionHandler
import br.com.zup.compartilhado.handlers.ExceptionHandler
import br.com.zup.compartilhado.handlers.HttpClientExceptionHandler
import com.google.rpc.Code
import com.google.rpc.Status
import io.micronaut.http.client.exceptions.HttpClientException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ExceptionHandlerResolverTest {

    @Test
    internal fun `deve usar o defaul handler para excecao sem handler`() {

        val defaultHandler = DefaultExceptionHandler()
        val resolver = ExceptionHandlerResolver(emptyList(), defaultHandler)
        val handler = resolver.resolve(RuntimeException("exceção de teste"))
        assertEquals(handler, defaultHandler)

    }

    @Test
    internal fun `deve encontrar o handler apropriado para a excecao`() {

        val handler = HttpClientExceptionHandler()
        val resolver = listOf(handler as ExceptionHandler<Exception>)
            .let { handlers -> ExceptionHandlerResolver(handlers, DefaultExceptionHandler()) }

        val foundHandler = resolver.resolve(HttpClientException("exceção de teste"))
        assertEquals(handler, foundHandler)

    }

    @Test
    internal fun `deve retornar erro para mais de um handler encontrado`() {

        val resolver = listOf(HttpClientExceptionHandler() as ExceptionHandler<Exception>,
            novoExceptionHandler as ExceptionHandler<Exception>)
            .let { handlers -> ExceptionHandlerResolver(handlers, DefaultExceptionHandler()) }

        assertThrows<IllegalStateException> { resolver.resolve(HttpClientException("exceção de teste")) }

    }

    private val novoExceptionHandler: ExceptionHandler<HttpClientException> = object : ExceptionHandler<HttpClientException> {
        override fun handle(exception: HttpClientException) = Status.newBuilder().setCode(Code.UNKNOWN_VALUE).build()
        override fun supports(exception: Exception) = exception is HttpClientException
    }
}