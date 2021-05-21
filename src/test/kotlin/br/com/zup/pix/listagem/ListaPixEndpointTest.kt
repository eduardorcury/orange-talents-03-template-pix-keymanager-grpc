package br.com.zup.pix.listagem

import br.com.zup.KeymanagerListaGrpcServiceGrpc
import br.com.zup.KeymanagerListaGrpcServiceGrpc.*
import br.com.zup.ListaPixRequest
import br.com.zup.ListaPixResponse
import br.com.zup.pix.ChavePix
import br.com.zup.pix.ChavePixRepository
import br.com.zup.pix.conta.Conta
import br.com.zup.pix.enums.TipoDeChave
import br.com.zup.pix.enums.TipoDeConta
import com.google.protobuf.Timestamp
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.ZoneId
import java.util.*
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class ListaPixEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeymanagerListaGrpcServiceBlockingStub,
) {

    @BeforeEach
    internal fun setUp() {
        repository.deleteAll()
    }

    @Test
    internal fun `deve listar as chaves do usuario`() {

        val clienteId = UUID.randomUUID().toString()
        val chave1 = retornaChaveSalva(clienteId)
        val chave2 = retornaChaveSalva(clienteId)

        val request = ListaPixRequest
            .newBuilder()
            .setClienteId(clienteId)
            .build()

        val response = grpcClient.lista(request)

        with(response.chavesList) {
            assertThat(this, hasSize(2))
            assertEquals(clienteId, this[0].clienteId)
            assertEquals(clienteId, this[1].clienteId)
            assertEquals(chave1.id, this[0].pixId)
            assertEquals(chave2.id, this[1].pixId)
        }
    }

    @Test
    internal fun `deve listar apenas as chaves do usuario`() {

        val clienteId1 = UUID.randomUUID().toString()
        val clienteId2 = UUID.randomUUID().toString()
        val chave1 = retornaChaveSalva(clienteId1)
        val chave2 = retornaChaveSalva(clienteId2)

        val request = ListaPixRequest
            .newBuilder()
            .setClienteId(clienteId1)
            .build()

        val response = grpcClient.lista(request)

        with(response.chavesList) {
            assertThat(this, hasSize(1))
            assertEquals(clienteId1, this[0].clienteId)
            assertEquals(chave1.id, this[0].pixId)
        }

    }

    @Test
    internal fun `deve retornar array vazio se cliente nao possuir chaves`() {

        val request = ListaPixRequest
            .newBuilder()
            .setClienteId(UUID.randomUUID().toString())
            .build()

        val response = grpcClient.lista(request)

        with(response) {
            assertThat(chavesList, hasSize(0))
        }

    }

    @ParameterizedTest
    @ValueSource(strings = ["", "  ", "abc"])
    internal fun `deve retornar erros de validacao para request invalido`(clienteId: String) {

        val request = ListaPixRequest
            .newBuilder()
            .setClienteId(clienteId)
            .build()

        val erro = assertThrows<StatusRuntimeException> { grpcClient.lista(request) }

        with(erro) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }

    }

    @Factory
    class Clients {
        @Singleton
        fun grpcStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel) =
            newBlockingStub(channel)
    }

    private fun retornaChaveSalva(clienteId: String) = repository.save(ChavePix(
        idTitular = clienteId,
        tipoDeChave = TipoDeChave.ALEATORIA,
        valor = UUID.randomUUID().toString(),
        conta = Conta(
            tipo = TipoDeConta.CONTA_POUPANCA,
            instituicao = "ITAÚ",
            agencia = "0001",
            numero = "12345",
            titular = "Eduardo",
            cpf = "87821765074"
        )
    ))

    private fun converter(chavePix: ChavePix): ListaPixResponse.itemChave =
        with(chavePix) {
            ListaPixResponse.itemChave
                .newBuilder()
                .setPixId(id)
                .setClienteId(idTitular)
                .setTipoDeChave(br.com.zup.TipoDeChave.valueOf(tipoDeChave.name))
                .setValor(valor)
                .setTipoDeConta(br.com.zup.TipoDeConta.valueOf(conta.tipo.name))
                .setCriadaEm(Timestamp.newBuilder()
                    .setSeconds(criadaEm.atZone(ZoneId.of("UTC")).toInstant().epochSecond)
                    .setNanos(criadaEm.atZone(ZoneId.of("UTC")).toInstant().nano)
                )
                .build()
        }

}