package br.com.zup.pix.deleta

import br.com.zup.DeletaPixRequest
import br.com.zup.KeymanagerDeletaGrpcServiceGrpc.KeymanagerDeletaGrpcServiceBlockingStub
import br.com.zup.KeymanagerDeletaGrpcServiceGrpc.newBlockingStub
import br.com.zup.pix.ChavePix
import br.com.zup.pix.ChavePixRepository
import br.com.zup.pix.conta.Conta
import br.com.zup.pix.enums.TipoDeChave
import br.com.zup.pix.enums.TipoDeConta
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.UUID.randomUUID
import java.util.stream.Stream
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class DeletaPixEndpointTest(
    private val repository: ChavePixRepository,
    private val grpcClient: KeymanagerDeletaGrpcServiceBlockingStub,
) {

    @BeforeEach
    internal fun setUp() {
        repository.deleteAll()
    }

    @Test
    internal fun `deve deletar chave pix`() {

        val request = repository.save(criaChave())
            .also { assertTrue(repository.findAll().size == 1) }
            .let { chavePix ->
                criaRequest(
                    pixId = chavePix.id!!,
                    clienteId = chavePix.idTitular
                )
            }

        grpcClient.deleta(request)
        assertTrue(repository.findAll().isEmpty())

    }

    @Test
    internal fun `deve retornar NOT_FOUND para chave inexistente`() {

        val request = criaChave()
            .let { chavePix ->
                criaRequest(
                    pixId = randomUUID().toString(),
                    clienteId = chavePix.idTitular
                )
            }
            .also { assertTrue(repository.findAll().isEmpty()) }

        val erro = assertThrows<StatusRuntimeException> { grpcClient.deleta(request) }
        with(erro) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix de id ${request.pixId} não encontrada", status.description)
        }

    }

    @Test
    internal fun `deve retornar PERMISSION_DENIED se chave nao pertencer ao usuario`() {

        val request = repository.save(criaChave())
            .also { assertTrue(repository.findAll().size == 1) }
            .let { chavePix ->
                criaRequest(
                    pixId = chavePix.id!!,
                    clienteId = randomUUID().toString()
                )
            }

        val erro = assertThrows<StatusRuntimeException> { grpcClient.deleta(request) }
        with(erro) {
            assertEquals(Status.PERMISSION_DENIED.code, status.code)
            assertEquals("Chave Pix de id ${request.pixId} " +
                    "não pertence ao usuário de id ${request.clienteId}", status.description)
        }

    }

    @ParameterizedTest
    @MethodSource("geraDadosInvalidos")
    internal fun `testa validacao do request`(pixId: String, clienteId: String) {

        val request = DeletaPixRequest.newBuilder()
            .setPixId(pixId)
            .setClienteId(clienteId)
            .build()

        val erro = assertThrows<StatusRuntimeException> { grpcClient.deleta(request) }
        with(erro) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }

    }

    @Factory
    class Cliente() {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel) =
            newBlockingStub(channel)
    }

    private fun criaChave() =
        ChavePix(
            idTitular = randomUUID().toString(),
            tipoDeChave = TipoDeChave.ALEATORIA,
            valor = randomUUID().toString(),
            conta = Conta(
                tipo = TipoDeConta.CONTA_CORRENTE,
                instituicao = "ITAÚ",
                agencia = "0001",
                numero = "12345",
                titular = "Eduardo"
            )
        )

    private fun criaRequest(pixId: String, clienteId: String) =
        DeletaPixRequest
            .newBuilder()
            .setPixId(pixId)
            .setClienteId(clienteId)
            .build()

    companion object {
        @JvmStatic
        private fun geraDadosInvalidos(): Stream<Arguments> =
            Stream.of(
                Arguments.of("", randomUUID().toString()),
                Arguments.of(randomUUID().toString(), ""),
                Arguments.of("12345", "12345"),
                Arguments.of("12345", randomUUID().toString()),
                Arguments.of(randomUUID().toString(), "12345"),
            )
    }

}