package br.com.zup.pix.cadastro

import br.com.zup.pix.ChavePix
import br.com.zup.pix.ChavePixRepository
import br.com.zup.pix.SistemaErpClient
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class CadastroPixService(
    @Inject val repository: ChavePixRepository,
    @Inject val client: SistemaErpClient
) {

    private val LOGGER = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun cadastrar(@Valid novaChave: NovaChavePix): ChavePix {

        if (repository.existsByValor(novaChave.valor!!)) {
            throw IllegalStateException("Chave Pix ${novaChave.valor} já existente")
        }

        val httpResponse = client.retornaDadosCliente(novaChave.idTitular!!, novaChave.tipoDeConta!!.name)
        val conta = httpResponse.body() ?: throw HttpStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado")

        val chavePix = novaChave.toModel(conta.toModel())
        return repository.save(chavePix)
            .also { LOGGER.info("Chave Pix de ID ${chavePix.id} criada") }

    }
}