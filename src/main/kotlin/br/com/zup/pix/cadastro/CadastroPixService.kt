package br.com.zup.pix.cadastro

import br.com.zup.compartilhado.exceptions.ChavePixExistenteException
import br.com.zup.compartilhado.exceptions.RecursoNaoEncontradoException
import br.com.zup.pix.ChavePix
import br.com.zup.pix.ChavePixRepository
import br.com.zup.pix.SistemaErpClient
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
            throw ChavePixExistenteException("Chave Pix ${novaChave.valor} já existente")
        }

        val httpResponse = client.retornaDadosCliente(novaChave.idTitular!!, novaChave.tipoDeConta!!.name)
        val conta = httpResponse.body() ?:
                    throw RecursoNaoEncontradoException("Cliente não existe ou não possui conta do tipo ${novaChave.tipoDeConta}")

        val chavePix = novaChave.toModel(conta.toModel())
        return repository.save(chavePix)
            .also { LOGGER.info("Chave Pix de ID ${chavePix.id} criada") }

    }
}