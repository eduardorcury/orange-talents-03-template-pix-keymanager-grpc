package br.com.zup.pix.conta

import br.com.zup.pix.enums.TipoDeConta
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class DadosContaDeserializerTest {

    @Test
    internal fun `deve desserializar dados da conta corretamente`() {

        val mapper = ObjectMapper()
        val jsonParser = mapper.factory.createParser("{\n" +
                "  \"tipo\": \"CONTA_CORRENTE\",\n" +
                "  \"instituicao\": {\n" +
                "    \"nome\": \"ITAÚ UNIBANCO S.A.\",\n" +
                "    \"ispb\": \"60701190\"\n" +
                "  },\n" +
                "  \"agencia\": \"0001\",\n" +
                "  \"numero\": \"291900\",\n" +
                "  \"titular\": {\n" +
                "    \"id\": \"c56dfef4-7901-44fb-84e2-a2cefb157890\",\n" +
                "    \"nome\": \"Rafael M C Ponte\",\n" +
                "    \"cpf\": \"02467781054\"\n" +
                "  }\n" +
                "}")

        val dadosConta = DadosContaResponse(
            tipoDeConta = TipoDeConta.valueOf("CONTA_CORRENTE"),
            instituicao = "ITAÚ UNIBANCO S.A.",
            agencia = "0001",
            numero = "291900",
            titular = "Rafael M C Ponte"
        )

        val response = DadosContaDeserializer()
            .deserialize(parser = jsonParser, ctxt = mapper.deserializationContext)

        assertEquals(dadosConta, response)

    }
}