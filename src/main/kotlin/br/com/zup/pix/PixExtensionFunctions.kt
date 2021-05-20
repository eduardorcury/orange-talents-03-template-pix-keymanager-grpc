package br.com.zup.pix

import br.com.zup.*
import br.com.zup.ConsultaPixRequest.ConsultaCase.*
import br.com.zup.pix.cadastro.NovaChavePix
import br.com.zup.pix.consulta.Consulta
import br.com.zup.pix.consulta.Consulta.*
import br.com.zup.pix.deleta.DeletaChaveData
import br.com.zup.pix.enums.TipoDeChave
import br.com.zup.pix.enums.TipoDeConta
import com.google.protobuf.Timestamp
import java.time.ZoneId
import java.time.ZoneOffset
import javax.validation.ConstraintViolationException
import javax.validation.Validator

fun CadastroPixRequest.converter(): NovaChavePix = NovaChavePix(
    idTitular = idTitular,
    tipoDeChave = if (tipoDeChave.number != 0) TipoDeChave.valueOf(tipoDeChave.name) else null,
    valor = valor,
    tipoDeConta = if (tipoDeConta.number != 0) TipoDeConta.valueOf(tipoDeConta.name) else null,
)

fun DeletaPixRequest.converter(): DeletaChaveData = DeletaChaveData(
    pixId = pixId,
    clienteId = clienteId
)

fun ConsultaPixRequest.toModel(validator: Validator): Consulta {
    val consulta = when(consultaCase) {
        INTERNA -> ConsultaInterna(pixId = interna.pixId, clienteId = interna.clienteId)
        CHAVE -> ConsultaPorChave(chave = chave)
        CONSULTA_NOT_SET -> throw IllegalStateException("Consulta não informada")
    }
    validator.validate(consulta)
        .let { violations -> if (violations.isNotEmpty())
            throw ConstraintViolationException(violations) else return consulta }

}
