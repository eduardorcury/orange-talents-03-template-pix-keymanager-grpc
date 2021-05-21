package br.com.zup.compartilhado.handlers

import com.google.rpc.Code
import com.google.rpc.Status
import javax.inject.Singleton

@Singleton
class DefaultExceptionHandler : ExceptionHandler<Exception> {

    override fun handle(exception: Exception) =
        Status
            .newBuilder()
            .setCode(Code.UNKNOWN_VALUE)
            .setMessage(exception.message)
            .build()

    override fun supports(exception: Exception) = false

}