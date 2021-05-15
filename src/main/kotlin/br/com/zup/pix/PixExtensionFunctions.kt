package br.com.zup.pix

import br.com.zup.CadastroPixRequest
import br.com.zup.pix.cadastro.NovaChavePix
import br.com.zup.pix.enums.TipoDeChave
import br.com.zup.pix.enums.TipoDeConta

fun CadastroPixRequest.converter(): NovaChavePix = NovaChavePix(
    idTitular = idTitular,
    tipoDeChave = if (tipoDeChave.number != 0) TipoDeChave.valueOf(tipoDeChave.name) else null,
    valor = valor,
    tipoDeConta = if (tipoDeConta.number != 0) TipoDeConta.valueOf(tipoDeConta.name) else null,
)