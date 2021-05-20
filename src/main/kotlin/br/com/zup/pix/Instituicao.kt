package br.com.zup.pix

class Instituicao {

    companion object {
        fun fromIspb(ispb: String): String {
            return when(ispb) {
                "60701190" -> "ITAÚ UNIBANCO"
                else -> "Banco não encontrado"
            }
        }
    }

}