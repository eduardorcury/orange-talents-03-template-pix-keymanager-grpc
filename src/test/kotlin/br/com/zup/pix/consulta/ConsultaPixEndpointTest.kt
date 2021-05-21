package br.com.zup.pix.consulta

import br.com.zup.ConsultaPixRequest
import br.com.zup.KeymanagerConsultaGrpcServiceGrpc.KeymanagerConsultaGrpcServiceBlockingStub
import br.com.zup.KeymanagerConsultaGrpcServiceGrpc.newBlockingStub
import br.com.zup.pix.BcbClient
import br.com.zup.pix.ChavePix
import br.com.zup.pix.ChavePixRepository
import br.com.zup.pix.Instituicao
import br.com.zup.pix.cadastro.BankAccount
import br.com.zup.pix.cadastro.Owner
import br.com.zup.pix.conta.Conta
import br.com.zup.pix.enums.TipoDeChave
import br.com.zup.pix.enums.TipoDeConta
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class ConsultaPixEndpointTest(
    private val repository: ChavePixRepository,
    private val grpcClient: KeymanagerConsultaGrpcServiceBlockingStub
) {

    @field:Inject
    lateinit var bcbClient: BcbClient

    @BeforeEach
    internal fun setUp() {
        repository.deleteAll()
    }

    @Test
    internal fun `deve consultar chave salva no sistema com pix ID e cliente ID`() {

        val chavePix = retornaChaveSalva()

        `when`(bcbClient.consulta(chavePix.valor))
            .thenReturn(HttpResponse.ok())

        val response = grpcClient.consulta(ConsultaPixRequest
            .newBuilder()
            .setInterna(ConsultaPixRequest.ConsultaInterna
                .newBuilder()
                .setPixId(chavePix.id)
                .setClienteId(chavePix.idTitular)
                .build())
            .build())

        with(response) {
            assertEquals(chavePix.id, pixId)
            assertEquals(chavePix.idTitular, clienteId)
            assertEquals(chavePix.tipoDeChave.name, tipoDeChave.name)
            assertEquals(chavePix.valor, valor)
            assertEquals(chavePix.conta.titular, titular.nome)
            assertEquals(chavePix.conta.cpf, titular.cpf)
            assertEquals(chavePix.conta.agencia, conta.agencia)
            assertEquals(chavePix.conta.numero, conta.numero)
            assertEquals(chavePix.conta.instituicao, conta.instituicao)
            assertEquals(chavePix.conta.tipo.name, conta.tipo.name)
        }

    }

    @Test
    internal fun `deve consultar chave salva no sistema com valor da chave`() {

        val chavePix = retornaChaveSalva()

        `when`(bcbClient.consulta(chavePix.valor))
            .thenReturn(HttpResponse.ok())

        val response = grpcClient.consulta(ConsultaPixRequest
            .newBuilder()
            .setChave(chavePix.valor)
            .build())

        with(response) {
            assertEquals(chavePix.id, pixId)
            assertEquals(chavePix.idTitular, clienteId)
            assertEquals(chavePix.tipoDeChave.name, tipoDeChave.name)
            assertEquals(chavePix.valor, valor)
            assertEquals(chavePix.conta.titular, titular.nome)
            assertEquals(chavePix.conta.cpf, titular.cpf)
            assertEquals(chavePix.conta.agencia, conta.agencia)
            assertEquals(chavePix.conta.numero, conta.numero)
            assertEquals(chavePix.conta.instituicao, conta.instituicao)
            assertEquals(chavePix.conta.tipo.name, conta.tipo.name)
        }

    }

    @Test
    internal fun `deve consultar chave no sistema BCB com valor da chave`() {

        val valorChave = "email@gmail.com"
        val respostaBcb = PixKeyDetailsResponse(
            keyType = "EMAIL",
            key = valorChave,
            bankAccount = BankAccount(
                participant = "60701190",
                branch = "0001",
                accountNumber = "12345",
                accountType = "CACC"
            ),
            owner = Owner(
                name = "Eduardo",
                taxIdNumber = "87821765074"
            ),
            createdAt = LocalDateTime.now()
        )
        assertTrue(repository.findAll().isEmpty())

        `when`(bcbClient.consulta(valorChave))
            .thenReturn(HttpResponse.ok(respostaBcb))

        val response = grpcClient.consulta(ConsultaPixRequest
            .newBuilder()
            .setChave(valorChave)
            .build())

        with(response) {
            assertTrue(pixId.isBlank())
            assertTrue(clienteId.isBlank())
            assertEquals(TipoDeChave.fromBcb(respostaBcb.keyType).name, tipoDeChave.name)
            assertEquals(respostaBcb.key, valor)
            assertEquals(respostaBcb.owner.name, titular.nome)
            assertEquals(respostaBcb.owner.taxIdNumber, titular.cpf)
            assertEquals(respostaBcb.bankAccount.branch, conta.agencia)
            assertEquals(respostaBcb.bankAccount.accountNumber, conta.numero)
            assertEquals(Instituicao.fromIspb(respostaBcb.bankAccount.participant), conta.instituicao)
            assertEquals(TipoDeConta.fromBcb(respostaBcb.bankAccount.accountType).name, conta.tipo.name)
            assertEquals(respostaBcb.createdAt,
                LocalDateTime.ofEpochSecond(criadaEm.seconds, criadaEm.nanos, ZoneOffset.of("Z")))
        }

    }

    @Test
    internal fun `deve retornar NOT_FOUND se a chave nao for encontrada no sistema`() {

        val chaveId = UUID.randomUUID().toString()
        val clienteId = UUID.randomUUID().toString()

        val request: ConsultaPixRequest = ConsultaPixRequest
            .newBuilder()
            .setInterna(ConsultaPixRequest.ConsultaInterna
                .newBuilder()
                .setPixId(chaveId)
                .setClienteId(clienteId)
                .build())
            .build()

        val erro = assertThrows<StatusRuntimeException> { grpcClient.consulta(request) }
        with(erro) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave de id $chaveId não encontrada no sistema",
                status.description)
        }

    }

    @Test
    internal fun `deve retornar NOT_FOUND se a chave for encontrada no sistema mas nao estiver no BCB`() {

        val chavePix = retornaChaveSalva()

        `when`(bcbClient.consulta(chavePix.valor))
            .thenReturn(HttpResponse.notFound())

        val request = ConsultaPixRequest
            .newBuilder()
            .setInterna(ConsultaPixRequest.ConsultaInterna
                .newBuilder()
                .setPixId(chavePix.id)
                .setClienteId(chavePix.idTitular)
                .build())
            .build()

        val erro = assertThrows<StatusRuntimeException> { grpcClient.consulta(request) }
        with(erro) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave de valor ${chavePix.valor} não consta no sistema BCB",
                status.description)
        }

    }

    @Test
    internal fun `deve retornar PERMISSION_DENIED se o cliente nao for dono da chave consultada`() {

        val chavePix: ChavePix = retornaChaveSalva()

        `when`(bcbClient.consulta(chavePix.valor))
            .thenReturn(HttpResponse.ok())

        val request: ConsultaPixRequest = ConsultaPixRequest
            .newBuilder()
            .setInterna(ConsultaPixRequest.ConsultaInterna
                .newBuilder()
                .setPixId(chavePix.id)
                .setClienteId(UUID.randomUUID().toString())
                .build())
            .build()

        val erro = assertThrows<StatusRuntimeException> { grpcClient.consulta(request) }
        with(erro) {
            assertEquals(Status.PERMISSION_DENIED.code, status.code)
            assertEquals("Chave de id ${request.interna.pixId} " +
                    "não pertence ao cliente de id ${request.interna.clienteId}",
                status.description)
        }

    }

    @Test
    internal fun `deve retornar NOT_FOUND se a chave nao for encontrada no BCB`() {

        val valorChave = "email@gmail.com"

        `when`(bcbClient.consulta(valorChave))
            .thenReturn(HttpResponse.notFound())

        val request: ConsultaPixRequest = ConsultaPixRequest
            .newBuilder()
            .setChave(valorChave)
            .build()

        val erro = assertThrows<StatusRuntimeException> { grpcClient.consulta(request) }
        with(erro) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave de valor $valorChave não consta no sistema BCB",
                status.description)
        }

    }

    @ParameterizedTest
    @MethodSource("geraDadosInvalidos")
    internal fun `nao deve buscar com id pix ou id cliente invalidos`(
        pixId: String, clienteId: String
    ) {

        val request = ConsultaPixRequest
            .newBuilder()
            .setInterna(
                ConsultaPixRequest.ConsultaInterna
                    .newBuilder()
                    .setPixId(pixId)
                    .setClienteId(clienteId)
                    .build()
            )
            .build()

        val erro = assertThrows<StatusRuntimeException> { grpcClient.consulta(request) }
        with(erro) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }

    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   "])
    internal fun `nao deve buscar com valor de chave invalido`(valorChave: String) {

        val request = ConsultaPixRequest
            .newBuilder()
            .setChave(valorChave)
            .build()

        val erro = assertThrows<StatusRuntimeException> { grpcClient.consulta(request) }
        with(erro) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }

    }

    @Test
    internal fun `nao deve buscar chave com request sem dados`() {
        val request = ConsultaPixRequest
            .newBuilder()
            .build()

        val erro = assertThrows<StatusRuntimeException> { grpcClient.consulta(request) }
        with(erro) {
            assertEquals(Status.UNKNOWN.code, status.code)
            assertEquals("UNKNOWN: Consulta não informada", message)
        }
    }

    @Factory
    class Cliente() {
        @Singleton
        fun grcpStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel) =
            newBlockingStub(channel)
    }

    @MockBean(BcbClient::class)
    fun bcbClient() = mock(BcbClient::class.java)

    private fun retornaChaveSalva() = repository.save(ChavePix(
        idTitular = UUID.randomUUID().toString(),
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

    companion object {
        @JvmStatic
        private fun geraDadosInvalidos(): Stream<Arguments> =
            Stream.of(
                Arguments.of("", UUID.randomUUID().toString()),
                Arguments.of(UUID.randomUUID().toString(), ""),
                Arguments.of("12345", "12345"),
                Arguments.of("12345", UUID.randomUUID().toString()),
                Arguments.of(UUID.randomUUID().toString(), "12345"),
            )
    }

}