package br.com.zup.pix.cadastro

import br.com.zup.CadastroPixRequest
import br.com.zup.KeymanagerCadastraGrpcServiceGrpc.KeymanagerCadastraGrpcServiceBlockingStub
import br.com.zup.KeymanagerCadastraGrpcServiceGrpc.newBlockingStub
import br.com.zup.pix.ChavePixRepository
import br.com.zup.pix.SistemaErpClient
import br.com.zup.pix.conta.DadosContaResponse
import br.com.zup.pix.enums.TipoDeChave
import br.com.zup.pix.enums.TipoDeConta
import io.grpc.ManagedChannel
import io.grpc.Status.*
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class CadastroPixEndpointTest(
    private val repository: ChavePixRepository,
    private val grcpClient: KeymanagerCadastraGrpcServiceBlockingStub,
) {

    @field:Inject
    lateinit var erpClient: SistemaErpClient

    @BeforeEach
    internal fun setUp() {
        repository.deleteAll()
    }

    @Test
    internal fun `deve salvar nova chave no banco de dados`() {

        val request = request()
        val dadosConta = dadosDaConta()

        `when`(erpClient.retornaDadosCliente(
            request.idTitular!!,
            request.tipoDeConta!!.name)
        ).thenReturn(HttpResponse.ok(dadosConta))

        val response = grcpClient.cadastro(request)
        assertNotNull(response.pixId)

        val chaves = repository.findAll()
        assertTrue(chaves.isNotEmpty())
        with(chaves[0]) {
            assertTrue(idTitular == request.idTitular)
            assertTrue(tipoDeChave == TipoDeChave.valueOf(request.tipoDeChave.name))
            assertTrue(valor == request.valor)
            assertTrue(conta.tipo == TipoDeConta.valueOf(request.tipoDeConta.name))
            assertTrue(conta.tipo == dadosConta.tipoDeConta)
            assertTrue(conta.instituicao == dadosConta.instituicao)
            assertTrue(conta.agencia == dadosConta.agencia)
            assertTrue(conta.numero == dadosConta.numero)
            assertTrue(conta.titular == dadosConta.titular)
        }
    }

    @Test
    internal fun `deve salvar chave do tipo aleatoria`() {

        val request: CadastroPixRequest = CadastroPixRequest
            .newBuilder()
            .setTipoDeConta(br.com.zup.TipoDeConta.CONTA_CORRENTE)
            .setTipoDeChave(br.com.zup.TipoDeChave.ALEATORIA)
            .setIdTitular(UUID.randomUUID().toString())
            .build()
        val dadosConta = dadosDaConta()

        `when`(erpClient.retornaDadosCliente(
            request.idTitular!!,
            request.tipoDeConta!!.name)
        ).thenReturn(HttpResponse.ok(dadosConta))

        val response = grcpClient.cadastro(request)
        assertNotNull(response.pixId)

        val chaves = repository.findAll()
        assertTrue(chaves.isNotEmpty())
        with(chaves[0].valor) {
            isNotBlank()
            matches(Regex("^[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}$"))
        }

    }

    @Test
    internal fun `deve retornar erros de validacao`() {

        val error = assertThrows<StatusRuntimeException> {
            grcpClient.cadastro(CadastroPixRequest.newBuilder().build())
        }

        with(error) {
            assertEquals(INVALID_ARGUMENT.code, status.code)
        }

    }

    @Test
    internal fun `deve retornar ALREADY_EXISTS para chave ja existente`() {

        val request = request()
        val dadosConta = dadosDaConta()

        `when`(erpClient.retornaDadosCliente(
            request.idTitular!!,
            request.tipoDeConta!!.name)
        ).thenReturn(HttpResponse.ok(dadosConta))

        grcpClient.cadastro(request)

        val erro = assertThrows<StatusRuntimeException> { grcpClient.cadastro(request) }
        with(erro) {
            assertEquals(ALREADY_EXISTS.code, status.code)
            assertEquals("Chave Pix ${request.valor} já existente", status.description)
        }

    }

    @Test
    internal fun `deve retornar NOT_FOUND para cliente nao encontrado`() {

        val request = request()

        `when`(erpClient.retornaDadosCliente(
            request.idTitular!!,
            request.tipoDeConta!!.name)
        ).thenReturn(HttpResponse.ok())

        val erro = assertThrows<StatusRuntimeException> { grcpClient.cadastro(request) }
        with(erro) {
            assertEquals(NOT_FOUND.code, status.code)
            assertEquals("Cliente não existe ou não possui conta do tipo ${request.tipoDeConta}",
                status.description)
        }

    }

    @Test
    internal fun `deve retornar INTERNAL para erro no client ERP`() {

        val request = request()

        `when`(erpClient.retornaDadosCliente(
            request.idTitular!!,
            request.tipoDeConta!!.name)
        ).thenThrow(HttpClientException("mensagem de erro"))

        val erro = assertThrows<StatusRuntimeException> { grcpClient.cadastro(request) }
        with(erro) {
            assertEquals(INTERNAL.code, status.code)
            assertEquals("mensagem de erro", status.description)
        }

    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeymanagerCadastraGrpcServiceBlockingStub =
            newBlockingStub(channel)
    }

    @MockBean(SistemaErpClient::class)
    fun erpClient() = mock(SistemaErpClient::class.java)

    private fun request() =
        CadastroPixRequest
            .newBuilder()
            .setIdTitular(UUID.randomUUID().toString())
            .setTipoDeChave(br.com.zup.TipoDeChave.EMAIL)
            .setValor("email@gmail.com")
            .setTipoDeConta(br.com.zup.TipoDeConta.CONTA_CORRENTE)
            .build()

    private fun dadosDaConta() =
        DadosContaResponse(
            tipoDeConta = TipoDeConta.valueOf(request().tipoDeConta.name),
            instituicao = "ITAÚ",
            agencia = "0001",
            numero = "12345",
            titular = "Eduardo"
        )
}