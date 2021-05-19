package br.com.zup.pix.enums

enum class TipoDeConta {

    CONTA_CORRENTE {
        override val nomeBCB: String = "CACC"
    }, CONTA_POUPANCA {
        override val nomeBCB: String = "SVGS"
    };

    abstract val nomeBCB: String

    companion object {
        fun fromBcb(valor: String): TipoDeConta {
            return when(valor) {
                "CACC" -> CONTA_CORRENTE
                "SVGS" -> CONTA_POUPANCA
                else -> throw IllegalStateException("Tipo de conta $valor não suportado")
            }
        }
    }

}