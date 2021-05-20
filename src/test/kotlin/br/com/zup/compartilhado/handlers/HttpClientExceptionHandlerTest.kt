package br.com.zup.compartilhado.handlers

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.google.rpc.Code
import com.google.rpc.Status
import io.micronaut.http.client.exceptions.HttpClientException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class HttpClientExceptionHandlerTest {

    @Test
    internal fun `deve criar status com violacoes`() {

        val problema = Problem(
            type = "BAD_REQUEST",
            status = "400",
            title = "Bad request",
            detail = "Entry has invalid fields",
            violations = listOf(Violation(
                field = "bankAccount.branch",
                message = "tamanho deve ser entre 4 e 4"
            ))
        )

        val mensagem = XmlMapper().writeValueAsString(problema)

        val handler = HttpClientExceptionHandler()
        val status: Status = handler.handle(HttpClientException(mensagem))

        with(status) {
            assertEquals(Code.INTERNAL_VALUE, code)
            assertEquals("Erro de tipo ${problema.type}, ${problema.status}. ${problema.title}: "
                + "${problema.detail}. ${problema.violations!!.map { "${it.field}: ${it.message}" }}", message)
            assertEquals(problema.violations!!.size, detailsCount)
        }

    }

    @Test
    internal fun `deve criar status sem violacoes`() {

        val problema = Problem(
            type = "BAD_REQUEST",
            status = "400",
            title = "Bad request",
            detail = "Entry has invalid fields",
            violations = null
        )

        val mensagem = XmlMapper().writeValueAsString(problema)

        val handler = HttpClientExceptionHandler()
        val status: Status = handler.handle(HttpClientException(mensagem))

        with(status) {
            assertEquals(Code.INTERNAL_VALUE, code)
            assertEquals("Erro de tipo ${problema.type}, ${problema.status}. " +
                    "${problema.title}: ${problema.detail}.", message)
        }

    }

    @Test
    internal fun `deve criar status com mensagem aleatoria`() {

        val handler = HttpClientExceptionHandler()
        val status: Status = handler.handle(HttpClientException("Mensagem aleatória"))

        with(status) {
            assertEquals(Code.INTERNAL_VALUE, code)
            assertEquals("Mensagem aleatória", message)
        }

    }

}