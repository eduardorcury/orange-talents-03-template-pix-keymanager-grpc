package br.com.zup.compartilhado.handlers

import com.google.rpc.Code
import com.google.rpc.Status
import io.micronaut.http.client.exceptions.HttpClientException
import javax.inject.Singleton

@Singleton
class HttpClientExceptionHandler : ExceptionHandler<HttpClientException> {

    override fun handle(exception: HttpClientException) =
        Status
            .newBuilder()
            .setMessage(exception.message)
            .setCode(Code.INTERNAL_VALUE)
            .build()

    override fun supports(exception: Exception) = exception is HttpClientException

}