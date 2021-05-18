package br.com.zup.pix.deleta

import br.com.zup.pix.validacao.UUID
import javax.validation.constraints.NotNull

data class DeletaChaveData(

    @field:UUID
    @field:NotNull
    val pixId: String,

    @field:UUID
    @field:NotNull
    val clienteId: String,

)
