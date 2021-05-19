package br.com.zup.pix.cadastro

import br.com.zup.compartilhado.exceptions.ChavePixExistenteException
import br.com.zup.compartilhado.exceptions.RecursoNaoEncontradoException
import br.com.zup.pix.BcbClient
import br.com.zup.pix.ChavePixRepository
import br.com.zup.pix.SistemaErpClient
import br.com.zup.pix.conta.DadosContaResponse
import br.com.zup.pix.converter
import br.com.zup.pix.enums.TipoDeChave
import br.com.zup.pix.enums.TipoDeConta
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class CadastroPixServiceTest {

    @field:Inject
    lateinit var service: CadastroPixService

    @field:Inject
    lateinit var repository: ChavePixRepository

    @field:Inject
    lateinit var erpClient: SistemaErpClient

    @field:Inject
    lateinit var bcbClient: BcbClient

    @BeforeEach
    internal fun setUp() {
        repository.deleteAll()
    }

    @Test
    internal fun `deve salvar nova chave no banco de dados`() {

        val novaChavePix = NovaChavePix(
            idTitular = UUID.randomUUID().toString(),
            tipoDeChave = TipoDeChave.EMAIL,
            valor = "email@gmail.com",
            tipoDeConta = TipoDeConta.CONTA_CORRENTE
        )

        val dadosConta = DadosContaResponse(
            tipoDeConta = novaChavePix.tipoDeConta,
            instituicao = "ITAÚ",
            agencia = "0001",
            numero = "12345",
            titular = "Eduardo",
            cpf = "87821765074"
        )

        val bcbRequest = CreatePixKeyRequest(
            chavePix = novaChavePix,
            conta = dadosConta.toModel()
        )

        `when`(erpClient.retornaDadosCliente(
            novaChavePix.idTitular!!,
            novaChavePix.tipoDeConta!!.name)
        ).thenReturn(HttpResponse.ok(dadosConta))

        `when`(bcbClient.cadastra(bcbRequest))
            .thenReturn(HttpResponse.created(respostaBcb(bcbRequest)))

        service.cadastrar(novaChavePix)

        val chaves = repository.findAll()
        assertTrue(chaves.isNotEmpty())
        with(chaves[0]) {
            assertTrue(idTitular == novaChavePix.idTitular)
            assertTrue(tipoDeChave == novaChavePix.tipoDeChave)
            assertTrue(valor == novaChavePix.valor)
            assertTrue(conta.tipo == novaChavePix.tipoDeConta)
            assertTrue(conta.tipo == dadosConta.tipoDeConta)
            assertTrue(conta.instituicao == dadosConta.instituicao)
            assertTrue(conta.agencia == dadosConta.agencia)
            assertTrue(conta.numero == dadosConta.numero)
            assertTrue(conta.titular == dadosConta.titular)
        }

    }

    @Test
    internal fun `deve lancar ChavePixExistenteException para chave ja existente`() {

        val novaChavePix = NovaChavePix(
            idTitular = UUID.randomUUID().toString(),
            tipoDeChave = TipoDeChave.EMAIL,
            valor = "email@gmail.com",
            tipoDeConta = TipoDeConta.CONTA_CORRENTE
        )

        val dadosConta = DadosContaResponse(
            tipoDeConta = novaChavePix.tipoDeConta,
            instituicao = "ITAÚ",
            agencia = "0001",
            numero = "12345",
            titular = "Eduardo",
            cpf = "87821765074"
        )

        val bcbRequest = CreatePixKeyRequest(
            chavePix = novaChavePix,
            conta = dadosConta.toModel()
        )

        `when`(erpClient.retornaDadosCliente(
            novaChavePix.idTitular!!,
            novaChavePix.tipoDeConta!!.name)
        ).thenReturn(HttpResponse.ok(dadosConta))

        `when`(bcbClient.cadastra(bcbRequest))
            .thenReturn(HttpResponse.created(respostaBcb(bcbRequest)))

        service.cadastrar(novaChavePix)

        assertThrows<ChavePixExistenteException>(
            message = "Chave Pix ${novaChavePix.valor} já existente"
        ) { service.cadastrar(novaChavePix) }

    }

    @Test
    internal fun `deve lancar RecursoNaoEncontradoException para cliente nao encontrado`() {

        val novaChavePix = NovaChavePix(
            idTitular = UUID.randomUUID().toString(),
            tipoDeChave = TipoDeChave.EMAIL,
            valor = "email@gmail.com",
            tipoDeConta = TipoDeConta.CONTA_CORRENTE
        )

        `when`(erpClient.retornaDadosCliente(
            novaChavePix.idTitular!!,
            novaChavePix.tipoDeConta!!.name)
        ).thenReturn(HttpResponse.ok())

        assertThrows<RecursoNaoEncontradoException>(
            message = "Cliente não existe ou não possui conta do tipo ${novaChavePix.tipoDeConta}"
        ) { service.cadastrar(novaChavePix) }

    }

    @MockBean(SistemaErpClient::class)
    fun erpClient() = mock(SistemaErpClient::class.java)

    @MockBean(BcbClient::class)
    fun bcbClient() = mock(BcbClient::class.java)

    private fun respostaBcb(bcbRequest: CreatePixKeyRequest): CreatePixKeyResponse =
        with(bcbRequest) {
            CreatePixKeyResponse(
                keyType = keyType,
                key = key!!,
                bankAccount = bankAccount,
                owner = owner,
                createdAt = LocalDateTime.now()
            )
        }

}