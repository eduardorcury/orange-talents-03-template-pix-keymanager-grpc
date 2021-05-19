package br.com.zup.pix.conta

import br.com.zup.pix.enums.TipoDeConta
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

class DadosContaDeserializer :
    StdDeserializer<DadosContaResponse>(DadosContaResponse::class.java) {

    override fun deserialize(parser: JsonParser, ctxt: DeserializationContext?): DadosContaResponse {

        val node: JsonNode = parser.getCodec().readTree(parser)
        return DadosContaResponse(
            tipoDeConta = TipoDeConta.valueOf(node.get("tipo").textValue()),
            instituicao = node.get("instituicao").get("nome").textValue(),
            agencia = node.get("agencia").textValue(),
            numero = node.get("numero").textValue(),
            titular = node.get("titular").get("nome").textValue(),
            cpf = node.get("titular").get("cpf").textValue()
        )

    }

}