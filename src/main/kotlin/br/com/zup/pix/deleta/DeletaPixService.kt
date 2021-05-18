package br.com.zup.pix.deleta

import br.com.zup.compartilhado.exceptions.PermissaoNegadaException
import br.com.zup.compartilhado.exceptions.RecursoNaoEncontradoException
import br.com.zup.pix.ChavePix
import br.com.zup.pix.ChavePixRepository
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.validation.Validated
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import java.util.function.Consumer
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class DeletaPixService(
    @Inject val repository: ChavePixRepository,
) {

    private val LOGGER: Logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun deleta(@Valid request: DeletaChaveData) {

        val possivelChave: Optional<ChavePix> = repository.findById(request.pixId)

        possivelChave.ifPresentOrElse({ chavePix ->
            if (chavePix.idTitular != request.clienteId) {
                LOGGER.warn("Cliente de ID ${request.clienteId} tentou deletar uma chave que não pertence a ele")
                throw PermissaoNegadaException("Chave Pix de id ${request.pixId} " +
                        "não pertence ao usuário de id ${request.clienteId}")
            }
            repository.deleteById(request.pixId)
            LOGGER.info("Chave Pix de id ${request.pixId} deletada")
        }) { throw RecursoNaoEncontradoException("Chave Pix de id ${request.pixId} não encontrada") }

    }

}