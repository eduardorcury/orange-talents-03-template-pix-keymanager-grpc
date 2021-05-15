package br.com.zup.pix

import br.com.zup.pix.conta.DadosContaResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${sistemas.erp.url}")
interface SistemaErpClient {

    @Get(value = "/api/v1/clientes/{clientId}/contas", produces = [APPLICATION_JSON])
    fun retornaDadosCliente(
        @PathVariable clientId: String,
        @QueryValue(value = "tipo") tipoDeConta: String,
    ): HttpResponse<DadosContaResponse?>

}