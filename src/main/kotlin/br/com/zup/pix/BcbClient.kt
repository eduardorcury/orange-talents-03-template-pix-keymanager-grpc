package br.com.zup.pix

import br.com.zup.pix.cadastro.CreatePixKeyRequest
import br.com.zup.pix.cadastro.CreatePixKeyResponse
import br.com.zup.pix.cadastro.DeletePixKeyRequest
import br.com.zup.pix.cadastro.DeletePixKeyResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType.APPLICATION_XML
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client

@Client("\${sistemas.bcb.url}")
interface BcbClient {

    @Post(value = "/api/v1/pix/keys", consumes = [APPLICATION_XML], produces = [APPLICATION_XML])
    fun cadastra(@Body chave: CreatePixKeyRequest) : HttpResponse<CreatePixKeyResponse?>

    @Delete(value = "/api/v1/pix/keys/{valorChave}", consumes = [APPLICATION_XML], produces = [APPLICATION_XML])
    fun deleta(@Body request: DeletePixKeyRequest, @PathVariable valorChave: String) : HttpResponse<DeletePixKeyResponse?>

}