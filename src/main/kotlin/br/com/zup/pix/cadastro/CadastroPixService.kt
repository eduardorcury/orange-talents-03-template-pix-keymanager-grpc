package br.com.zup.pix.cadastro

import br.com.zup.compartilhado.exceptions.ChavePixExistenteException
import br.com.zup.compartilhado.exceptions.RecursoNaoEncontradoException
import br.com.zup.pix.BcbClient
import br.com.zup.pix.ChavePix
import br.com.zup.pix.ChavePixRepository
import br.com.zup.pix.SistemaErpClient
import br.com.zup.pix.enums.TipoDeChave
import br.com.zup.pix.enums.TipoDeChave.*
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import java.lang.IllegalStateException
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class CadastroPixService(
    @Inject val repository: ChavePixRepository,
    @Inject val erpClient: SistemaErpClient,
    @Inject val bcbClient: BcbClient
) {

    private val LOGGER = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun cadastrar(@Valid novaChave: NovaChavePix): ChavePix {

        if (repository.existsByValor(novaChave.valor!!)) {
            throw ChavePixExistenteException("Chave Pix ${novaChave.valor} já existente")
        }

        val httpResponse = erpClient.retornaDadosCliente(novaChave.idTitular!!, novaChave.tipoDeConta!!.name)
        val conta = httpResponse.body() ?:
                    throw RecursoNaoEncontradoException("Cliente não existe ou não possui conta do tipo ${novaChave.tipoDeConta}")

        val chavePix = CreatePixKeyRequest(chavePix = novaChave, conta = conta.toModel())
            .run { bcbClient.cadastra(this) }
            .let { bcbResponse -> if (bcbResponse.status == HttpStatus.CREATED) bcbResponse.body()
                                  else throw HttpClientException("BCB retornou erro: ${bcbResponse.status}")}
            .let { createPixKeyResponse ->
                if (createPixKeyResponse == null) throw HttpClientException("Sistema retornou uma resposta de corpo nulo")
                assert(createPixKeyResponse.dadosConsistentes(novaChave, conta.toModel()))
                createPixKeyResponse.toModel(novaChave.idTitular)
            }

        return repository.save(chavePix)
            .also { LOGGER.info("Chave Pix de ID ${chavePix.id} criada") }

    }
}